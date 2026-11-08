<template>
  <GenFormShell v-model="visible" :title="t('common.import')" :width="560">
    <div class="gen-excel-import">
      <section class="gen-excel-import__step" aria-labelledby="gen-excel-import-step1-title">
        <div class="gen-excel-import__step-head">
          <span class="gen-excel-import__step-index" aria-hidden="true">1</span>
          <div class="gen-excel-import__step-copy">
            <div id="gen-excel-import-step1-title" class="gen-excel-import__step-title">
              {{ t('common.importStepTemplate') }}
            </div>
            <div class="gen-excel-import__step-desc" id="gen-excel-import-step1-hint">
              {{ templateHint }}
            </div>
          </div>
        </div>
        <div class="gen-excel-import__actions">
          <el-button
            type="primary"
            plain
            :icon="Download"
            :loading="downloading"
            :aria-describedby="'gen-excel-import-step1-hint'"
            @click="downloadTemplate"
          >
            {{ t('common.importTemplate') }}
          </el-button>
        </div>
      </section>

      <section class="gen-excel-import__step" aria-labelledby="gen-excel-import-step2-title">
        <div class="gen-excel-import__step-head">
          <span class="gen-excel-import__step-index" aria-hidden="true">2</span>
          <div class="gen-excel-import__step-copy">
            <div id="gen-excel-import-step2-title" class="gen-excel-import__step-title">
              {{ t('common.importStepUpload') }}
            </div>
            <div class="gen-excel-import__step-desc" id="gen-excel-import-step2-hint">
              {{ uploadHint }}
            </div>
          </div>
        </div>
        <el-upload
          class="gen-excel-import__upload"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xlsx"
          :file-list="fileList"
          :on-change="onFileChange"
          :on-remove="onFileRemove"
          :on-exceed="onExceed"
          :disabled="submitInFlight"
          aria-describedby="gen-excel-import-step2-hint"
        >
          <el-icon class="gen-excel-import__upload-icon"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            {{ t('common.importDropHint') }}
            <em>{{ t('common.importClickHint') }}</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">{{ t('common.importXlsxOnly') }}</div>
          </template>
        </el-upload>
      </section>

      <section v-if="canChooseExecMode" class="gen-excel-import__step" aria-labelledby="gen-excel-import-mode-title">
        <div class="gen-excel-import__step-head">
          <span class="gen-excel-import__step-index" aria-hidden="true">3</span>
          <div class="gen-excel-import__step-copy">
            <div id="gen-excel-import-mode-title" class="gen-excel-import__step-title">
              {{ t('common.excelExecMode') }}
            </div>
          </div>
        </div>
        <el-radio-group v-model="importExecMode" size="small" :disabled="submitInFlight">
          <el-radio-button value="sync">{{ t('common.importSync') }}</el-radio-button>
          <el-radio-button value="async">{{ t('common.importAsync') }}</el-radio-button>
        </el-radio-group>
      </section>

      <section
        v-if="importErrors.length"
        class="gen-excel-import__errors"
        role="alert"
        aria-live="polite"
      >
        <div class="gen-excel-import__errors-title">
          {{ t('common.importFailedCount', { count: errorTotal }) }}
        </div>
        <el-table :data="importErrors" size="small" max-height="220" border stripe>
          <el-table-column
            prop="rowNumber"
            :label="t('common.importErrorRow')"
            width="88"
            align="center"
          />
          <el-table-column :label="t('common.importErrorMessage')" min-width="200">
            <template #default="{ row }">
              {{ resolveRowError(row.message) }}
            </template>
          </el-table-column>
        </el-table>
        <div v-if="errorTotal > importErrors.length" class="gen-excel-import__errors-more">
          {{ t('common.importErrorsTruncated', { shown: importErrors.length, total: errorTotal }) }}
        </div>
      </section>
    </div>
    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button
        type="primary"
        :icon="Upload"
        :loading="submitInFlight"
        :disabled="!selectedFile || submitInFlight"
        @click="submit"
      >
        {{
          canChooseExecMode && importExecMode === 'async'
            ? t('common.importAsync')
            : canChooseExecMode && importExecMode === 'sync'
              ? t('common.importSync')
              : t('common.import')
        }}
      </el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type UploadFile, type UploadUserFile } from 'element-plus'
import { Download, Upload, UploadFilled } from '@element-plus/icons-vue'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { downloadByApi, isAbortError } from '@/utils/download'
import request from '@/utils/request'
import type { ApiResponse, ExcelImportResult, ExcelImportRowError } from '@/types/common'
import type { GenCrudApi } from '@/types/gen/crudApi'
import type { GenViewToolbarAction } from '@/types/tool/genView'
import {
  excelImportHasErrors,
  unwrapExcelImportResult,
  validateExcelImportFile,
} from '@/utils/excelImport'
import { ASYNC_TASK_STATUS, AsyncTaskPollError, pollAsyncTask } from '@/utils/asyncTaskPoll'
import { resolveI18nPayloadMessage } from '@/utils/asyncTaskError'
import { isApiMessageKey } from '@/utils/apiError'

