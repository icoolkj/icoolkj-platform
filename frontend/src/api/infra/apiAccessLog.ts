/** AUTO-GENERATED — 接口访问日志表 API */
import request from '@/utils/request'
import {
  defineReadOnlyGenCrudResource,
} from '@/lib/gen-crud'
import type { GenCrudPerm } from '@/types/gen/crudApi'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  ApiAccessLogForm,
  ApiAccessLogPageQuery,
  ApiAccessLogVO,
} from '@/types/infra/apiAccessLog'

const BASE = '/infra/apiAccessLog'

export function pageApiAccessLog(params: ApiAccessLogPageQuery) {
  return request.get<ApiResponse<PageResult<ApiAccessLogVO>>>(`${BASE}/page`, { params })
}

export function getApiAccessLog(id: string) {
  return request.get<ApiResponse<ApiAccessLogVO>>(`${BASE}/${id}`)
}

export function createApiAccessLog(data: ApiAccessLogForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateApiAccessLog(data: ApiAccessLogForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteApiAccessLog(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_ApiAccessLog = {
  view: 'monitor:apiLog:view',
  list: 'monitor:apiLog:list',
}

/** @deprecated use PERM_ApiAccessLog */
export const PERM_API_ACCESS_LOG = PERM_ApiAccessLog

const PERM_ApiAccessLogCrud: GenCrudPerm = {
  view: PERM_ApiAccessLog.view,
  list: PERM_ApiAccessLog.list,
  add: 'monitor:apiLog:list',
  update: 'monitor:apiLog:list',
  delete: 'monitor:apiLog:list',
}

export const GEN_RESOURCE_KEY = 'infra.apiAccessLog' as const

export const genCrudResource = defineReadOnlyGenCrudResource(
  PERM_ApiAccessLogCrud,
  pageApiAccessLog,
)
