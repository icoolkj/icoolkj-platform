/**
 * Site day/night theme — same pattern as Admin `utils/theme.ts`:
 * storage key + apply to documentElement, no framework lock-in.
 */
export type SiteTheme = 'light' | 'dark'

export const SITE_THEME_STORAGE_KEY = 'icoolkj-site-theme'

export function readStoredTheme(): SiteTheme | null {
  try {
    const raw = localStorage.getItem(SITE_THEME_STORAGE_KEY)
    if (raw === 'light' || raw === 'dark') return raw
  } catch {
    /* ignore */
  }
  return null
}

export function preferSystemTheme(): SiteTheme {
  if (typeof window !== 'undefined' && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return 'dark'
  }
  return 'light'
}

export function resolveInitialTheme(): SiteTheme {
  return readStoredTheme() ?? preferSystemTheme()
}

export function applySiteTheme(theme: SiteTheme) {
  const root = document.documentElement
  root.setAttribute('data-theme', theme)
  root.style.colorScheme = theme
  const meta = document.querySelector('meta[name="theme-color"]')
  if (meta) {
    meta.setAttribute('content', theme === 'dark' ? '#0f1419' : '#f5f7fa')
  }
}

export function persistSiteTheme(theme: SiteTheme) {
  try {
    localStorage.setItem(SITE_THEME_STORAGE_KEY, theme)
  } catch {
    /* ignore */
  }
}

export function setSiteTheme(theme: SiteTheme) {
  applySiteTheme(theme)
  persistSiteTheme(theme)
}

export function toggleSiteTheme(current: SiteTheme): SiteTheme {
  const next: SiteTheme = current === 'light' ? 'dark' : 'light'
  setSiteTheme(next)
  return next
}
