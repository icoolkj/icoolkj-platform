<!-- 兼容封装：单图封面 → AttachmentManager（新代码请直接用 AttachmentManager） -->
<template>
  <AttachmentManager
    :model-value="modelValue ?? ''"
    :preview-url="previewUrl"
    :disabled="disabled"
    :gp="gp"
    :biz-type="bizType"
    :download-scope="downloadScope"
    :accept="accept"
    :hint="hint"
    :profile="profile"
    :constraints="constraints"
    mode="single"
    variant="image"
    @update:model-value="emit('update:modelValue', String($event ?? ''))"
    @update:preview-url="emit('update:previewUrl', $event)"
  />
</template>

<script setup lang="ts">
import { AttachmentManager } from '@/components/attachment'
import {
  FILE_BIZ_NOTICE_COVER,
  FILE_DOWNLOAD_SCOPE_NOTICE,
  type FileDownloadScope,
} from '@/constants/file'
import type { FileProfileVO } from '@/types/infra/fileProfile'
import type { FileUploadConstraints } from '@/utils/fileUploadConstraints'

withDefaults(
  defineProps<{
    modelValue?: string
    previewUrl?: string
    disabled?: boolean
    gp?: (key: string, params?: Record<string, string | number>) => string
    bizType?: string
    downloadScope?: FileDownloadScope
    accept?: string
    hint?: string
    profile?: FileProfileVO | null
    constraints?: FileUploadConstraints | null
  }>(),
  {
    downloadScope: FILE_DOWNLOAD_SCOPE_NOTICE,
    bizType: FILE_BIZ_NOTICE_COVER,
  },
)

const emit = defineEmits<{
  'update:modelValue': [string]
  'update:previewUrl': [string]
}>()
</script>
