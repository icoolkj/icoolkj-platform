<!--
  统一附件管理组件。业务页优先使用本组件；仅头像裁剪/富文本内嵌/管理端存储对话框等特殊场景手写。
  v-model：single → string(fileId)；multiple → string[]
-->
<template>
  <div class="attachment-manager" :class="[`is-${variant}`, { 'is-disabled': disabled }]">
    <!-- 封面/单图 -->
    <template v-if="isSingle && variant === ATTACHMENT_VARIANT_IMAGE">
      <div v-if="singleId" class="attachment-manager__preview">
        <el-image v-if="displayUrl" :src="displayUrl" fit="cover" class="attachment-manager__image">
          <template #error>
            <div class="attachment-manager__error">{{ t('common.downloadFailed') }}</div>
          </template>
        </el-image>
        <div v-else class="attachment-manager__error">{{ t('common.downloadFailed') }}</div>
        <div v-if="!disabled" class="attachment-manager__actions">
          <el-button size="small" :loading="uploading" @click="triggerSelect">
            {{ uploadLabel }}
          </el-button>
          <el-button size="small" type="danger" plain @click="clearSingle">
            {{ t('common.delete') }}
          </el-button>
        </div>
      </div>
      <div v-else-if="!disabled" class="attachment-manager__empty" @click="triggerSelect">
        <el-icon><Plus /></el-icon>
        <span>{{ uploadLabel }}</span>
      </div>
      <div v-else class="attachment-manager__empty-text">—</div>
    </template>

    <!-- 文件列表（单/多） -->
    <template v-else>
      <div v-if="fileItems.length" class="attachment-manager__list">
        <div v-for="item in fileItems" :key="item.id" class="attachment-manager__item">
          <span class="attachment-manager__name" :title="item.name">{{ item.name }}</span>
          <div class="attachment-manager__item-actions">
            <el-button link type="primary" @click="downloadFile(item)">
              {{ listLabel('download') }}
            </el-button>
            <el-button v-if="!disabled" link type="danger" @click="removeId(item.id)">
              {{ listLabel('delete') }}
            </el-button>
          </div>
        </div>
      </div>
      <div v-if="!disabled" class="attachment-manager__trigger">
        <el-button :loading="uploading" :disabled="reachedLimit" @click="triggerSelect">
          {{ uploadLabel }}
        </el-button>
        <span class="attachment-manager__hint">{{ statusHint }}</span>
      </div>
      <div v-else-if="!fileItems.length" class="attachment-manager__empty-text">—</div>
    </template>

    <div v-if="hintText && (variant === ATTACHMENT_VARIANT_IMAGE || !fileItems.length)" class="attachment-manager__hint">
      {{ hintText }}
    </div>

    <input
      ref="fileInputRef"
      type="file"
      class="attachment-manager__input"
      :accept="acceptAttr"
      :multiple="isMultiple"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAuthImageUrl } from '@/composables/useAuthImageUrl'
import {
  resolveAttachmentConstraints,
  resolveAttachmentHint,
  normalizeAttachmentIds,
  uploadAttachmentFile,
} from '@/composables/useAttachmentUpload'
import {
  ATTACHMENT_MODE_MULTIPLE,
  ATTACHMENT_MODE_SINGLE,
  ATTACHMENT_VARIANT_IMAGE,
  FILE_DOWNLOAD_SCOPE_INFRA,
  FILE_DOWNLOAD_SCOPE_NOTICE,
  FILE_SENSITIVITY_INTERNAL,
  type AttachmentMode,
  type AttachmentVariant,
  type FileDownloadScope,
} from '@/constants/file'
import { discardUnreferencedInfraFile, getInfraFile } from '@/api/infra/file'
import type { FileProfileVO } from '@/types/infra/fileProfile'
import type { FileUploadConstraints } from '@/utils/fileUploadConstraints'
import { acceptFromProfile } from '@/utils/fileUploadConstraints'
import { canonicalInfraDownloadUrl } from '@/utils/infraFileUrl'
import { downloadByApi } from '@/utils/download'

