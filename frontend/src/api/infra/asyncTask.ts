/** AUTO-GENERATED — 异步任务表 API */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type { AsyncTaskForm, AsyncTaskPageQuery, AsyncTaskVO } from '@/types/infra/asyncTask'

const BASE = '/infra/asyncTask'

export function pageAsyncTask(params: AsyncTaskPageQuery) {
  return request.get<ApiResponse<PageResult<AsyncTaskVO>>>(`${BASE}/page`, { params })
}

export function getAsyncTask(id: string) {
  return request.get<ApiResponse<AsyncTaskVO>>(`${BASE}/${id}`)
}

/** Owner-only status (Excel async import poll). Authenticated; no asyncTask:list required. */
export function getAsyncTaskMine(id: string) {
  return request.get<ApiResponse<AsyncTaskVO>>(`${BASE}/mine/${id}`, { silentError: true })
}

export function createAsyncTask(data: AsyncTaskForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateAsyncTask(data: AsyncTaskForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteAsyncTask(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export { pollAsyncTask, ASYNC_TASK_STATUS, isAsyncTaskTerminal, AsyncTaskPollError } from '@/utils/asyncTaskPoll'
export type { PollAsyncTaskOptions } from '@/utils/asyncTaskPoll'

export const PERM_AsyncTask = {
  view: 'infra:asyncTask:view',
  list: 'infra:asyncTask:list',
  add: 'infra:asyncTask:add',
  update: 'infra:asyncTask:update',
  delete: 'infra:asyncTask:delete',
}

/** @deprecated use PERM_AsyncTask */
export const PERM_ASYNC_TASK = PERM_AsyncTask

export const GEN_RESOURCE_KEY = 'infra.asyncTask' as const

export const genCrudResource = defineGenCrudResource(PERM_AsyncTask, {
  page: pageAsyncTask,
  get: getAsyncTask,
  create: createAsyncTask,
  update: updateAsyncTask,
  delete: deleteAsyncTask,
})
