<!-- 兼容封装：OA 多文件 → AttachmentManager（新代码请直接用 AttachmentManager） -->
<template>
  <AttachmentManager
    :model-value="normalizeModel(modelValue)"
    :disabled="disabled"
    :biz-type="bizType"
    :max-files="maxFiles"
    :gp="gp"
    mode="multiple"
    variant="list"
    :download-scope="FILE_DOWNLOAD_SCOPE_INFRA"
    @update:model-value="emitIds($event)"
  />
</template>

<script setup lang="ts">
import { AttachmentManager } from '@/components/attachment'
import { normalizeAttachmentIds } from '@/composables/useAttachmentUpload'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { FILE_BIZ_OA_INSTANCE, FILE_DOWNLOAD_SCOPE_INFRA } from '@/constants/file'
const MAX_FILES = 5

const { gp } = useGenPageShellI18n('oaWidget')

withDefaults(
  defineProps<{
    modelValue?: unknown
    disabled?: boolean
    bizType?: string
    maxFiles?: number
  }>(),
  {
    disabled: false,
    bizType: FILE_BIZ_OA_INSTANCE,
    maxFiles: MAX_FILES,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
}>()

function normalizeModel(raw: unknown): string[] {
  return normalizeAttachmentIds(raw)
}

function emitIds(value: string | string[]) {
  emit('update:modelValue', Array.isArray(value) ? value : value ? [value] : [])
}
</script>
