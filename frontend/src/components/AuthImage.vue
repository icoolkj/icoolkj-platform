<template>
  <el-image
    v-if="displayUrl"
    :src="displayUrl"
    :fit="fit"
    :class="imageClass"
    :preview-src-list="previewable ? [displayUrl] : undefined"
  />
  <span v-else-if="showPlaceholder" class="text-muted">{{ placeholder }}</span>
</template>

<script setup lang="ts">
import { toRef } from 'vue'
import { useAuthImageUrl } from '@/composables/useAuthImageUrl'
import type { FileDownloadScope } from '@/utils/infraFileUrl'

const props = withDefaults(
  defineProps<{
    src?: string
    fit?: 'fill' | 'contain' | 'cover' | 'none' | 'scale-down'
    imageClass?: string
    placeholder?: string
    showPlaceholder?: boolean
    previewable?: boolean
    downloadScope?: FileDownloadScope
  }>(),
  {
    fit: 'cover',
    placeholder: '-',
    showPlaceholder: false,
    previewable: false,
    downloadScope: 'infra',
  },
)

const displayUrl = useAuthImageUrl(toRef(props, 'src'), props.downloadScope)
</script>
