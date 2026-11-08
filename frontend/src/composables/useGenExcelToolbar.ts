import { computed, onUnmounted, ref, type Ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Download, Upload } from '@element-plus/icons-vue'
import type { GenCrudApi, GenCrudPerm } from '@/types/gen/crudApi'
import type { GenViewToolbarAction } from '@/types/tool/genView'
import { downloadByApi, isAbortError } from '@/utils/download'
import { buildQueryString } from '@/utils/queryString'
import { ASYNC_TASK_STATUS, AsyncTaskPollError, pollAsyncTask } from '@/utils/asyncTaskPoll'
import { resolveI18nPayloadMessage } from '@/utils/asyncTaskError'
import { isApiMessageKey } from '@/utils/apiError'

export type GenExcelToolbarOptions = {
  apiModule: string | Ref<string> | (() => string)
  businessName: string | Ref<string> | (() => string)
  api: GenCrudApi | Ref<GenCrudApi> | (() => GenCrudApi)
  perm: GenCrudPerm | Ref<GenCrudPerm> | (() => GenCrudPerm)
  readOnly: Ref<boolean> | (() => boolean)
  /** Current list query; pageNo/pageSize stripped for export */
  exportQuery: Ref<Record<string, unknown>> | (() => Record<string, unknown>)
  /** Resolve page i18n label keys from view.json */
  resolveLabel?: (key: string) => string
  toolbarActions?: Ref<GenViewToolbarAction[] | undefined> | (() => GenViewToolbarAction[] | undefined)
}

function unwrap<T>(value: T | Ref<T> | (() => T)): T {
  if (typeof value === 'function') return (value as () => T)()
  if (value && typeof value === 'object' && 'value' in (value as object)) {
    return (value as Ref<T>).value
  }
  return value as T
}

/** Strip pagination params so export is filter-scoped, not UI page-sized. */
export function toExcelExportQuery(query: Record<string, unknown>): Record<string, unknown> {
  const out: Record<string, unknown> = { ...query }
  delete out.pageNo
  delete out.pageSize
  return out
}

/**
 * Shared Excel export/import toolbar for GenCrud / GenTree / GenMasterDetail.
 */
