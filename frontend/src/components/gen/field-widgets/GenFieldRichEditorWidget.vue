<template>
  <RichTextEditor
    :model-value="(ctx.modelValue as string) ?? ''"
    :placeholder="ctx.placeholder"
    :disabled="ctx.disabled"
    :biz-type="richEditorBizType"
    :download-scope="richEditorDownloadScope"
    @update:model-value="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue'
import {
  FILE_DOWNLOAD_SCOPE_INFRA,
  FILE_DOWNLOAD_SCOPE_NOTICE,
  type FileDownloadScope,
} from '@/constants/file'
import type { GenFieldWidgetContext, GenFieldWidgetEmits } from '@/components/gen/registry/field-widgets/types'

const RichTextEditor = defineAsyncComponent(() => import('@/components/RichTextEditor.vue'))

const props = defineProps<{ ctx: GenFieldWidgetContext }>()
const emit = defineEmits<GenFieldWidgetEmits>()

const richEditorBizType = computed(() => props.ctx.formConfig.bizType?.trim() || undefined)

const richEditorDownloadScope = computed<FileDownloadScope>(() => {
  if (props.ctx.formConfig.downloadScope === 'notice') return FILE_DOWNLOAD_SCOPE_NOTICE
  if (props.ctx.formConfig.downloadScope === 'infra') return FILE_DOWNLOAD_SCOPE_INFRA
  const biz = props.ctx.formConfig.bizType?.trim() || ''
  if (!biz || biz.startsWith('notice_')) return FILE_DOWNLOAD_SCOPE_NOTICE
  return FILE_DOWNLOAD_SCOPE_INFRA
})
</script>
