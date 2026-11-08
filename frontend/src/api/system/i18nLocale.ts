/** AUTO-GENERATED — 国际化语言配置表 API */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type { I18nLocaleForm, I18nLocalePageQuery, I18nLocaleVO } from '@/types/system/i18nLocale'

const BASE = '/system/i18n/locale'

export function pageI18nLocale(params: I18nLocalePageQuery) {
  return request.get<ApiResponse<PageResult<I18nLocaleVO>>>(`${BASE}/page`, { params })
}

export function getI18nLocale(id: string) {
  return request.get<ApiResponse<I18nLocaleVO>>(`${BASE}/${id}`)
}

export function createI18nLocale(data: I18nLocaleForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateI18nLocale(data: I18nLocaleForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteI18nLocale(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_I18nLocale = {
  view: 'system:i18nLocale:view',
  list: 'system:i18nLocale:list',
  add: 'system:i18nLocale:add',
  update: 'system:i18nLocale:update',
  delete: 'system:i18nLocale:delete',
}

/** @deprecated use PERM_I18nLocale */
export const PERM_I18N_LOCALE = PERM_I18nLocale

export const GEN_RESOURCE_KEY = 'system.i18nLocale' as const

export const genCrudResource = defineGenCrudResource(PERM_I18nLocale, {
  page: pageI18nLocale,
  get: getI18nLocale,
  create: createI18nLocale,
  update: updateI18nLocale,
  delete: deleteI18nLocale,
})
