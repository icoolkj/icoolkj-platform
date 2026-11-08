import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { applyThemePreset, getThemePreset, THEME_PRESETS, THEME_STORAGE_KEY } from '@/utils/theme'
import { updatePreference } from '@/api/auth/preference'
import { getToken } from '@/utils/request'
import { useLayoutStore } from '@/stores/layout'

export const useThemeStore = defineStore('theme', () => {
  const themeId = ref(localStorage.getItem(THEME_STORAGE_KEY) || 'blue')

  const preset = computed(() => getThemePreset(themeId.value))

  const themeOptions = computed(() =>
    THEME_PRESETS.map((item) => ({
      id: item.id,
      primary: item.primary,
    })),
  )

  function applyTheme(id: string) {
    themeId.value = id
    localStorage.setItem(THEME_STORAGE_KEY, id)
    applyThemePreset(getThemePreset(id))
    useLayoutStore().applyShellStyles()
  }

  async function setTheme(id: string, persist = true) {
    applyTheme(id)
    if (persist && getToken()) {
      await updatePreference({ theme: id })
    }
  }

  function initTheme() {
    applyThemePreset(getThemePreset(themeId.value))
  }

  return {
    themeId,
    preset,
    themeOptions,
    setTheme,
    applyTheme,
    initTheme,
  }
})