const props = defineProps<{
  modelValue: boolean
  action: GenViewToolbarAction | null
  apiModule: string
  businessName: string
  api: GenCrudApi
  canChooseExecMode?: boolean
  resolvePageText?: (key: string) => string
  labelResolver?: (key: string) => string
  resolveFieldValidation?: (field: string, rule: string) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  success: []
}>()

function resolvePageLabel(key: string) {
  return props.labelResolver?.(key) ?? props.resolvePageText?.(key) ?? key
}

const { t } = useI18n()

function pageOrCommon(pageKey: string, commonKey: string) {
  const page = resolvePageLabel(pageKey)
  if (page && page !== pageKey) return page
  return t(commonKey)
}

const templateHint = computed(() =>
  pageOrCommon('importStepTemplateHint', 'common.importStepTemplateHint'),
)
const uploadHint = computed(() =>
  pageOrCommon('importStepUploadHint', 'common.importStepUploadHint'),
)
const downloading = ref(false)
const submitInFlight = ref(false)
const importExecMode = ref<'sync' | 'async'>('async')
const selectedFile = ref<File | null>(null)
const fileList = ref<UploadUserFile[]>([])
const importErrors = ref<ExcelImportRowError[]>([])
const errorTotal = ref(0)
let pollAbort: AbortController | null = null

const canChooseExecMode = computed(() => Boolean(props.canChooseExecMode))

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

watch(
  () => props.modelValue,
  (open) => {
    if (!open) {
      // Abort poll / sync upload; keep submitInFlight until the in-flight op finishes (anti double-submit).
      pollAbort?.abort()
      pollAbort = null
      selectedFile.value = null
      fileList.value = []
      importErrors.value = []
      errorTotal.value = 0
    } else {
      importExecMode.value = 'async'
    }
  },
)

onUnmounted(() => {
  pollAbort?.abort()
  pollAbort = null
})

function resolveTemplatePath() {
  return (
    props.action?.importTemplatePath ||
    props.api.importTemplatePath ||
    `/${props.apiModule}/${props.businessName}/import-template`
  )
}

function resolveImportPath() {
  return (
    props.action?.importPath ||
    props.api.importPath ||
    `/${props.apiModule}/${props.businessName}/import`
  )
}

function toastFileError(code: ReturnType<typeof validateExcelImportFile>) {
  if (code === 'xlsx_only') ElMessage.error(t('common.importXlsxOnly'))
  else if (code === 'too_large') ElMessage.error(t('common.importFileTooLarge'))
  else if (code === 'empty') ElMessage.error(t('common.importEmpty'))
}

function acceptFile(file: File | undefined | null) {
  const err = validateExcelImportFile(file ?? null)
  if (err) {
    toastFileError(err)
    selectedFile.value = null
    fileList.value = []
    return false
  }
  selectedFile.value = file!
  importErrors.value = []
  errorTotal.value = 0
  return true
}

function resolveTemplateFileName() {
  if (props.action?.fileName) return props.action.fileName
  const fromPage = resolvePageLabel('importTemplateFileName')
  if (fromPage && fromPage !== 'importTemplateFileName') return fromPage
  return `${props.businessName}_template.xlsx`
}

async function downloadTemplate() {
  downloading.value = true
  try {
    await downloadByApi(resolveTemplatePath(), resolveTemplateFileName())
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : t('common.downloadFailed'))
  } finally {
    downloading.value = false
  }
}

function onFileChange(uploadFile: UploadFile) {
  const raw = uploadFile.raw
  if (!acceptFile(raw)) return
  fileList.value = uploadFile.name ? [{ name: uploadFile.name, uid: uploadFile.uid }] : []
}

function onFileRemove() {
  selectedFile.value = null
  fileList.value = []
}

function onExceed(files: File[]) {
  const file = files[0]
  if (!file || !acceptFile(file)) return
  fileList.value = [{ name: file.name, uid: Date.now() }]
}

function isAsyncDisabledError(e: unknown) {
  return isApiMessageKey(e, 'error.async.disabled')
}

async function runSyncImport(
  file: File,
  signal?: AbortSignal,
): Promise<ExcelImportResult | undefined> {
  if (props.api.importFile) {
    return unwrapExcelImportResult(await props.api.importFile(file))
  }
  const formData = new FormData()
  formData.append('file', file)
  const res = await request.post<ApiResponse<ExcelImportResult>>(resolveImportPath(), formData, {
    signal,
  })
  return res.data.data
}