const props = withDefaults(
  defineProps<{
    /** single: string；multiple: string[] */
    modelValue?: string | string[] | null
    previewUrl?: string
    disabled?: boolean
    mode?: AttachmentMode
    variant?: AttachmentVariant
    /** 必填：上传 bizType（profileCode 或 @/constants/file 常量），禁止依赖静默默认 */
    bizType: string
    /** 业务主键；有值时上传写 ref */
    bizId?: string
    refField?: string
    downloadScope?: FileDownloadScope
    accept?: string
    hint?: string
    profile?: FileProfileVO | null
    constraints?: FileUploadConstraints | null
    /** 覆盖 profile.maxNum */
    maxFiles?: number
    sensitivityLevel?: string
    /** 清空单文件时是否 discard 无引用文件 */
    discardOnClear?: boolean
    /** 无槽位规则时单图是否强制图片 */
    imageOnlyFallback?: boolean
    gp?: (key: string, params?: Record<string, string | number>) => string
  }>(),
  {
    mode: ATTACHMENT_MODE_SINGLE,
    variant: ATTACHMENT_VARIANT_IMAGE,
    downloadScope: FILE_DOWNLOAD_SCOPE_INFRA,
    discardOnClear: true,
    imageOnlyFallback: undefined,
    sensitivityLevel: FILE_SENSITIVITY_INTERNAL,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string | string[]]
  'update:previewUrl': [value: string]
  uploaded: [ids: string[]]
}>()

const { t } = useI18n()
const fileInputRef = ref<HTMLInputElement>()
const uploading = ref(false)
const resolvedPreviewUrl = ref('')
const fileItems = ref<{ id: string; name: string }[]>([])

const isSingle = computed(() => props.mode === ATTACHMENT_MODE_SINGLE)
const isMultiple = computed(() => props.mode === ATTACHMENT_MODE_MULTIPLE)

const gp = computed(() => props.gp ?? ((key: string) => key))

const uploadBizType = computed(() => {
  const code = props.bizType?.trim()
  if (!code) {
    throw new Error('[AttachmentManager] bizType is required')
  }
  return code
})

const resolvedConstraints = computed(() =>
  resolveAttachmentConstraints({ constraints: props.constraints, profile: props.profile }),
)

const acceptAttr = computed(() => {
  if (props.accept?.trim()) return props.accept.trim()
  const fromProfile = acceptFromProfile(props.profile)
  if (fromProfile) return fromProfile
  return resolvedConstraints.value.accept || ''
})

const hintText = computed(() =>
  resolveAttachmentHint({
    hint: props.hint,
    profile: props.profile,
    t: (key, params) =>
      t(key.startsWith('common.') ? key : `common.${key}`, params as Record<string, unknown>),
  }),
)

const maxCount = computed(() => {
  if (props.maxFiles != null && props.maxFiles > 0) return props.maxFiles
  const fromProfile = resolvedConstraints.value.maxNum
  if (fromProfile != null && fromProfile > 0) return fromProfile
  return isMultiple.value ? 5 : 1
})

const ids = computed(() => normalizeAttachmentIds(props.modelValue))
const singleId = computed(() => (isSingle.value ? ids.value[0] || '' : ''))
const reachedLimit = computed(() => ids.value.length >= maxCount.value)

const uploadLabel = computed(() => {
  const viaUpload = gp.value('upload')
  if (viaUpload && viaUpload !== 'upload') return viaUpload
  return t('common.upload')
})

const statusHint = computed(() => {
  if (reachedLimit.value) {
    const via = gp.value('limitReached', { n: maxCount.value })
    if (via && via !== 'limitReached') return via
    return t('common.fileUploadMaxNum', { n: maxCount.value })
  }
  const via = gp.value('maxFiles', { n: maxCount.value })
  if (via && via !== 'maxFiles') return via
  return hintText.value || t('common.fileUploadMaxNum', { n: maxCount.value })
})

const imageOnlyFallback = computed(() => {
  if (props.imageOnlyFallback != null) return props.imageOnlyFallback
  return props.variant === ATTACHMENT_VARIANT_IMAGE && isSingle.value
})

const displayUrl = useAuthImageUrl(
  resolvedPreviewUrl,
  props.downloadScope === FILE_DOWNLOAD_SCOPE_INFRA
    ? FILE_DOWNLOAD_SCOPE_INFRA
    : FILE_DOWNLOAD_SCOPE_NOTICE,
)

watch(
  () => props.modelValue,
  () => {
    if (isSingle.value) {
      const fileId = singleId.value
      resolvedPreviewUrl.value = fileId ? canonicalInfraDownloadUrl(fileId) : ''
    } else {
      void syncFileMeta(ids.value)
    }
  },
  { immediate: true },
)

watch(
  () => props.previewUrl,
  (url) => {
    if (url?.trim() && !singleId.value) {
      resolvedPreviewUrl.value = url.trim()
    }
  },
)

