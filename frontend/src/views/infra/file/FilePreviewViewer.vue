<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :fullscreen="fullscreen"
    width="92%"
    top="3vh"
    destroy-on-close
    class="file-preview-dialog"
    @closed="handleClosed"
  >
    <div class="preview-toolbar">
      <template v-if="kind === 'image'">
        <el-button-group>
          <el-button :icon="ZoomOut" :disabled="imageScale <= 0.5" @click="zoomOut" />
          <el-button disabled class="zoom-label">{{ Math.round(imageScale * 100) }}%</el-button>
          <el-button :icon="ZoomIn" :disabled="imageScale >= 3" @click="zoomIn" />
          <el-button @click="resetZoom">{{ gp('previewFit') }}</el-button>
        </el-button-group>
      </template>
      <div class="toolbar-spacer" />
      <el-button :icon="FullScreen" @click="toggleFullscreen">
        {{ fullscreen ? gp('previewExitFullscreen') : gp('previewFullscreen') }}
      </el-button>
      <el-button :icon="TopRight" @click="openNewTab">{{ gp('previewNewTab') }}</el-button>
      <el-button type="primary" :icon="Download" @click="emit('download')">
        {{ gp('download') }}
      </el-button>
    </div>

    <div v-loading="loading" class="preview-viewport">
      <div v-if="kind === 'image' && objectUrl" class="image-stage">
        <img
          :src="objectUrl"
          class="preview-image"
          :style="{ transform: `scale(${imageScale})` }"
          alt=""
        />
      </div>
      <div v-else-if="kind === 'video' && objectUrl" ref="videoStageRef" class="video-stage">
        <video :src="objectUrl" controls class="preview-video" />
      </div>
      <iframe
        v-else-if="kind === 'pdf' && pdfSrc"
        :src="pdfSrc"
        class="preview-frame"
        title="PDF preview"
      />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Download, FullScreen, TopRight, ZoomIn, ZoomOut } from '@element-plus/icons-vue'
import { stopHtmlMediaPlayback } from '@/utils/mediaPlayback'

const props = defineProps<{
  modelValue: boolean
  title: string
  kind: 'image' | 'pdf' | 'video' | ''
  objectUrl: string
  loading?: boolean
  gp: (key: string, params?: Record<string, string | number>) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  closed: []
  download: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

const fullscreen = ref(true)
const imageScale = ref(1)
const videoStageRef = ref<HTMLElement | null>(null)

function stopVideoPlayback() {
  stopHtmlMediaPlayback(videoStageRef.value)
}

const pdfSrc = computed(() => {
  if (!props.objectUrl || props.kind !== 'pdf') return ''
  return `${props.objectUrl}#view=FitH&toolbar=1&navpanes=0`
})

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      fullscreen.value = props.kind === 'pdf' || props.kind === 'video'
      imageScale.value = 1
    } else {
      stopVideoPlayback()
    }
  },
)

watch(
  () => props.kind,
  (kind) => {
    if (props.modelValue) {
      fullscreen.value = kind === 'pdf' || kind === 'video'
    }
  },
)

function toggleFullscreen() {
  fullscreen.value = !fullscreen.value
}

function zoomIn() {
  imageScale.value = Math.min(3, Math.round((imageScale.value + 0.25) * 100) / 100)
}

function zoomOut() {
  imageScale.value = Math.max(0.5, Math.round((imageScale.value - 0.25) * 100) / 100)
}

function resetZoom() {
  imageScale.value = 1
}

function openNewTab() {
  if (!props.objectUrl) return
  window.open(props.objectUrl, '_blank', 'noopener,noreferrer')
}

function handleClosed() {
  stopVideoPlayback()
  emit('closed')
}
</script>

<style scoped>
.preview-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.toolbar-spacer {
  flex: 1;
}
.zoom-label {
  min-width: 72px;
  cursor: default;
}
.preview-viewport {
  min-height: 60vh;
  height: calc(100vh - 180px);
  background: var(--el-fill-color-light);
  border-radius: 6px;
  overflow: auto;
}
.file-preview-dialog.is-fullscreen .preview-viewport {
  height: calc(100vh - 140px);
}
.image-stage {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.preview-image {
  max-width: 100%;
  transform-origin: center center;
  transition: transform 0.15s ease;
  box-shadow: 0 2px 12px rgb(0 0 0 / 12%);
}
.preview-frame {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
  background: #525659;
}
.video-stage {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: #1a1a1a;
}
.preview-video {
  max-width: 100%;
  max-height: calc(100vh - 200px);
  outline: none;
}
</style>

<style>
.file-preview-dialog .el-dialog__body {
  padding-top: 8px;
}
</style>
