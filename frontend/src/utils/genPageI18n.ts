import type { GenI18nMessages } from '@/types/tool/genI18n'
import profileZhCN from '@/i18n/gen/profile/zh-CN.json'
import profileEnUS from '@/i18n/gen/profile/en-US.json'

/** Git-bundled gen page i18n for shell routes (breadcrumb / navbar). */
const GEN_PAGE_I18N: Record<string, Record<string, GenI18nMessages>> = {
  profile: {
    'zh-CN': profileZhCN as GenI18nMessages,
    'en-US': profileEnUS as GenI18nMessages,
  },
}

export function resolveGenPageText(business: string, locale: string, key: string) {
  const pack = GEN_PAGE_I18N[business]?.[locale] ?? GEN_PAGE_I18N[business]?.['zh-CN']
  return pack?.page?.[key] ?? key
}
