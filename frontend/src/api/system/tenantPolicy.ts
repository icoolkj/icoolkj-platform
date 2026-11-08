import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

const BASE = '/system/tenant-policy'

export interface TenantPolicyConfigItem {
  configKey: string
  valueType: string
  platformDefaultValue: string | null
  effectiveValue: string | null
  tenantValue: string | null
  overridden: boolean
  sensitive?: boolean
}

export function listTenantPolicyCatalog() {
  return request.get<ApiResponse<TenantPolicyConfigItem[]>>(`${BASE}/catalog`)
}

export function upsertTenantPolicy(configKey: string, configValue: string) {
  return request.put<ApiResponse<void>>(`${BASE}/${encodeURIComponent(configKey)}`, {
    configValue,
  })
}

export function resetTenantPolicy(configKey: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${encodeURIComponent(configKey)}`)
}

export function refreshTenantPolicyCache() {
  return request.post<ApiResponse<void>>(`${BASE}/refreshCache`)
}

export const PERM_TenantPolicy = {
  view: 'system:tenantPolicy:view',
  query: 'system:tenantPolicy:query',
  update: 'system:tenantPolicy:update',
}
