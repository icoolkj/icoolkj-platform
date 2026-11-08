/** 文件上传配置 API */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type {
  FileCatalogUpsertRequest,
  FileCatalogVO,
  FileProfileBatchDeleteRequest,
  FileProfileUpsertRequest,
  FileProfileVO,
} from '@/types/infra/fileProfile'

const BASE = '/infra/file-profile'

export function listFileProfiles(params?: {
  categoryTag?: string
  keyword?: string
  targetTenantId?: string
}) {
  return request.get<ApiResponse<FileProfileVO[]>>(`${BASE}/list`, { params })
}

export function resolveFileProfile(code: string, targetTenantId?: string) {
  return request.get<ApiResponse<FileProfileVO>>(`${BASE}/resolve`, {
    params: { code, targetTenantId },
  })
}

export function upsertFileProfile(data: FileProfileUpsertRequest) {
  return request.put<ApiResponse<void>>(BASE, data)
}

export function resetFileProfileOverride(profileCode: string, targetTenantId?: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${encodeURIComponent(profileCode)}/override`, {
    params: { targetTenantId },
  })
}

export function deleteFileProfile(profileCode: string, targetTenantId?: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${encodeURIComponent(profileCode)}`, {
    params: { targetTenantId },
  })
}

export function batchDeleteFileProfiles(data: FileProfileBatchDeleteRequest) {
  return request.delete<ApiResponse<void>>(`${BASE}/batch`, { data })
}

export function listFileCatalogs(targetTenantId?: string) {
  return request.get<ApiResponse<FileCatalogVO[]>>(`${BASE}/catalogs`, {
    params: { targetTenantId },
  })
}

export function getFileCatalog(catalogCode: string, targetTenantId?: string) {
  return request.get<ApiResponse<FileCatalogVO>>(
    `${BASE}/catalogs/${encodeURIComponent(catalogCode)}`,
    { params: { targetTenantId } },
  )
}

export function upsertFileCatalog(data: FileCatalogUpsertRequest) {
  return request.put<ApiResponse<void>>(`${BASE}/catalogs`, data)
}

export function deleteFileCatalog(catalogCode: string, targetTenantId?: string) {
  return request.delete<ApiResponse<void>>(
    `${BASE}/catalogs/${encodeURIComponent(catalogCode)}`,
    { params: { targetTenantId } },
  )
}

/** 操作权限共用；各页面入口 view 在菜单上已拆分（fileCatalog / fileCatalogItem / fileProfile） */
export const PERM_FileProfile = {
  view: 'infra:fileProfile:view',
  catalogView: 'infra:fileCatalog:view',
  catalogItemView: 'infra:fileCatalogItem:view',
  query: 'infra:fileProfile:query',
  add: 'infra:fileProfile:add',
  update: 'infra:fileProfile:update',
  delete: 'infra:fileProfile:delete',
}
