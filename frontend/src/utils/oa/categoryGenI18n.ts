import type { GenFieldValidation, GenI18nMessages } from '@/types/tool/genI18n'

/**
 * OA category_code（snake）→ gen businessName（camel），对齐 i18n-standard：
 * `i18n/gen/{businessName}/zh-CN.json` 为表级 CRUD / 表单字段文案 SoT。
 * 例：oa_leave → oaLeave
 */
export function categoryToGenBusinessName(categoryCode?: string | null): string {
  if (!categoryCode?.trim()) return ''
  const parts = categoryCode
    .trim()
    .split(/[_\s-]+/)
    .filter(Boolean)
  if (!parts.length) return ''
  return parts
    .map((p, i) => {
      const lower = p.toLowerCase()
      if (i === 0) return lower
      return lower.charAt(0).toUpperCase() + lower.slice(1)
    })
    .join('')
}

type GenLocaleModule = { default?: GenI18nMessages } | GenI18nMessages

const zhModules = import.meta.glob('@/i18n/gen/*/zh-CN.json', {
  eager: true,
}) as Record<string, GenLocaleModule>

const enModules = import.meta.glob('@/i18n/gen/*/en-US.json', {
  eager: true,
}) as Record<string, GenLocaleModule>

function asMessages(mod: GenLocaleModule | undefined): GenI18nMessages | null {
  if (!mod) return null
  if ('default' in mod && mod.default) return mod.default
  return mod as GenI18nMessages
}

function findModule(
  modules: Record<string, GenLocaleModule>,
  businessName: string,
): GenI18nMessages | null {
  const suffix = `/i18n/gen/${businessName}/`
  for (const [path, mod] of Object.entries(modules)) {
    if (path.includes(suffix)) {
      return asMessages(mod)
    }
  }
  return null
}

/** 按 category 解析 gen 文案包；无对应目录则返回 null（用字段元数据兜底） */
export function resolveCategoryGenI18n(
  categoryCode?: string | null,
): Record<string, GenI18nMessages> | null {
  const businessName = categoryToGenBusinessName(categoryCode)
  if (!businessName) return null
  const zh = findModule(zhModules, businessName)
  const en = findModule(enModules, businessName)
  if (!zh && !en) return null
  const pack: Record<string, GenI18nMessages> = {}
  if (zh) pack['zh-CN'] = zh
  if (en) pack['en-US'] = en
  return pack
}

export function categoryFieldLabel(
  pack: GenI18nMessages | null | undefined,
  fieldCode: string,
  fallback: string,
): string {
  return pack?.fields?.[fieldCode]?.label || fallback
}

export function categoryFieldPlaceholder(
  pack: GenI18nMessages | null | undefined,
  fieldCode: string,
): string {
  return pack?.fields?.[fieldCode]?.placeholder || ''
}

export function categoryFieldValidation(
  pack: GenI18nMessages | null | undefined,
  fieldCode: string,
  rule: keyof GenFieldValidation,
): string {
  return pack?.fields?.[fieldCode]?.validation?.[rule] || ''
}
