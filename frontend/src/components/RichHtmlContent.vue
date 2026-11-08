<template>
  <div ref="rootRef" class="rich-html-content" v-html="displayHtml" />
</template>

<script setup lang="ts">
import { onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRichHtmlSession } from '@/composables/useRichHtmlSession'
import { stopHtmlMediaPlayback } from '@/utils/mediaPlayback'
import {
  FILE_DOWNLOAD_SCOPE_NOTICE,
  type FileDownloadScope,
} from '@/utils/infraFileUrl'

const props = withDefaults(
  defineProps<{
    html?: string
    downloadScope?: FileDownloadScope
    /** 为 false 时立即停止媒体播放（如弹窗关闭） */
    active?: boolean
  }>(),
  {
    downloadScope: FILE_DOWNLOAD_SCOPE_NOTICE,
    active: true,
  },
)

const sessionOptions = reactive<{ downloadScope: FileDownloadScope }>({
  downloadScope: props.downloadScope ?? FILE_DOWNLOAD_SCOPE_NOTICE,
})
watch(
  () => props.downloadScope,
  (scope) => {
    sessionOptions.downloadScope = scope ?? FILE_DOWNLOAD_SCOPE_NOTICE
  },
)
const richHtml = useRichHtmlSession(sessionOptions)
const displayHtml = ref('')
const rootRef = ref<HTMLElement | null>(null)
let resolveVersion = 0

function stopPlayback() {
  stopHtmlMediaPlayback(rootRef.value)
}

watch(
  () => props.active,
  (active) => {
    if (!active) stopPlayback()
  },
)

watch(
  () => [props.html, props.downloadScope] as const,
  async ([html]) => {
    stopPlayback()
    const version = ++resolveVersion
    richHtml.revokeAll()
    displayHtml.value = html ? await richHtml.resolveForDisplay(html) : ''
    if (version !== resolveVersion) {
      richHtml.revokeAll()
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  stopPlayback()
  richHtml.revokeAll()
})

defineExpose({ stopPlayback })
</script>

<style scoped lang="scss">
.rich-html-content {
  line-height: 1.7;
  word-break: break-word;

  :deep(img),
  :deep(video) {
    max-width: 100%;
  }
}
</style>