export function useGenExcelToolbar(options: GenExcelToolbarOptions) {
  const { t } = useI18n()
  const exporting = ref(false)
  const exportDialogVisible = ref(false)
  const pendingExportAction = ref<GenViewToolbarAction | null>(null)
  const importDialogVisible = ref(false)
  const pendingImportAction = ref<GenViewToolbarAction | null>(null)
  let exportAbort: AbortController | null = null

  onUnmounted(() => {
    exportAbort?.abort()
    exportAbort = null
  })

  const declaredActions = computed(() => {
    const actions =
      (options.toolbarActions ? unwrap(options.toolbarActions) : undefined) ?? []
    // Excel toolbar only renders builtin export/import (avoids dead custom buttons).
    return actions.filter((a) => a.builtin === 'export' || a.builtin === 'import')
  })

  const canChooseExportMode = computed(() => {
    const api = unwrap(options.api)
    return Boolean(api.exportFileAsync)
  })

  const canChooseImportMode = computed(() => {
    const api = unwrap(options.api)
    return Boolean(api.importFileAsync && (api.importFile || api.importPath))
  })

  if (import.meta.env.DEV) {
    watch(
      () => (options.toolbarActions ? unwrap(options.toolbarActions) : undefined) ?? [],
      (actions) => {
        const ignored = actions.filter((a) => a.builtin !== 'export' && a.builtin !== 'import')
        if (ignored.length) {
          console.warn(
            '[GenExcelToolbar] non-export/import toolbarActions are ignored by the Excel kit:',
            ignored.map((a) => a.id),
          )
        }
      },
      { immediate: true },
    )
  }

  function isReadOnly() {
    return unwrap(options.readOnly)
  }

  function toolbarActionPerm(action: GenViewToolbarAction) {
    const perm = unwrap(options.perm)
    const key =
      action.permKey ??
      (action.builtin === 'export' ? 'export' : action.builtin === 'import' ? 'import' : undefined)
    const code = key ? perm[key] : undefined
    return code ? [code] : []
  }

  function toolbarActionVisible(action: GenViewToolbarAction) {
    // Builtin Excel actions require an explicit perm code — never render an unguarded button.
    if (
      (action.builtin === 'export' || action.builtin === 'import') &&
      toolbarActionPerm(action).length === 0
    ) {
      return false
    }
    if (!isReadOnly()) return true
    if (action.builtin === 'export') return action.allowOnReadOnly !== false
    return action.allowOnReadOnly === true
  }

  function toolbarActionLabel(action: GenViewToolbarAction) {
    if (action.labelKey && options.resolveLabel) return options.resolveLabel(action.labelKey)
    if (action.builtin === 'export') return t('common.export')
    if (action.builtin === 'import') return t('common.import')
    return action.id
  }

  function toolbarActionIcon(action: GenViewToolbarAction) {
    if (action.builtin === 'export') return Download
    if (action.builtin === 'import') return Upload
    return undefined
  }

  function toolbarActionType(action: GenViewToolbarAction) {
    if (action.type && action.type !== 'default') return action.type
    if (action.builtin === 'export') return 'success'
    if (action.builtin === 'import') return 'warning'
    return undefined
  }

  function toolbarActionLoading(action: GenViewToolbarAction) {
    if (action.builtin === 'export') return exporting.value
    return false
  }

  async function runToolbarAction(action: GenViewToolbarAction) {
    if (action.builtin === 'export') {
      if (canChooseExportMode.value) {
        pendingExportAction.value = action
        exportDialogVisible.value = true
        return
      }
      await runBuiltinExport(action, 'async')
      return
    }
    if (action.builtin === 'import') {
      pendingImportAction.value = action
      importDialogVisible.value = true
    }
  }

  async function confirmExport(mode: 'sync' | 'async') {
    const action = pendingExportAction.value
    if (!action || exporting.value) return
    await runBuiltinExport(action, mode)
  }

  function resolveExportFileName(action: GenViewToolbarAction) {
    if (action.fileName) return action.fileName
    const fromPage = options.resolveLabel?.('exportFileName')
    if (fromPage && fromPage !== 'exportFileName') return fromPage
    return `${unwrap(options.businessName)}.xlsx`
  }

  async function runSyncExport(
    action: GenViewToolbarAction,
    api: GenCrudApi,
    query: Record<string, unknown>,
    fileName: string,
    signal?: AbortSignal,
  ) {
    if (api.exportFile && !action.exportPath && !api.exportPath) {
      await api.exportFile(query)
      return
    }
    const apiModule = unwrap(options.apiModule)
    const businessName = unwrap(options.businessName)
    const path = action.exportPath || api.exportPath || `/${apiModule}/${businessName}/export`
    await downloadByApi(`${path}${buildQueryString(query)}`, fileName, { signal })
  }

  async function runBuiltinExport(action: GenViewToolbarAction, mode: 'sync' | 'async') {
    const query = toExcelExportQuery(unwrap(options.exportQuery))
    const api = unwrap(options.api)
    const fileName = resolveExportFileName(action)
    exporting.value = true
    exportAbort?.abort()
    exportAbort = new AbortController()
    const signal = exportAbort.signal
    const preferAsync =
      Boolean(api.exportFileAsync) &&
      !action.exportPath &&
      !api.exportPath &&
      mode === 'async'
    try {
      if (preferAsync) {
        try {
          await runAsyncExport(api, query, fileName, signal)
          exportDialogVisible.value = false
          pendingExportAction.value = null
          return
        } catch (e) {
          if (signal.aborted || isAbortError(e)) return
          if (isApiMessageKey(e, 'error.async.disabled')) {
            if (mode === 'async' && canChooseExportMode.value) {
              ElMessage.warning(t('error.async.disabled'))
              return
            }
            ElMessage.warning(t('common.exportAsyncFallbackSync'))
            await runSyncExport(action, api, query, fileName, signal)
            exportDialogVisible.value = false
            pendingExportAction.value = null
            return
          }
          throw e
        }
      }
      await runSyncExport(action, api, query, fileName, signal)
      exportDialogVisible.value = false
      pendingExportAction.value = null
    } catch (e) {
      if (signal.aborted || isAbortError(e)) return
      ElMessage.error(e instanceof Error ? e.message : t('common.downloadFailed'))
    } finally {
      exporting.value = false
    }
  }

  async function runAsyncExport(
    api: GenCrudApi,
    query: Record<string, unknown>,
    fallbackFileName: string,
    signal: AbortSignal,
  ) {
    ElMessage.info(t('common.exportAsyncSubmitted'))
    const submitted = await api.exportFileAsync!(query)
    if (signal.aborted) return
    const taskId = submitted?.taskId
    if (!taskId) {
      ElMessage.error(t('common.exportAsyncFailed'))
      return
    }
    try {
      const task = await pollAsyncTask(taskId, { signal })
      if (signal.aborted) return
      if (task.taskStatus === ASYNC_TASK_STATUS.FAILED) {
        ElMessage.error(
          resolveI18nPayloadMessage(t, task.errorMessage, {
            fallbackKey: 'common.exportAsyncFailed',
          }),
        )
        return
      }
      if (task.taskStatus === ASYNC_TASK_STATUS.CANCELLED) {
        ElMessage.error(t('common.exportAsyncCancelled'))
        return
      }
      const payload = parseExportResult(task.resultData)
      if (!payload?.fileId) {
        ElMessage.error(t('common.exportAsyncFailed'))
        return
      }
      await downloadByApi(
        `/infra/file/${payload.fileId}/download`,
        payload.fileName || fallbackFileName,
        { signal },
      )
      if (signal.aborted) return
      ElMessage.success(t('common.exportAsyncSuccess'))
    } catch (e) {
      if (signal.aborted || isAbortError(e)) return
      if (e instanceof AsyncTaskPollError) {
        if (e.code === 'timeout') ElMessage.error(t('common.exportAsyncTimeout'))
        else if (e.code === 'pending_timeout') ElMessage.warning(t('common.exportAsyncStillQueued'))
        return
      }
      throw e
    }
  }

  function parseExportResult(raw?: string | null): { fileId?: string; fileName?: string } | null {
    if (!raw?.trim()) return null
    try {
      return JSON.parse(raw) as { fileId?: string; fileName?: string }
    } catch {
      return null
    }
  }

  return {
    declaredActions,
    exporting,
    exportDialogVisible,
    canChooseExportMode,
    canChooseImportMode,
    importDialogVisible,
    pendingImportAction,
    toolbarActionVisible,
    toolbarActionPerm,
    toolbarActionLabel,
    toolbarActionIcon,
    toolbarActionType,
    toolbarActionLoading,
    runToolbarAction,
    confirmExport,
  }
}
