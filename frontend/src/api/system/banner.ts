import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  BannerBatchDeletePayload,
  BannerForm,
  BannerListVO,
  BannerPageQuery,
  BannerPublishedQuery,
  BannerPublishedVO,
  BannerStatusPayload,
  BannerVO,
} from '@/types/system/banner'

const BASE = '/system/banner'

export function pageBanner(params: BannerPageQuery) {
  return request.get<ApiResponse<PageResult<BannerListVO>>>(`${BASE}/page`, { params })
}

export function listPublishedBanner(params: BannerPublishedQuery) {
  return request.get<ApiResponse<BannerPublishedVO[]>>(`${BASE}/published`, { params })
}

export function getBanner(id: string) {
  return request.get<ApiResponse<BannerVO>>(`${BASE}/${id}`)
}

export function createBanner(data: BannerForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateBanner(data: BannerForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function updateBannerStatus(data: BannerStatusPayload) {
  return request.put<ApiResponse<void>>(`${BASE}/status`, data)
}

export function deleteBanner(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export function batchDeleteBanner(data: BannerBatchDeletePayload) {
  return request.delete<ApiResponse<void>>(`${BASE}/batch`, { data })
}

export const PERM_Banner = {
  view: 'system:banner:view',
  list: 'system:banner:list',
  add: 'system:banner:add',
  update: 'system:banner:update',
  delete: 'system:banner:delete',
}

export const GEN_RESOURCE_KEY = 'system.banner' as const

export const genCrudResource = defineGenCrudResource(PERM_Banner, {
  page: pageBanner,
  get: getBanner,
  create: createBanner,
  update: updateBanner,
  delete: deleteBanner,
  batchDelete: batchDeleteBanner,
})
