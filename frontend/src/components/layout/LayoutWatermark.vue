<template>
  <div v-if="enabled" ref="rootRef" class="layout-watermark" aria-hidden="true" />
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user'

const props = defineProps<{ enabled: boolean }>()

const userStore = useUserStore()
const { user } = storeToRefs(userStore)
const rootRef = ref<HTMLDivElement | null>(null)

function renderWatermark() {
  const el = rootRef.value
  if (!el || !props.enabled) {
    if (el) {
      el.style.backgroundImage = ''
    }
    return
  }
  const text = user.value?.username || 'iCoolKJ'
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  if (!ctx) {
    return
  }
  canvas.width = 240
  canvas.height = 160
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  ctx.font = '14px sans-serif'
  ctx.fillStyle = 'rgba(0, 0, 0, 0.06)'
  ctx.translate(120, 80)
  ctx.rotate((-22 * Math.PI) / 180)
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(text, 0, 0)
  el.style.backgroundImage = `url(${canvas.toDataURL('image/png')})`
}

onMounted(renderWatermark)
watch([() => props.enabled, user], renderWatermark, { deep: true })
</script>

<style scoped>
.layout-watermark {
  pointer-events: none;
  position: fixed;
  inset: 0;
  z-index: 9999;
  background-repeat: repeat;
}
</style>
