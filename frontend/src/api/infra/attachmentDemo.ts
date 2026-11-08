import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  AttachmentDemoBatchDeletePayload,
  AttachmentDemoForm,
  AttachmentDemoPageQuery,
  AttachmentDemoStatusPayload,
  AttachmentDemoVO,
} from '@/types/infra/attachmentDemo'

const BASE = '/infra/attachmentDemo'

export function pageAttachmentDemo(params: AttachmentDemoPageQuery) {
  return request.get<ApiResponse<PageResult<AttachmentDemoVO>>>(`${BASE}/page`, { params })
}

export function getAttachmentDemo(id: string) {
  return request.get<ApiResponse<AttachmentDemoVO>>(`${BASE}/${id}`)
}

export function createAttachmentDemo(data: AttachmentDemoForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateAttachmentDemo(data: AttachmentDemoForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function updateAttachmentDemoStatus(data: AttachmentDemoStatusPayload) {
  return request.put<ApiResponse<void>>(`${BASE}/status`, data)
}

export function deleteAttachmentDemo(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export function batchDeleteAttachmentDemo(data: AttachmentDemoBatchDeletePayload) {
  return request.delete<ApiResponse<void>>(`${BASE}/batch`, { data })
}

export const PERM_AttachmentDemo = {
  view: 'infra:attachmentDemo:view',
  list: 'infra:attachmentDemo:list',
  add: 'infra:attachmentDemo:add',
  update: 'infra:attachmentDemo:update',
  delete: 'infra:attachmentDemo:delete',
}

export const GEN_RESOURCE_KEY = 'infra.attachmentDemo' as const

export const genCrudResource = defineGenCrudResource(PERM_AttachmentDemo, {
  page: pageAttachmentDemo,
  get: getAttachmentDemo,
  create: createAttachmentDemo,
  update: updateAttachmentDemo,
  delete: deleteAttachmentDemo,
  batchDelete: batchDeleteAttachmentDemo,
})
