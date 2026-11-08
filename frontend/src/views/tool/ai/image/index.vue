<template>
  <div class="gen-page-shell">
    <GenListLayout
      :title="gp('menuImage')"
      :description="gp('imageDescription')"
      :show-search="false"
      :show-right-toolbar="false"
    >
      <el-alert v-if="!aiReady" type="warning" :closable="false" show-icon class="form-block-tip">
        {{ gp('imageNotReady') }}
      </el-alert>
      <AiRuntimeBanner :status="status" :ai-ready="aiReady" />

      <el-form label-width="108px" class="image-form" @submit.prevent>
        <el-form-item :label="gp('imagePrompt')" required>
          <el-input
            v-model="form.prompt"
            type="textarea"
            :rows="4"
            maxlength="2000"
            show-word-limit
            :disabled="!aiReady || generating"
            :placeholder="gp('imagePromptPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="gp('imageNegativePrompt')">
          <el-input
            v-model="form.negativePrompt"
            maxlength="500"
            show-word-limit
            :disabled="!aiReady || generating"
            :placeholder="gp('imageNegativePromptPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="gp('imageSize')">
          <el-select v-model="form.size" :disabled="!aiReady || generating" style="width: 280px">
            <el-option
              v-for="opt in sizeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="gp('imageCount')">
          <el-input-number
            v-model="form.n"
            :min="1"
            :max="6"
            :disabled="!aiReady || generating"
          />
          <span class="field-hint">{{ gp('imageCountHint') }}</span>
        </el-form-item>
        <el-form-item :label="gp('imageResponseFormat')">
          <el-radio-group v-model="form.responseFormat" :disabled="!aiReady || generating">
            <el-radio-button value="url">url</el-radio-button>
            <el-radio-button value="b64_json">b64_json</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="gp('imagePromptExtend')">
          <el-switch v-model="form.promptExtend" :disabled="!aiReady || generating" />
        </el-form-item>
        <el-form-item :label="gp('imageWatermark')">
          <el-switch v-model="form.watermark" :disabled="!aiReady || generating" />
        </el-form-item>
        <el-form-item :label="gp('imageExecMode')">
          <div class="exec-actions">
            <el-button
              type="primary"
              v-hasPermi="[PERM.image]"
              :loading="generating && activeMode === 'sync'"
              :disabled="!aiReady || !form.prompt.trim() || generating"
              @click="runGenerate('sync')"
            >
              {{ gp('generateImage') }}
            </el-button>
            <el-button
              v-hasPermi="[PERM.image]"
              :loading="generating && activeMode === 'async'"
              :disabled="!aiReady || !form.prompt.trim() || generating"
              @click="runGenerate('async')"
            >
              {{ gp('generateImageAsync') }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <div v-if="result" class="result-panel">
        <div class="result-meta">
          <span v-if="result.providerName">{{ result.providerName }}</span>
          <span v-if="result.model" class="muted">{{ result.model }}</span>
          <span v-if="result.size" class="muted">{{ result.size }}</span>
          <span v-if="result.n" class="muted">×{{ result.n }}</span>
        </div>
        <p class="result-prompt">{{ result.prompt }}</p>
        <div class="result-grid">
          <AuthImage
            v-for="(item, idx) in displayItems"
            :key="item.fileId || idx"
            :src="item.url"
            fit="contain"
            image-class="result-image"
            previewable
            :show-placeholder="true"
            :placeholder="gp('imagePreviewEmpty')"
          />
        </div>
      </div>
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import AuthImage from '@/components/AuthImage.vue'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import AiRuntimeBanner from '../components/AiRuntimeBanner.vue'
import { useToolAiI18n } from '@/composables/useToolAiI18n'
import { useAiReady } from '@/composables/useAiReady'
import { PERM_AI as PERM } from '@/api/tool/ai/catalog'
import {
  generateAiImage,
  generateAiImageAsync,
  parseAiImageAsyncResult,
  type AiImageGeneratePayload,
  type AiImageGenerateResult,
  type AiImageItem,
} from '@/api/tool/aiImage'
import { isApiMessageKey } from '@/utils/apiError'
import { resolveI18nPayloadMessage } from '@/utils/asyncTaskError'
import { ASYNC_TASK_STATUS, AsyncTaskPollError, pollAsyncTask } from '@/utils/asyncTaskPoll'
import { isAbortError } from '@/utils/download'

const { gp } = useToolAiI18n()
const { t } = useI18n()
const { aiReady, status } = useAiReady('image')

const sizeOptions = [
  { value: '1024x1024', label: '1024 × 1024 (1:1)' },
  { value: '1328x1328', label: '1328 × 1328 (1:1)' },
  { value: '1664x928', label: '1664 × 928 (16:9)' },
  { value: '1472x1140', label: '1472 × 1140 (4:3)' },
  { value: '1140x1472', label: '1140 × 1472 (3:4)' },
  { value: '928x1664', label: '928 × 1664 (9:16)' },
]

const form = reactive({
  prompt: '',
  negativePrompt: '',
  size: '1024x1024',
  n: 1,
  responseFormat: 'url' as 'url' | 'b64_json',
  promptExtend: true,
  watermark: false,
})

const generating = ref(false)
const activeMode = ref<'sync' | 'async' | null>(null)
const result = ref<AiImageGenerateResult | null>(null)
let pollAbort: AbortController | null = null

onUnmounted(() => {
  pollAbort?.abort()
})

const displayItems = computed<AiImageItem[]>(() => {
  const data = result.value
  if (!data) return []
  if (data.items?.length) return data.items
  if (data.url) return [{ fileId: data.fileId, url: data.url }]
  return []
})

function buildPayload(text: string): AiImageGeneratePayload {
  return {
    prompt: text,
    size: form.size,
    n: form.n,
    negativePrompt: form.negativePrompt.trim() || undefined,
    responseFormat: form.responseFormat,
    promptExtend: form.promptExtend,
    watermark: form.watermark,
  }
}

async function runSyncGenerate(payload: AiImageGeneratePayload) {
  const res = await generateAiImage(payload)
  result.value = res.data.data ?? null
  ElMessage.success(gp('imageGenerateSuccess'))
}

async function runAsyncGenerate(payload: AiImageGeneratePayload, signal: AbortSignal) {
  const submitted = await generateAiImageAsync(payload)
  if (signal.aborted) return
  const taskId = submitted.data.data?.taskId
  if (!taskId) {
    ElMessage.error(gp('imageAsyncFailed'))
    return
  }
  ElMessage.info(gp('imageAsyncSubmitted'))
  const task = await pollAsyncTask(taskId, {
    signal,
    timeoutMs: 10 * 60 * 1000,
  })
  if (signal.aborted) return
  if (task.taskStatus === ASYNC_TASK_STATUS.FAILED) {
    ElMessage.error(
      resolveI18nPayloadMessage(t, task.errorMessage) || gp('imageAsyncFailed'),
    )
    return
  }
  if (task.taskStatus === ASYNC_TASK_STATUS.CANCELLED) {
    ElMessage.error(gp('imageAsyncCancelled'))
    return
  }
  const parsed = parseAiImageAsyncResult(task.resultData)
  if (!parsed) {
    ElMessage.error(gp('imageAsyncFailed'))
    return
  }
  result.value = parsed
  ElMessage.success(gp('imageGenerateSuccess'))
}

async function runGenerate(mode: 'sync' | 'async') {
  const text = form.prompt.trim()
  if (!text) {
    ElMessage.warning(gp('imagePromptRequired'))
    return
  }
  if (!aiReady.value) {
    ElMessage.warning(gp('imageNotReady'))
    return
  }
  pollAbort?.abort()
  pollAbort = new AbortController()
  const signal = pollAbort.signal
  const payload = buildPayload(text)
  generating.value = true
  activeMode.value = mode
  try {
    if (mode === 'sync') {
      await runSyncGenerate(payload)
      return
    }
    try {
      await runAsyncGenerate(payload, signal)
    } catch (e) {
      if (signal.aborted || isAbortError(e)) return
      if (isApiMessageKey(e, 'error.async.disabled')) {
        ElMessage.warning(t('error.async.disabled'))
        return
      }
      if (e instanceof AsyncTaskPollError) {
        if (e.code === 'timeout') ElMessage.error(gp('imageAsyncTimeout'))
        else if (e.code === 'pending_timeout') ElMessage.warning(gp('imageAsyncStillQueued'))
        return
      }
      throw e
    }
  } finally {
    generating.value = false
    activeMode.value = null
  }
}
</script>

<script lang="ts">
export default { name: 'ToolAiImage' }
</script>

<style scoped lang="scss">
@use '@/styles/tool-ai.scss';

.image-form {
  max-width: 760px;
  margin-bottom: 24px;
}

.field-hint {
  margin-left: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.exec-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.result-panel {
  max-width: 960px;
}

.result-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 13px;
}

.muted {
  color: var(--el-text-color-secondary);
}

.result-prompt {
  margin: 0 0 12px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

:deep(.result-image) {
  width: 100%;
  max-height: 420px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
}
</style>
