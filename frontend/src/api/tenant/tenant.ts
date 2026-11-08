/** AUTO-GENERATED — 租户管理 API */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import { STATUS_DISABLE, STATUS_NORMAL } from '@/constants/flags'
import type { ApiResponse, PageResult } from '@/types/common'
import type { TenantForm, TenantPageQuery, TenantProvisionVO, TenantVO } from '@/types/tenant/tenant'

const BASE = '/tenant/tenant'

export function pageTenant(params: TenantPageQuery) {
  return request.get<ApiResponse<PageResult<TenantVO>>>(`${BASE}/page`, { params })
}

export function getTenant(id: string) {
  return request.get<ApiResponse<TenantVO>>(`${BASE}/${id}`)
}

export function createTenant(data: TenantForm) {
  return request.post<ApiResponse<TenantProvisionVO>>(`${BASE}/create`, data)
}

export function updateTenant(data: TenantForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function updateTenantStatus(id: string, status: string) {
  return request.put<ApiResponse<void>>(`${BASE}/status`, { id, status })
}

export function deleteTenant(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export function disableTenant(id: string) {
  return updateTenantStatus(id, STATUS_DISABLE)
}

export function enableTenant(id: string) {
  return updateTenantStatus(id, STATUS_NORMAL)
}

export const PERM_Tenant = {
  view: 'tenant:tenant:view',
  list: 'tenant:tenant:list',
  add: 'tenant:tenant:add',
  update: 'tenant:tenant:update',
  delete: 'tenant:tenant:delete',
  disable: 'tenant:tenant:update',
}

/** @deprecated use PERM_Tenant */
export const PERM_TENANT = PERM_Tenant

export const GEN_RESOURCE_KEY = 'tenant.tenant' as const

export const genCrudResource = defineGenCrudResource(PERM_Tenant, {
  page: pageTenant,
  get: getTenant,
  create: createTenant,
  update: updateTenant,
  delete: deleteTenant,
})
