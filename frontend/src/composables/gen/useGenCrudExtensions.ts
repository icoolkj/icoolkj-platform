import { computed, reactive, watch, type ComputedRef, type Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDialogWidget,
  getRowActionHandler,
  type GenRowActionHandler,
} from '@/components/gen/registry/extension-blocks/registry'
import { useExcelImportExport } from '@/composables/useExcelImportExport'
import {
  evaluateRowActionVisible,
  evaluateSelectionFilter,
} from '@/composables/useGenRowActionRules'
import { useDemoStore } from '@/stores/demo'
import type { GenCrudApi, GenCrudPerm } from '@/types/gen/crudApi'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import type { GenViewRowAction, GenViewSchema, GenViewToolbarAction } from '@/types/tool/genView'
import {
  GEN_DETAIL_DIALOG_ID,
  GEN_DIALOG_ID_PROP,
  GEN_INTERACTIVE_DIALOG_CHROME,
} from '@/composables/gen/genCrudConstants'
import GenDetailDialog from '@/components/gen/dialogs/GenDetailDialog.vue'

export type UseGenCrudExtensionsOptions = {
  view: ComputedRef<GenViewSchema>
  perm: GenCrudPerm
  api: GenCrudApi
  businessName: string
  apiModule: string
  i18nFiles: Record<string, GenI18nMessages>
  readOnly: ComputedRef<boolean>
  gp: (key: string, params?: Record<string, string | number>) => string
  actionHandlers?: Record<string, GenRowActionHandler>
  rowActionVisible?: (
    action: GenViewRowAction,
    row: Record<string, unknown>,
  ) => boolean | undefined
  loadPage: () => Promise<void>
  openForm: (row?: Record<string, unknown>) => void
  handleDelete: (row: Record<string, unknown>) => Promise<void>
  selectedRows: Ref<Record<string, unknown>[]>
  clearSelection: () => void
  selectionFilterPreset: ComputedRef<string | undefined>
}