async function submit() {
  if (submitInFlight.value || !acceptFile(selectedFile.value)) return
  pollAbort?.abort()
  pollAbort = new AbortController()
  const signal = pollAbort.signal
  submitInFlight.value = true
  try {
    let result: ExcelImportResult | undefined | null
    const file = selectedFile.value!
    const preferAsync =
      Boolean(props.api.importFileAsync) &&
      (!canChooseExecMode.value || importExecMode.value === 'async')
    if (preferAsync) {
      try {
        ElMessage.info(t('common.importAsyncSubmitted'))
        const submitted = await props.api.importFileAsync!(file)
        const taskId = submitted?.taskId
        if (!taskId) {
          ElMessage.error(t('common.importInvalidResult'))
          return
        }
        try {
          const task = await pollAsyncTask(taskId, { signal })
          if (signal.aborted) {
            return
          }
          if (task.taskStatus === ASYNC_TASK_STATUS.FAILED) {
            ElMessage.error(
              resolveI18nPayloadMessage(t, task.errorMessage, {
                fallbackKey: 'common.importAsyncFailed',
                resolveGenValidation,
              }),
            )
            return
          }
          if (task.taskStatus === ASYNC_TASK_STATUS.CANCELLED) {
            ElMessage.error(t('common.importAsyncCancelled'))
            return
          }
          result = parseAsyncImportResult(task.resultData)
          if (result === null) {
            return
          }
        } catch (e) {
          if (e instanceof AsyncTaskPollError) {
            if (e.code === 'timeout') {
              ElMessage.error(t('common.importAsyncTimeout'))
            } else if (e.code === 'pending_timeout') {
              ElMessage.warning(t('common.importAsyncStillQueued'))
            }
            return
          }
          ElMessage.error(t('common.importAsyncFailed'))
          return
        }
      } catch (e) {
        if (isAsyncDisabledError(e) && (props.api.importFile || props.api.importPath)) {
          if (canChooseExecMode.value && importExecMode.value === 'async') {
            ElMessage.warning(t('error.async.disabled'))
            return
          }
          ElMessage.warning(t('common.importAsyncFallbackSync'))
          result = await runSyncImport(file, signal)
        } else {
          // axios interceptor already toasted (except async.disabled)
          return
        }
      }
    } else {
      result = await runSyncImport(file, signal)
    }
    if (signal.aborted) {
      return
    }
    if (!result) {
      ElMessage.error(t('common.importInvalidResult'))
      return
    }
    if (excelImportHasErrors(result)) {
      importErrors.value = result.errors ?? []
      errorTotal.value = result.errorTotal ?? result.errors?.length ?? 0
      const first = importErrors.value[0]
      ElMessage.error(
        first
          ? t('common.importFailedDetail', {
              row: first.rowNumber,
              message: resolveRowError(first.message),
              count: errorTotal.value,
            })
          : t('common.importFailedCount', { count: errorTotal.value }),
      )
      return
    }
    const success = result.success ?? 0
    const skipped = result.skipped ?? 0
    if (success <= 0) {
      ElMessage.warning(t('common.importSuccessEmpty'))
      return
    }
    if (skipped > 0) {
      ElMessage.success(t('common.importSuccessWithSkipped', { count: success, skipped }))
    } else {
      ElMessage.success(t('common.importSuccessCount', { count: success }))
    }
    visible.value = false
    emit('success')
  } catch (e) {
    if (isAbortError(e) || pollAbort?.signal.aborted) {
      return
    }
    // axios interceptor toasts business errors (sync path / submit)
  } finally {
    if (pollAbort?.signal === signal) {
      pollAbort = null
    }
    submitInFlight.value = false
  }
}

function resolveGenValidation(business: string, field: string, rule: string) {
  if (business !== props.businessName || !props.resolveFieldValidation) return undefined
  const text = props.resolveFieldValidation(field, rule)
  return text || undefined
}

function resolveRowError(message?: string) {
  return resolveI18nPayloadMessage(t, message, { resolveGenValidation })
}

function parseAsyncImportResult(raw?: string | null): ExcelImportResult | undefined | null {
  if (!raw?.trim()) return undefined
  try {
    return unwrapExcelImportResult(JSON.parse(raw))
  } catch {
    ElMessage.error(t('common.importInvalidResult'))
    return null
  }
}
</script>

<style scoped>
.gen-excel-import {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.gen-excel-import__step {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
}

.gen-excel-import__step-head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.gen-excel-import__step-index {
  flex: 0 0 auto;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--el-color-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  line-height: 24px;
  text-align: center;
}

.gen-excel-import__step-copy {
  min-width: 0;
}

.gen-excel-import__step-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 24px;
}

.gen-excel-import__step-desc {
  margin-top: 2px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}

.gen-excel-import__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.gen-excel-import__upload {
  width: 100%;
}

.gen-excel-import__upload :deep(.el-upload) {
  width: 100%;
}

.gen-excel-import__upload :deep(.el-upload-dragger) {
  width: 100%;
  padding: 28px 16px;
}

.gen-excel-import__upload-icon {
  font-size: 40px;
  color: var(--el-color-primary);
  margin-bottom: 8px;
}

.gen-excel-import__upload :deep(.el-upload-list) {
  margin-top: 8px;
}

.gen-excel-import__errors {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.gen-excel-import__errors-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-danger);
}

.gen-excel-import__errors-more {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
