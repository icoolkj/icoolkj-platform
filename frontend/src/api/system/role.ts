import request, { type SilentRequestConfig } from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type { RoleForm, RolePageQuery, RoleVO } from '@/types/system/role'

const BASE = '/system/role'

export function pageRole(params: RolePageQuery, config?: SilentRequestConfig) {
  return request.get<ApiResponse<PageResult<RoleVO>>>(`${BASE}/page`, { ...config, params })
}

export function getRole(id: string) {
  return request.get<ApiResponse<RoleVO>>(`${BASE}/${id}`)
}

export function createRole(data: RoleForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateRole(data: RoleForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteRole(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export function getRoleMenuIds(roleId: string) {
  return request.get<ApiResponse<string[]>>(`${BASE}/${roleId}/menuIds`)
}

export function getRoleAssignableMenus(roleId: string) {
  return request.get<ApiResponse<import('@/types/system/menu').MenuVO[]>>(
    `${BASE}/${roleId}/assignable-menus`,
  )
}

export function assignRoleMenus(roleId: string, menuIds: string[]) {
  return request.put<ApiResponse<void>>(`${BASE}/${roleId}/menus`, { menuIds })
}

export const PERM_Role = {
  view: 'system:role:view',
  list: 'system:role:list',
  add: 'system:role:add',
  update: 'system:role:update',
  delete: 'system:role:delete',
}

/** @deprecated use PERM_Role */
export const PERM_ROLE = PERM_Role

export const GEN_RESOURCE_KEY = 'system.role' as const

export const genCrudResource = defineGenCrudResource(PERM_Role, {
  page: pageRole,
  get: getRole,
  create: createRole,
  update: updateRole,
  delete: deleteRole,
})