export function useGenCrudExtensions(options: UseGenCrudExtensionsOptions) {
  const { t } = useI18n()
  const demoStore = useDemoStore()

  const excelToolbarActions = computed(() => {
    const fromView = options.view.value.extensions?.toolbarActions ?? []
    if (fromView.some((a) => a.builtin === 'export' || a.builtin === 'import')) {
      return fromView.filter((a) => a.builtin === 'export' || a.builtin === 'import')
    }
    return useExcelImportExport({
      businessName: options.businessName,
      apiModule: options.apiModule,
      api: options.api,
      perm: options.perm,
    }).actions
  })

  const batchDeleteToolbarActions = computed(
    () =>
      options.view.value.extensions?.toolbarActions?.filter((a) => a.builtin === 'batchDelete') ??
      [],
  )

  const declaredRowActions = computed(() => options.view.value.extensions?.rowActions ?? [])

  const tableActions = computed(() => ({
    edit: !options.readOnly.value && options.view.value.table?.actions?.edit !== false,
    delete: !options.readOnly.value && options.view.value.table?.actions?.delete !== false,
    width: options.view.value.table?.actions?.width,
  }))

  const dialogState = reactive<Record<string, { visible: boolean; props: Record<string, unknown> }>>(
    {},
  )

  const hasDetailDialog = computed(
    () => (options.view.value.detailDialog?.fields?.length ?? 0) > 0,
  )

  watch(
    () => [
      ...(options.view.value.extensions?.dialogs ?? []),
      ...(hasDetailDialog.value ? [GEN_DETAIL_DIALOG_ID] : []),
    ],
    (ids) => {
      for (const id of ids) {
        if (!dialogState[id]) {
          dialogState[id] = { visible: false, props: {} }
        }
      }
    },
    { immediate: true },
  )

  const mountedDialogs = computed(() => {
    const map: Record<string, object> = {}
    if (hasDetailDialog.value) {
      map[GEN_DETAIL_DIALOG_ID] = GenDetailDialog
    }
    for (const id of options.view.value.extensions?.dialogs ?? []) {
      const comp = getDialogWidget(id)
      if (comp) map[id] = comp
    }
    return map
  })

  const dialogPageContext = computed(() => ({
    fileView: options.view.value,
    businessName: options.businessName,
    apiModule: options.apiModule,
    i18nFiles: options.i18nFiles,
    get: options.api.get,
  }))

  function tryGp(key: string) {
    const text = options.gp(key)
    return text && text !== key ? text : ''
  }

  function actionVisible(action: GenViewRowAction, row: Record<string, unknown>) {
    if (demoStore.isDemo && action.builtin === 'delete') return false
    if (options.rowActionVisible) {
      const override = options.rowActionVisible(action, row)
      if (typeof override === 'boolean') return override
    }
    if (!evaluateRowActionVisible(action, row)) return false
    if (options.readOnly.value) return action.allowOnReadOnly === true
    return true
  }

  function actionDisabled(action: GenViewRowAction, row: Record<string, unknown>) {
    if (!action.disabledWhen) return false
    const current = String(row[action.disabledWhen.field] ?? '')
    return action.disabledWhen.values.includes(current)
  }

  function actionPerm(action: GenViewRowAction): string[] {
    if (action.allowOnReadOnly && !action.permKey && !action.builtin) return []
    if (action.builtin === 'detail' && action.allowOnReadOnly) return []
    const key = action.permKey ?? (action.builtin === 'delete' ? 'delete' : 'update')
    const code = options.perm[key]
    return code ? [code] : []
  }

  function actionLabel(action: GenViewRowAction) {
    if (action.labelKey) return options.gp(action.labelKey)
    if (action.builtin === 'edit') return t('common.edit')
    if (action.builtin === 'delete') return t('common.delete')
    if (action.builtin === 'detail') {
      return action.labelKey ? options.gp(action.labelKey) : options.gp('detail')
    }
    return action.id
  }

  async function runRowAction(action: GenViewRowAction, row: Record<string, unknown>) {
    if (actionDisabled(action, row)) return
    if (action.builtin === 'edit') {
      options.openForm(row)
      return
    }
    if (action.builtin === 'delete') {
      await options.handleDelete(row)
      return
    }
    if (action.builtin === 'detail') {
      if (!hasDetailDialog.value) {
        console.warn('[GenCrud] row action builtin:detail requires view.detailDialog.fields')
        return
      }
      openDetailDialog(row, action)
      return
    }
    if (action.dialog) {
      if (action.dialog === GEN_DETAIL_DIALOG_ID) {
        openDetailDialog(row, action)
        return
      }
      const state = dialogState[action.dialog]
      if (!state) return
      const rowKey = action.dialogRowKey ?? 'id'
      const idProp = action.dialogIdProp ?? GEN_DIALOG_ID_PROP[action.dialog] ?? 'id'
      const title =
        tryGp('detailTitle') ||
        (action.labelKey ? tryGp(`${action.labelKey}Title`) : '') ||
        options.gp('title')
      const propsForDialog: Record<string, unknown> = {
        [idProp]: row[rowKey] != null ? String(row[rowKey]) : null,
        title,
      }
      if (GEN_INTERACTIVE_DIALOG_CHROME.has(action.dialog)) {
        propsForDialog.cancelText = t('common.cancel')
        propsForDialog.confirmText = t('common.confirm')
        propsForDialog.successText = t('common.success')
        propsForDialog.emptyText = tryGp('noRolesAvailable') || ''
      }
      state.props = propsForDialog
      state.visible = true
      return
    }
    if (action.handler) {
      const fn = options.actionHandlers?.[action.handler] ?? getRowActionHandler(action.handler)
      if (!fn) {
        console.warn(`[GenCrud] unregistered row action handler: ${action.handler}`)
        return
      }
      await fn({ row, gp: options.gp, t, reload: options.loadPage })
    }
  }

  function openDetailDialog(row: Record<string, unknown>, action: GenViewRowAction) {
    const state = dialogState[GEN_DETAIL_DIALOG_ID]
    if (!state) return
    const rowKey = action.dialogRowKey ?? 'id'
    const idProp = action.dialogIdProp ?? 'id'
    const title =
      tryGp('detailTitle') ||
      (action.labelKey ? tryGp(`${action.labelKey}Title`) : '') ||
      (action.labelKey ? tryGp(action.labelKey) : '') ||
      options.gp('title')
    state.props = {
      [idProp]: row[rowKey] != null ? String(row[rowKey]) : null,
      title,
    }
    state.visible = true
  }

  function batchDeletePerm(action: GenViewToolbarAction): string[] {
    const key = action.permKey ?? 'delete'
    const code = options.perm[key]
    return code ? [code] : []
  }

  function batchDeleteLabel(action: GenViewToolbarAction) {
    if (action.labelKey) return options.gp(action.labelKey)
    return options.gp('batchDelete')
  }

  async function handleBatchDelete(action: GenViewToolbarAction) {
    if (!options.api.batchDelete) {
      console.warn('[GenCrud] batchDelete requires api.batchDelete')
      return
    }
    const filter = action.selectionFilter ?? options.selectionFilterPreset.value
    const ids = options.selectedRows.value
      .filter((row) => evaluateSelectionFilter(filter, row))
      .map((row) => String(row.id))
      .filter(Boolean)
    if (!ids.length) return
    await ElMessageBox.confirm(
      options.gp('batchDeleteConfirm', { count: ids.length }),
      t('common.tip'),
    )
    await options.api.batchDelete({ ids })
    ElMessage.success(t('common.success'))
    options.clearSelection()
    await options.loadPage()
  }

  return {
    excelToolbarActions,
    batchDeleteToolbarActions,
    declaredRowActions,
    tableActions,
    mountedDialogs,
    dialogState,
    dialogPageContext,
    actionVisible,
    actionDisabled,
    actionPerm,
    actionLabel,
    runRowAction,
    batchDeletePerm,
    batchDeleteLabel,
    handleBatchDelete,
  }
}

export type GenCrudExtensionsReturn = ReturnType<typeof useGenCrudExtensions>
