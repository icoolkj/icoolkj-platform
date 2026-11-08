/** 文件 API */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import { defineReadOnlyGenCrudResource } from '@/components/gen/registry/crud-resources/helpers'
import type {
  FileBatchDeleteRequest,
  FilePageQuery,
  FilePreviewUrlVO,
  FileRefCreateRequest,
  FileRefVO,
  FileStorageOptionsVO,
  FileVO,
} from '@/types/infra/file'

const BASE = '/infra/file'

export function pageInfraFile(params: FilePageQuery) {
  return request.get<ApiResponse<PageResult<FileVO>>>(`${BASE}/page`, { params })
}

export function getInfraFile(id: string) {
  return request.get<ApiResponse<FileVO>>(`${BASE}/${id}`)
}

export function getFileStorageOptions(config?: { silentError?: boolean }) {
  return request.get<ApiResponse<FileStorageOptionsVO>>(`${BASE}/storage-options`, config)
}

export function uploadInfraFile(formData: FormData, config?: { silentError?: boolean }) {
  // 勿手动设 Content-Type：须由浏览器带 multipart boundary
  return request.post<ApiResponse<FileVO>>(`${BASE}/upload`, formData, config)
}

export function getInfraFilePreviewUrl(id: string) {
  return request.get<ApiResponse<FilePreviewUrlVO>>(`${BASE}/${id}/preview-url`)
}

export function deleteInfraFile(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

/** 无引用时逻辑删除（换图/清空），只需 upload 权限 */
export function discardUnreferencedInfraFile(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}/unreferenced`)
}

export function batchDeleteInfraFile(data: FileBatchDeleteRequest) {
  return request.delete<ApiResponse<void>>(`${BASE}/batch`, { data })
}

export function listInfraFileRefs(id: string) {
  return request.get<ApiResponse<FileRefVO[]>>(`${BASE}/${id}/refs`)
}

export function addInfraFileRef(id: string, data: FileRefCreateRequest) {
  return request.post<ApiResponse<void>>(`${BASE}/${id}/refs`, data)
}

export function releaseInfraFileRef(id: string, data: FileRefCreateRequest) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}/refs`, { data })
}

export const PERM_InfraFile = {
  view: 'infra:file:view',
  list: 'infra:file:list',
  upload: 'infra:file:upload',
  download: 'infra:file:download',
  /** 机密密级下载（对齐后端 download-guard） */
  downloadSensitive: 'infra:file:download:sensitive',
  delete: 'infra:file:delete',
}

/** @deprecated use PERM_InfraFile */
export const PERM_INFRA_FILE = PERM_InfraFile

export const GEN_RESOURCE_KEY = 'infra.file' as const

export const genCrudResource = defineReadOnlyGenCrudResource(PERM_InfraFile, pageInfraFile, {
  get: getInfraFile,
  delete: deleteInfraFile,
  batchDelete: batchDeleteInfraFile,
})
