/** 平台：租户管理员运维 API */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type { TenantProvisionVO } from '@/types/tenant/tenant'
import { defineReadOnlyGenCrudResource } from '@/components/gen/registry/crud-resources/helpers'

const BASE = '/tenant/admin'

export interface TenantAdminVO {
  userId?: string
  tenantId?: string
  tenantCode?: string
  tenantName?: string
  tenantType?: string
  username?: string
  nickname?: string
  roleCode?: string
  roleName?: string
  status?: string
  loginFailCount?: number
  lockedUntil?: string
  lastLoginAt?: string
  mustChangePassword?: string
  mfaEnabled?: string
}

export interface TenantAdminPageQuery {
  pageNo?: number
  pageSize?: number
  tenantCode?: string
  tenantName?: string
  username?: string
  status?: string
}

export function pageTenantAdmin(params: TenantAdminPageQuery) {
  return request.get<ApiResponse<PageResult<TenantAdminVO>>>(`${BASE}/page`, { params })
}

export function resetTenantAdminPassword(userId: string) {
  return request.put<ApiResponse<TenantProvisionVO>>(`${BASE}/${userId}/reset-password`)
}

export function unlockTenantAdmin(userId: string) {
  return request.put<ApiResponse<void>>(`${BASE}/${userId}/unlock`)
}

export const PERM_TenantAdmin = {
  view: 'tenant:admin:view',
  list: 'tenant:admin:list',
  resetPassword: 'tenant:admin:resetPassword',
  unlock: 'tenant:admin:unlock',
}

export const GEN_RESOURCE_KEY = 'tenant.admin' as const

export const genCrudResource = defineReadOnlyGenCrudResource(PERM_TenantAdmin, pageTenantAdmin)
