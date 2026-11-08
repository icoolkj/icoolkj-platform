/** AUTO-GENERATED ‚Ä?Êìç‰ΩúÊó•ÂøóË°?API */
import request from '@/utils/request'
import {
  defineGenCrudResource,
  wrapExportFileAsync,
} from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  OperationLogForm,
  OperationLogPageQuery,
  OperationLogVO,
} from '@/types/system/operationLog'

const BASE = '/system/operationLog'

export function pageOperationLog(params: OperationLogPageQuery) {
  return request.get<ApiResponse<PageResult<OperationLogVO>>>(`${BASE}/page`, { params })
}

export function getOperationLog(id: string) {
  return request.get<ApiResponse<OperationLogVO>>(`${BASE}/${id}`)
}

export function createOperationLog(data: OperationLogForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateOperationLog(data: OperationLogForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteOperationLog(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

/** Async export ‚Ä?poll GET /infra/asyncTask/mine/{id} then download staged file. */
export function exportOperationLogAsync(params: OperationLogPageQuery) {
  return request.post<ApiResponse<{ taskId: string }>>(`${BASE}/export-async`, null, { params })
}

export const PERM_OperationLog = {
  view: 'monitor:operationLog:view',
  list: 'monitor:operationLog:list',
  export: 'monitor:operationLog:export',
}

/** @deprecated use PERM_OperationLog */
export const PERM_OPERATION_LOG = PERM_OperationLog

export const GEN_RESOURCE_KEY = 'system.operationLog' as const

export const genCrudResource = defineGenCrudResource(PERM_OperationLog, {
  page: pageOperationLog,
  exportFileAsync: wrapExportFileAsync(exportOperationLogAsync),
})
