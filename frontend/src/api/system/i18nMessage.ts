/** AUTO-GENERATED — 平台国际化文案表 API */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  I18nMessageForm,
  I18nMessagePageQuery,
  I18nMessageVO,
} from '@/types/system/i18nMessage'

const BASE = '/system/i18n/message'

export function pageI18nMessage(params: I18nMessagePageQuery) {
  return request.get<ApiResponse<PageResult<I18nMessageVO>>>(`${BASE}/page`, { params })
}

export function getI18nMessage(id: string) {
  return request.get<ApiResponse<I18nMessageVO>>(`${BASE}/${id}`)
}

export function createI18nMessage(data: I18nMessageForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateI18nMessage(data: I18nMessageForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteI18nMessage(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_I18nMessage = {
  view: 'system:i18nMessage:view',
  list: 'system:i18nMessage:list',
  add: 'system:i18nMessage:add',
  update: 'system:i18nMessage:update',
  delete: 'system:i18nMessage:delete',
}

/** @deprecated use PERM_I18nMessage */
export const PERM_I18N_MESSAGE = PERM_I18nMessage

export function refreshI18nCache() {
  return request.post<ApiResponse<void>>(`${BASE}/refreshCache`)
}

export const GEN_RESOURCE_KEY = 'system.i18nMessage' as const

export const genCrudResource = defineGenCrudResource(PERM_I18nMessage, {
  page: pageI18nMessage,
  get: getI18nMessage,
  create: createI18nMessage,
  update: updateI18nMessage,
  delete: deleteI18nMessage,
})
