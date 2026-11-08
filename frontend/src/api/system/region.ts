/** AUTO-GENERATED — 行政区划 API */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type { RegionForm, RegionPageQuery, RegionVO } from '@/types/system/region'

const BASE = '/system/region'

export function pageRegion(params: RegionPageQuery) {
  return request.get<ApiResponse<PageResult<RegionVO>>>(`${BASE}/page`, { params })
}

export function getRegion(id: string) {
  return request.get<ApiResponse<RegionVO>>(`${BASE}/${id}`)
}

export function getRegionTree() {
  return request.get<ApiResponse<RegionVO[]>>(`${BASE}/tree`)
}

export function getTenantRegionTree() {
  return request.get<ApiResponse<RegionVO[]>>(`${BASE}/tenant-tree`)
}

export function getRegionSelectableRoots(tenantType?: string) {
  return request.get<ApiResponse<RegionVO[]>>(`${BASE}/selectable-roots`, {
    params: tenantType ? { tenantType } : undefined,
  })
}

export function createRegion(data: RegionForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateRegion(data: RegionForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteRegion(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_Region = {
  view: 'system:region:view',
  list: 'system:region:list',
  add: 'system:region:add',
  update: 'system:region:update',
  delete: 'system:region:delete',
}

export const GEN_RESOURCE_KEY = 'system.region' as const

export const genCrudResource = defineGenCrudResource(PERM_Region, {
  page: pageRegion,
  get: getRegion,
  create: createRegion,
  update: updateRegion,
  delete: deleteRegion,
})
