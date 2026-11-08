import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type { I18nBootstrap } from '@/types/system/i18n'

export {
  pageI18nLocale,
  getI18nLocale,
  createI18nLocale,
  updateI18nLocale,
  deleteI18nLocale,
  PERM_I18nLocale,
  PERM_I18N_LOCALE,
} from '@/api/system/i18nLocale'
export {
  pageI18nMessage,
  getI18nMessage,
  createI18nMessage,
  updateI18nMessage,
  deleteI18nMessage,
  refreshI18nCache,
  PERM_I18nMessage,
  PERM_I18N_MESSAGE,
} from '@/api/system/i18nMessage'

const BASE = '/system/i18n'

export function getI18nBootstrap() {
  return request.get<ApiResponse<I18nBootstrap>>(`${BASE}/bootstrap`)
}

export function getI18nMessages(locale: string, scope = 'frontend') {
  return request.get<ApiResponse<Record<string, string>>>(`${BASE}/messages`, {
    params: { locale, scope },
  })
}
