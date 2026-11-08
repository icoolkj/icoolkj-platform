/** AUTO-GENERATED — 部门表 API */
import request, { type SilentRequestConfig } from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type { DeptForm, DeptPageQuery, DeptVO } from '@/types/system/dept'

const BASE = '/system/dept'

export function pageDept(params: DeptPageQuery) {
  return request.get<ApiResponse<PageResult<DeptVO>>>(`${BASE}/page`, { params })
}

export function getDept(id: string, config?: SilentRequestConfig) {
  return request.get<ApiResponse<DeptVO>>(`${BASE}/${id}`, config)
}

export function createDept(data: DeptForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateDept(data: DeptForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteDept(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_Dept = {
  view: 'system:dept:view',
  list: 'system:dept:list',
  add: 'system:dept:add',
  update: 'system:dept:update',
  delete: 'system:dept:delete',
}

/** @deprecated use PERM_Dept */
export const PERM_DEPT = PERM_Dept

export const GEN_RESOURCE_KEY = 'system.dept' as const

export const genCrudResource = defineGenCrudResource(PERM_Dept, {
  page: pageDept,
  get: getDept,
  create: createDept,
  update: updateDept,
  delete: deleteDept,
})
