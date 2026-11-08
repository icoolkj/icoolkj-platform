import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type { BrandPack } from '@/types/system/config'

const BASE = '/system/brand-pack'

export interface BrandPackAdminVO extends BrandPack {
  targetTenantId?: string
  targetTenantCode?: string | null
  targetTenantName?: string | null
  tenantOverride?: boolean
  canSelectTenant?: boolean
}

export type BrandPackUpdateRequest = BrandPack

export function getBrandPack(targetTenantId?: string) {
  return request.get<ApiResponse<BrandPackAdminVO>>(BASE, {
    params: targetTenantId ? { targetTenantId } : {},
  })
}

export function updateBrandPack(data: BrandPackUpdateRequest, targetTenantId?: string) {
  return request.put<ApiResponse<void>>(BASE, data, {
    params: targetTenantId ? { targetTenantId } : {},
  })
}

export function resetBrandPack(targetTenantId?: string) {
  return request.delete<ApiResponse<void>>(BASE, {
    params: targetTenantId ? { targetTenantId } : {},
  })
}

export const PERM_BrandPack = {
  view: 'system:brandPack:view',
  query: 'system:brandPack:query',
  update: 'system:brandPack:update',
}
