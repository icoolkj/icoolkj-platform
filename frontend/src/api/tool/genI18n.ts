import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type { GenI18nMessageRow } from '@/types/tool/genI18n'

const BASE = '/tool/gen/i18n'

export function listGenI18n(tableId: string) {
  return request.get<ApiResponse<GenI18nMessageRow[]>>(`${BASE}/list/${tableId}`)
}

export function getGenI18n(tableId: string, locale: string) {
  return request.get<ApiResponse<GenI18nMessageRow>>(`${BASE}/messages`, {
    params: { tableId, locale },
  })
}

export function saveGenI18n(data: GenI18nMessageRow) {
  return request.put<ApiResponse<void>>(`${BASE}/messages`, data)
}

export function syncGenI18n(tableId: string) {
  return request.post<ApiResponse<void>>(`${BASE}/sync/${tableId}`)
}

export function restoreGenI18nBaseline(tableId: string, locale: string) {
  return request.post<ApiResponse<void>>(`${BASE}/restore-baseline`, null, {
    params: { tableId, locale },
  })
}

/** @deprecated Runtime Git-only — CRUD pages import local JSON; kept for legacy callers */
export function getGenI18nRuntimeByBusiness(
  businessName: string,
  apiModule: string,
  locale?: string,
) {
  return request.get<ApiResponse<GenI18nMessageRow>>(
    `/gen/i18n/messages/by-business/${businessName}`,
    { params: { apiModule, ...(locale ? { locale } : {}) } },
  )
}

/** @deprecated Use getGenI18nRuntimeByBusiness — tableId changes on re-import */
export function getGenI18nRuntime(tableId: string, locale?: string) {
  return request.get<ApiResponse<GenI18nMessageRow>>(`/gen/i18n/messages/${tableId}`, {
    params: locale ? { locale } : undefined,
  })
}
