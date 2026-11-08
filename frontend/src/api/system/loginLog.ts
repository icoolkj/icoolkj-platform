/** AUTO-GENERATED — 登录日志表 API */
import request from '@/utils/request'
import {
  defineGenCrudResource,
  wrapExportFileAsync,
} from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type { LoginLogForm, LoginLogPageQuery, LoginLogVO } from '@/types/system/loginLog'

const BASE = '/system/loginLog'

export function pageLoginLog(params: LoginLogPageQuery) {
  return request.get<ApiResponse<PageResult<LoginLogVO>>>(`${BASE}/page`, { params })
}

export function getLoginLog(id: string) {
  return request.get<ApiResponse<LoginLogVO>>(`${BASE}/${id}`)
}

export function createLoginLog(data: LoginLogForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateLoginLog(data: LoginLogForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteLoginLog(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

/** Async export — poll GET /infra/asyncTask/mine/{id} then download staged file. */
export function exportLoginLogAsync(params: LoginLogPageQuery) {
  return request.post<ApiResponse<{ taskId: string }>>(`${BASE}/export-async`, null, { params })
}

export const PERM_LoginLog = {
  view: 'monitor:loginLog:view',
  list: 'monitor:loginLog:list',
  export: 'monitor:loginLog:export',
}

/** @deprecated use PERM_LoginLog */
export const PERM_LOGIN_LOG = PERM_LoginLog

export const GEN_RESOURCE_KEY = 'system.loginLog' as const

export const genCrudResource = defineGenCrudResource(PERM_LoginLog, {
  page: pageLoginLog,
  exportFileAsync: wrapExportFileAsync(exportLoginLogAsync),
})
