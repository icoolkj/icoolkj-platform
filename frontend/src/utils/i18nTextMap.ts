/** 业务字段多语言 map：localeCode → text（对应 DB 中 *_i18n JSON） */

export type I18nTextMap = Record<string, string>

export function resolveI18nText(
  map: I18nTextMap | null | undefined,
  fallback: string | null | undefined,
  locale: string,
  defaultLocale = 'zh-CN',
): string {
  const normalized = (locale || '').trim()
  if (map) {
    const hit = map[normalized]?.trim()
    if (hit) return hit
    if (normalized !== defaultLocale) {
      const def = map[defaultLocale]?.trim()
      if (def) return def
    }
    for (const v of Object.values(map)) {
      const t = v?.trim()
      if (t) return t
    }
  }
  return (fallback || '').trim()
}

/** 清理空值；无有效项返回 undefined */
export function compactI18nTextMap(map: I18nTextMap | null | undefined): I18nTextMap | undefined {
  if (!map) return undefined
  const out: I18nTextMap = {}
  for (const [k, v] of Object.entries(map)) {
    const key = k?.trim()
    const val = v?.trim()
    if (key && val) out[key] = val
  }
  return Object.keys(out).length ? out : undefined
}

/** 用默认名回填主语言，再打开编辑 */
export function seedI18nTextMap(
  map: I18nTextMap | null | undefined,
  fallback: string | null | undefined,
  locales: string[],
  primaryLocale = 'zh-CN',
): I18nTextMap {
  const out: I18nTextMap = {}
  for (const code of locales) {
    out[code] = map?.[code]?.trim() || ''
  }
  const primary = locales.includes(primaryLocale) ? primaryLocale : locales[0]
  if (primary && !out[primary] && fallback?.trim()) {
    out[primary] = fallback.trim()
  }
  return out
}

/** 默认展示名：主语言 → 任一非空 */
export function primaryI18nText(
  map: I18nTextMap | null | undefined,
  primaryLocale = 'zh-CN',
): string {
  const compact = compactI18nTextMap(map)
  if (!compact) return ''
  if (compact[primaryLocale]?.trim()) return compact[primaryLocale].trim()
  return Object.values(compact)[0] || ''
}
