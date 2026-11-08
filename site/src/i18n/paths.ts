import type { SiteLocale } from '@/config/site'
import { SITE_LOCALES } from '@/config/site'

/** Strip locale prefix → logical path like `/product` */
export function stripLocalePrefix(path: string): string {
  const normalized = path.replace(/\/+$/, '') || '/'
  for (const item of SITE_LOCALES) {
    if (!item.pathPrefix) continue
    const prefix = `/${item.pathPrefix}`
    if (normalized === prefix) return '/'
    if (normalized.startsWith(`${prefix}/`)) {
      return normalized.slice(prefix.length) || '/'
    }
  }
  return normalized === '' ? '/' : normalized
}

export function localeFromPath(path: string): SiteLocale {
  const normalized = path.replace(/\/+$/, '') || '/'
  for (const item of SITE_LOCALES) {
    if (!item.pathPrefix) continue
    const prefix = `/${item.pathPrefix}`
    if (normalized === prefix || normalized.startsWith(`${prefix}/`)) {
      return item.code
    }
  }
  return 'zh-CN'
}

/** Build localized path for a logical route (`/`, `/product`, …) */
export function localizedPath(logicalPath: string, locale: SiteLocale): string {
  const base = logicalPath.startsWith('/') ? logicalPath : `/${logicalPath}`
  const clean = base === '/' ? '' : base.replace(/\/+$/, '')
  const meta = SITE_LOCALES.find((item) => item.code === locale)
  const prefix = meta?.pathPrefix ? `/${meta.pathPrefix}` : ''
  if (!clean) return prefix || '/'
  return `${prefix}${clean}`
}
