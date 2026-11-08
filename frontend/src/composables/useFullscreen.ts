import { onMounted, onUnmounted, ref } from 'vue'

/** Browser Fullscreen API for the whole app shell (documentElement). */
export function useFullscreen() {
  const isFullscreen = ref(false)

  function sync() {
    isFullscreen.value = Boolean(document.fullscreenElement)
  }

  async function toggle() {
    try {
      if (document.fullscreenElement) {
        await document.exitFullscreen()
      } else {
        await document.documentElement.requestFullscreen()
      }
    } catch {
      // Browser may deny (policy / iframe / unsupported).
    }
  }

  onMounted(() => {
    sync()
    document.addEventListener('fullscreenchange', sync)
  })

  onUnmounted(() => {
    document.removeEventListener('fullscreenchange', sync)
  })

  return { isFullscreen, toggle }
}
