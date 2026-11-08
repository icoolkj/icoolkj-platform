<template>
  <AttachmentManager
    :model-value="attachmentModel"
    :preview-url="String(ctx.formModel?.coverPreviewUrl ?? '')"
    :disabled="ctx.disabled"
    :gp="ctx.gp"
    :biz-type="bizType"
    :download-scope="downloadScope"
    :mode="mode"
    :variant="variant"
    @update:model-value="emit('update:modelValue', $event)"
    @update:preview-url="updatePreview"
  />
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue'
import { normalizeAttachmentIds } from '@/composables/useAttachmentUpload'
import {
  FILE_DOWNLOAD_SCOPE_INFRA,
  FILE_DOWNLOAD_SCOPE_NOTICE,
  type FileDownloadScope,
} from '@/constants/file'
import type { GenFieldWidgetContext, GenFieldWidgetEmits } from '@/components/gen/registry/field-widgets/types'

const AttachmentManager = defineAsyncComponent(
  () => import('@/components/attachment/AttachmentManager.vue'),
)

const props = defineProps<{
  ctx: GenFieldWidgetContext
  mode: 'single' | 'multiple'
  variant: 'image' | 'list'
}>()

const emit = defineEmits<GenFieldWidgetEmits>()

function requireAttachmentBizType(): string {
  const code = props.ctx.formConfig.bizType?.trim()
  if (!code) {
    throw new Error(
      `[GenFormField] field "${props.ctx.formConfig.field}" (${props.ctx.formConfig.component}) requires config.bizType`,
    )
  }
  return code
}

const bizType = computed(() => requireAttachmentBizType())

const downloadScope = computed<FileDownloadScope>(() =>
  props.ctx.formConfig.downloadScope === 'notice'
    ? FILE_DOWNLOAD_SCOPE_NOTICE
    : FILE_DOWNLOAD_SCOPE_INFRA,
)

const attachmentModel = computed(() =>
  props.mode === 'multiple'
    ? normalizeAttachmentIds(props.ctx.modelValue)
    : ((props.ctx.modelValue as string) ?? ''),
)

function updatePreview(value: string) {
  if (!props.ctx.formModel) return
  // eslint-disable-next-line vue/no-mutating-props -- shared form bag
  props.ctx.formModel.coverPreviewUrl = value
}
</script>
