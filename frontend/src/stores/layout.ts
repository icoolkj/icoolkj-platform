import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { LayoutSettings } from '@/types/layout'
import {
  applyShellTheme,
  DEFAULT_LAYOUT,
  loadLayoutFromStorage,
  normalizeLayout,
  saveLayoutToStorage,
} from '@/utils/layout'
import { getThemePreset } from '@/utils/theme'
import { updatePreference } from '@/api/auth/preference'
import { getToken } from '@/utils/request'
import { useThemeStore } from '@/stores/theme'
import { useTagsViewStore } from '@/stores/tagsView'

export const useLayoutStore = defineStore('layout', () => {
  const settings = ref<LayoutSettings>(loadLayoutFromStorage())
  const settingsVisible = ref(false)

  const sidebarTheme = computed(() => settings.value.sidebarTheme)
  const breadcrumb = computed(() => settings.value.breadcrumb)
  const tagsView = computed(() => settings.value.tagsView)
  const formMode = computed(() => settings.value.formMode)
  const watermark = computed(() => settings.value.watermark)
  const menuUniqueOpened = computed(() => settings.value.menuUniqueOpened)
  const pageHeader = computed(() => settings.value.pageHeader)

  function applyShellStyles() {
    const themeStore = useThemeStore()
    applyShellTheme(settings.value, getThemePreset(themeStore.themeId))
  }

  function syncTagsViewState(enabled: boolean) {
    if (!enabled) {
      useTagsViewStore().clearViews()
    }
  }

  function patchSettings(partial: Partial<LayoutSettings>) {
    const prevTagsView = settings.value.tagsView
    settings.value = normalizeLayout({ ...settings.value, ...partial })
    saveLayoutToStorage(settings.value)
    applyShellStyles()
    if (partial.tagsView !== undefined && partial.tagsView !== prevTagsView) {
      syncTagsViewState(settings.value.tagsView)
    }
  }

  /** Apply server-side layout as source of truth (login / preference sync). */
  function applySettings(partial?: Partial<LayoutSettings> | null) {
    if (!partial) {
      return
    }
    settings.value = normalizeLayout({ ...DEFAULT_LAYOUT, ...partial })
    saveLayoutToStorage(settings.value)
    applyShellStyles()
    syncTagsViewState(settings.value.tagsView)
  }

  async function updateSettings(partial: Partial<LayoutSettings>, persist = true) {
    patchSettings(partial)
    if (persist && getToken()) {
      const result = await updatePreference({ layout: partial })
      if (result.layout) {
        applySettings(result.layout)
      }
    }
  }

  /** Reset layout + theme locally and persist in a single request (avoids idempotent clash). */
  async function resetToDefaults() {
    const themeStore = useThemeStore()
    settings.value = normalizeLayout({ ...DEFAULT_LAYOUT })
    saveLayoutToStorage(settings.value)
    themeStore.applyTheme('blue')
    applyShellStyles()
    syncTagsViewState(settings.value.tagsView)
    if (getToken()) {
      const result = await updatePreference({
        theme: 'blue',
        layout: { ...DEFAULT_LAYOUT },
      })
      if (result.layout) {
        applySettings(result.layout)
      }
    }
  }

  function openSettings() {
    settingsVisible.value = true
  }

  function closeSettings() {
    settingsVisible.value = false
  }

  function initLayout() {
    applyShellStyles()
  }

  return {
    settings,
    settingsVisible,
    sidebarTheme,
    breadcrumb,
    tagsView,
    formMode,
    watermark,
    menuUniqueOpened,
    pageHeader,
    patchSettings,
    applySettings,
    updateSettings,
    resetToDefaults,
    openSettings,
    closeSettings,
    initLayout,
    applyShellStyles,
  }
})
