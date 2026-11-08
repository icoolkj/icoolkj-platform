import type { LayoutSettings } from '@/types/layout'
import type { ThemePreset } from '@/utils/theme'
import { LAYOUT_STORAGE_KEY } from '@/constants/storage'

export { LAYOUT_STORAGE_KEY }

export const DEFAULT_LAYOUT: LayoutSettings = {
  sidebarTheme: 'dark',
  headerStyle: 'primary',
  breadcrumb: true,
  tagsView: true,
  formMode: 'dialog',
  watermark: true,
  menuUniqueOpened: true,
  pageHeader: false,
}

export function normalizeLayout(partial?: Partial<LayoutSettings> | null): LayoutSettings {
  const base = { ...DEFAULT_LAYOUT, ...(partial ?? {}) }
  return {
    sidebarTheme: base.sidebarTheme === 'light' ? 'light' : 'dark',
    headerStyle: base.headerStyle === 'light' ? 'light' : 'primary',
    breadcrumb: base.breadcrumb !== false,
    tagsView: base.tagsView !== false,
    formMode: base.formMode === 'drawer' ? 'drawer' : 'dialog',
    watermark: base.watermark === true,
    menuUniqueOpened: base.menuUniqueOpened === true,
    pageHeader: base.pageHeader === true,
  }
}

export function loadLayoutFromStorage(): LayoutSettings {
  try {
    const raw = localStorage.getItem(LAYOUT_STORAGE_KEY)
    if (!raw) {
      return { ...DEFAULT_LAYOUT }
    }
    return normalizeLayout(JSON.parse(raw) as Partial<LayoutSettings>)
  } catch {
    return { ...DEFAULT_LAYOUT }
  }
}

export function saveLayoutToStorage(settings: LayoutSettings) {
  localStorage.setItem(LAYOUT_STORAGE_KEY, JSON.stringify(settings))
}

/** Apply sidebar CSS variables for dark / light shell. */
export function applySidebarTheme(
  sidebarTheme: LayoutSettings['sidebarTheme'],
  preset: ThemePreset,
) {
  const root = document.documentElement
  if (sidebarTheme === 'light') {
    root.style.setProperty('--ic-sidebar-bg', '#ffffff')
    root.style.setProperty('--ic-sidebar-bg-end', '#fafafa')
    root.style.setProperty('--ic-sidebar-active', preset.primaryLight9)
    root.style.setProperty('--ic-sidebar-text', '#595959')
    root.style.setProperty('--ic-sidebar-text-active', preset.primary)
    root.style.setProperty('--ic-sidebar-hover', 'rgba(0, 0, 0, 0.04)')
    root.style.setProperty('--ic-sidebar-border', '#f0f0f0')
    root.dataset.sidebarTheme = 'light'
  } else {
    root.style.setProperty('--ic-sidebar-bg', preset.sidebarBg)
    root.style.setProperty('--ic-sidebar-bg-end', preset.sidebarBgEnd)
    root.style.setProperty('--ic-sidebar-active', preset.sidebarActive)
    root.style.setProperty('--ic-sidebar-text', 'rgba(255, 255, 255, 0.75)')
    root.style.setProperty('--ic-sidebar-text-active', '#ffffff')
    root.style.setProperty('--ic-sidebar-hover', 'rgba(255, 255, 255, 0.06)')
    root.style.setProperty('--ic-sidebar-border', 'rgba(255, 255, 255, 0.08)')
    root.dataset.sidebarTheme = 'dark'
  }
}

/** Top shell bar colors — active nav uses same accent as sidebar menu. */
export function applyHeaderTheme(
  headerStyle: LayoutSettings['headerStyle'],
  preset: ThemePreset,
) {
  const root = document.documentElement
  const accent = preset.primary
  if (headerStyle === 'primary') {
    root.style.setProperty('--ic-header-bg', preset.sidebarBg)
    root.style.setProperty('--ic-header-bg-end', preset.sidebarBgEnd)
    root.style.setProperty('--ic-header-text', '#ffffff')
    root.style.setProperty('--ic-header-text-muted', 'rgba(255, 255, 255, 0.72)')
    root.style.setProperty('--ic-header-icon', 'rgba(255, 255, 255, 0.92)')
    root.style.setProperty('--ic-header-border', 'rgba(255, 255, 255, 0.08)')
    root.style.setProperty('--ic-header-hover', 'rgba(255, 255, 255, 0.08)')
    root.style.setProperty('--ic-header-nav-hover-text', '#ffffff')
    root.style.setProperty('--ic-header-nav-active', '#ffffff')
    root.style.setProperty('--ic-header-nav-active-bg', accent)
    root.style.setProperty('--ic-header-nav-indicator', 'transparent')
    root.style.setProperty('--ic-header-shadow', 'none')
    root.dataset.headerStyle = 'primary'
  } else {
    root.style.setProperty('--ic-header-bg', '#ffffff')
    root.style.setProperty('--ic-header-bg-end', '#ffffff')
    root.style.setProperty('--ic-header-text', '#262626')
    root.style.setProperty('--ic-header-text-muted', '#595959')
    root.style.setProperty('--ic-header-icon', '#595959')
    root.style.setProperty('--ic-header-border', '#f0f0f0')
    root.style.setProperty('--ic-header-hover', 'rgba(0, 0, 0, 0.04)')
    root.style.setProperty('--ic-header-nav-hover-text', accent)
    root.style.setProperty('--ic-header-nav-active', accent)
    root.style.setProperty('--ic-header-nav-active-bg', preset.primaryLight9)
    root.style.setProperty('--ic-header-nav-indicator', accent)
    root.style.setProperty('--ic-header-shadow', '0 1px 4px rgba(0, 21, 41, 0.04)')
    root.dataset.headerStyle = 'light'
  }
}

export function applyShellTheme(settings: LayoutSettings, preset: ThemePreset) {
  applySidebarTheme(settings.sidebarTheme, preset)
  applyHeaderTheme(settings.headerStyle, preset)
}
