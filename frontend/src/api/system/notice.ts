import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  NoticeApproveDetailVO,
  NoticeBatchDeletePayload,
  NoticeForm,
  NoticeListVO,
  NoticePageQuery,
  NoticePublishedQuery,
  NoticePublishedVO,
  NoticeVO,
} from '@/types/system/notice'

const BASE = '/system/notice'

export function pageNotice(params: NoticePageQuery) {
  return request.get<ApiResponse<PageResult<NoticeListVO>>>(`${BASE}/page`, { params })
}

export function listPublishedNotice(params: NoticePublishedQuery) {
  return request.get<ApiResponse<NoticePublishedVO[]>>(`${BASE}/published`, { params })
}

export function getPublishedNotice(id: string) {
  return request.get<ApiResponse<NoticePublishedVO>>(`${BASE}/published/${id}`)
}

export function getNotice(id: string) {
  return request.get<ApiResponse<NoticeVO>>(`${BASE}/${id}`)
}

export function createNotice(data: NoticeForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateNotice(data: NoticeForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteNotice(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export function batchDeleteNotice(data: NoticeBatchDeletePayload) {
  return request.delete<ApiResponse<void>>(`${BASE}/batch`, { data })
}

export function submitNoticeApprove(id: string) {
  return request.post<ApiResponse<string>>(`${BASE}/${id}/submit-approve`)
}

export function withdrawNoticeApprove(id: string) {
  return request.post<ApiResponse<void>>(`${BASE}/${id}/withdraw`)
}

export function closeNotice(id: string) {
  return request.post<ApiResponse<void>>(`${BASE}/${id}/close`)
}

export function publishNotice(id: string) {
  return request.post<ApiResponse<void>>(`${BASE}/${id}/publish`)
}

export function unpublishNotice(id: string) {
  return request.post<ApiResponse<void>>(`${BASE}/${id}/unpublish`)
}

export function getNoticeApproveDetail(id: string) {
  return request.get<ApiResponse<NoticeApproveDetailVO>>(`${BASE}/${id}/approve-detail`)
}

export const PERM_Notice = {
  view: 'system:notice:view',
  list: 'system:notice:list',
  add: 'system:notice:add',
  update: 'system:notice:update',
  delete: 'system:notice:delete',
  submit: 'system:notice:submit',
  withdraw: 'system:notice:withdraw',
  close: 'system:notice:close',
  publish: 'system:notice:publish',
}

/** @deprecated use PERM_Notice */
export const PERM_NOTICE = PERM_Notice

export const GEN_RESOURCE_KEY = 'system.notice' as const

export const genCrudResource = defineGenCrudResource(PERM_Notice, {
  page: pageNotice,
  get: getNotice,
  create: createNotice,
  update: updateNotice,
  delete: deleteNotice,
  batchDelete: batchDeleteNotice,
})
