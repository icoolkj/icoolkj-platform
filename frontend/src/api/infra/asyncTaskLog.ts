/** AUTO-GENERATED — 异步任务日志表 API */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  AsyncTaskLogForm,
  AsyncTaskLogPageQuery,
  AsyncTaskLogVO,
} from '@/types/infra/asyncTaskLog'

const BASE = '/infra/asyncTaskLog'

export function pageAsyncTaskLog(params: AsyncTaskLogPageQuery) {
  return request.get<ApiResponse<PageResult<AsyncTaskLogVO>>>(`${BASE}/page`, { params })
}

export function getAsyncTaskLog(id: string) {
  return request.get<ApiResponse<AsyncTaskLogVO>>(`${BASE}/${id}`)
}

export function createAsyncTaskLog(data: AsyncTaskLogForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateAsyncTaskLog(data: AsyncTaskLogForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteAsyncTaskLog(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_AsyncTaskLog = {
  view: 'infra:asyncTaskLog:view',
  list: 'infra:asyncTaskLog:list',
}

/** @deprecated use PERM_AsyncTaskLog */
export const PERM_ASYNC_TASK_LOG = PERM_AsyncTaskLog

export const GEN_RESOURCE_KEY = 'infra.asyncTaskLog' as const

export const genCrudResource = defineGenCrudResource(PERM_AsyncTaskLog, {
  page: pageAsyncTaskLog,
  get: getAsyncTaskLog,
  create: createAsyncTaskLog,
  update: updateAsyncTaskLog,
  delete: deleteAsyncTaskLog,
})
