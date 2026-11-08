import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type { GenViewConfigRow } from '@/types/tool/genView'

const BASE = '/tool/gen/view'

export function getGenView(tableId: string) {
  return request.get<ApiResponse<GenViewConfigRow>>(`${BASE}/${tableId}`)
}

export function saveGenView(data: GenViewConfigRow) {
  return request.put<ApiResponse<void>>(BASE, data)
}

export function syncGenView(tableId: string) {
  return request.post<ApiResponse<void>>(`${BASE}/sync/${tableId}`)
}

export function restoreGenViewBaseline(tableId: string) {
  return request.post<ApiResponse<void>>(`${BASE}/restore-baseline/${tableId}`)
}

/** @deprecated Runtime Git-only — CRUD pages import view.json; kept for legacy callers */
export function getGenViewRuntimeByBusiness(businessName: string, apiModule: string) {
  return request.get<ApiResponse<GenViewConfigRow>>(`/gen/view/by-business/${businessName}`, {
    params: { apiModule },
  })
}
