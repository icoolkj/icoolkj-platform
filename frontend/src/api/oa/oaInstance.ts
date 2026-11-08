import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type {
  OaInstanceApproveDetailVO,
  OaInstanceFormSavePayload,
  OaInstanceFormSchemaVO,
  OaInstanceVO,
} from '@/types/oa/oaInstance'

const BASE = '/oa/instance'

export function getOaInstanceFormSchema(categoryCode: string) {
  return request.get<ApiResponse<OaInstanceFormSchemaVO>>(
    `${BASE}/${encodeURIComponent(categoryCode)}/form-schema`,
  )
}

export function getOaInstance(id: string) {
  return request.get<ApiResponse<OaInstanceVO>>(`${BASE}/${encodeURIComponent(id)}`)
}

export function createOaInstance(categoryCode: string, data: OaInstanceFormSavePayload) {
  return request.post<ApiResponse<string>>(
    `${BASE}/${encodeURIComponent(categoryCode)}/create`,
    data,
  )
}

export function updateOaInstance(data: OaInstanceFormSavePayload) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteOaInstance(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${encodeURIComponent(id)}`)
}

export function submitOaInstanceApprove(id: string) {
  return request.post<ApiResponse<string>>(`${BASE}/${encodeURIComponent(id)}/submit-approve`)
}

export function closeOaInstance(id: string) {
  return request.post<ApiResponse<void>>(`${BASE}/${encodeURIComponent(id)}/close`)
}

export function getOaInstanceApproveDetail(id: string) {
  return request.get<ApiResponse<OaInstanceApproveDetailVO>>(
    `${BASE}/${encodeURIComponent(id)}/approve-detail`,
  )
}

export const PERM_OaInstance = {
  list: 'oa:instance:list',
  query: 'oa:instance:query',
  add: 'oa:instance:add',
  update: 'oa:instance:update',
  delete: 'oa:instance:delete',
  submit: 'oa:instance:submit',
  close: 'oa:instance:close',
  approveView: 'workflow:approve:oa:view',
}