function listLabel(key: string): string {
  const via = gp.value(key)
  if (via && via !== key) return via
  if (key === 'download') return t('common.download')
  if (key === 'delete') return t('common.delete')
  return key
}

function emitValue(next: string[]) {
  if (isSingle.value) {
    emit('update:modelValue', next[0] || '')
  } else {
    emit('update:modelValue', next)
  }
}

async function syncFileMeta(nextIds: string[]) {
  const prev = new Map(fileItems.value.map((item) => [item.id, item.name]))
  const items: { id: string; name: string }[] = []
  for (const id of nextIds) {
    const cached = prev.get(id)
    if (cached) {
      items.push({ id, name: cached })
      continue
    }
    try {
      const res = await getInfraFile(id)
      const data = res.data.data
      items.push({ id, name: data.originalName || data.fileName || id })
    } catch {
      items.push({ id, name: id })
    }
  }
  fileItems.value = items
}

function triggerSelect() {
  if (props.disabled || uploading.value || reachedLimit.value) return
  fileInputRef.value?.click()
}

function clearSingle() {
  const previousFileId = singleId.value
  emit('update:modelValue', '')
  emit('update:previewUrl', '')
  resolvedPreviewUrl.value = ''
  if (fileInputRef.value) fileInputRef.value.value = ''
  if (props.discardOnClear && previousFileId) {
    void discardUnreferencedInfraFile(previousFileId).catch(() => {
      /* ignore */
    })
  }
}

function removeId(id: string) {
  emitValue(ids.value.filter((item) => item !== id))
}

async function downloadFile(item: { id: string; name: string }) {
  try {
    await downloadByApi(`/infra/file/${item.id}/download`, item.name)
  } catch (err) {
    ElMessage.error(err instanceof Error ? err.message : t('common.downloadFailed'))
  }
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  input.value = ''
  if (!files.length) return

  const remain = maxCount.value - ids.value.length
  if (remain <= 0) {
    ElMessage.warning(statusHint.value)
    return
  }
  const selected = isSingle.value ? files.slice(0, 1) : files.slice(0, remain)
  uploading.value = true
  const uploaded: string[] = []
  try {
    for (const file of selected) {
      try {
        const data = await uploadAttachmentFile({
          file,
          bizType: uploadBizType.value,
          bizId: props.bizId,
          refField: props.refField,
          replaceFileId: isSingle.value ? singleId.value || undefined : undefined,
          sensitivityLevel: props.sensitivityLevel,
          constraints: resolvedConstraints.value,
          profile: props.profile,
          imageOnlyFallback: imageOnlyFallback.value,
          t,
        })
        if (data.id) uploaded.push(data.id)
        if (isSingle.value) {
          emit('update:previewUrl', data.fileUrl ?? '')
          resolvedPreviewUrl.value = data.id ? canonicalInfraDownloadUrl(data.id) : ''
        }
      } catch (err) {
        if (err instanceof Error && err.message === 'VALIDATION') continue
        ElMessage.error(err instanceof Error ? err.message : t('common.upload'))
      }
    }
    if (uploaded.length) {
      if (isSingle.value) {
        emitValue(uploaded.slice(0, 1))
      } else {
        emitValue([...ids.value, ...uploaded])
      }
      emit('uploaded', uploaded)
    }
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped lang="scss">
.attachment-manager {
  width: 100%;

  &__preview {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__image {
    width: 160px;
    height: 90px;
    border-radius: 4px;
    border: 1px solid var(--el-border-color-lighter);
  }

  &__error {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 160px;
    height: 90px;
    border-radius: 4px;
    border: 1px solid var(--el-border-color-lighter);
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }

  &__actions,
  &__item-actions {
    display: flex;
    gap: 8px;
  }

  &__empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 160px;
    height: 90px;
    border: 1px dashed var(--el-border-color);
    border-radius: 4px;
    color: var(--el-text-color-secondary);
    cursor: pointer;
    gap: 4px;

    &:hover {
      border-color: var(--el-color-primary);
      color: var(--el-color-primary);
    }
  }

  &__list {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-bottom: 8px;
  }

  &__item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 6px 10px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 4px;
    background: var(--el-fill-color-blank);
  }

  &__name {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
  }

  &__trigger {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__hint,
  &__empty-text {
    margin-top: 6px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  &__trigger &__hint {
    margin-top: 0;
  }

  &__input {
    display: none;
  }

  &.is-disabled .attachment-manager__empty {
    cursor: not-allowed;
    opacity: 0.6;
  }
}
</style>
