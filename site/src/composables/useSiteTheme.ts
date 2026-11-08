import { onMounted, onUnmounted, ref } from 'vue'
import {
  applySiteTheme,
  preferSystemTheme,
  readStoredTheme,
  resolveInitialTheme,
  setSiteTheme,
  toggleSiteTheme,
  type SiteTheme,
} from '@/utils/theme'

const theme = ref<SiteTheme>(resolveInitialTheme())

export function useSiteTheme() {
  function syncFromDom() {
    const attr = document.documentElement.getAttribute('data-theme')
    theme.value = attr === 'dark' ? 'dark' : 'light'
  }

  function setTheme(next: SiteTheme) {
    setSiteTheme(next)
    theme.value = next
  }

  function toggle() {
    theme.value = toggleSiteTheme(theme.value)
  }

  let mq: MediaQueryList | null = null
  function onSystemChange(e: MediaQueryListEvent) {
    if (readStoredTheme()) return
    const next: SiteTheme = e.matches ? 'dark' : 'light'
    applySiteTheme(next)
    theme.value = next
  }

  onMounted(() => {
    syncFromDom()
    mq = window.matchMedia('(prefers-color-scheme: dark)')
    mq.addEventListener('change', onSystemChange)
  })

  onUnmounted(() => {
    mq?.removeEventListener('change', onSystemChange)
  })

  return {
    theme,
    setTheme,
    toggle,
    preferSystemTheme,
  }
}
