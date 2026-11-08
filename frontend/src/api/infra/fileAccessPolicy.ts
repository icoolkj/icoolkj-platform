/** 文件访问策略 API（按 biz_type_pattern / 槽位�?*/
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type {
  FileAccessPolicyUpsertRequest,
  FileAccessPolicyVO,
} from '@/types/infra/fileAccessPolicy'

const BASE = '/infra/file-access-policy'

export function listFileAccessPolicies() {
  return request.get<ApiResponse<FileAccessPolicyVO[]>>(`${BASE}/list`)
}

export function listEffectiveFileAccessPolicies() {
  return request.get<ApiResponse<FileAccessPolicyVO[]>>(`${BASE}/effective`)
}

export function getFileAccessPolicy(id: string) {
  return request.get<ApiResponse<FileAccessPolicyVO>>(`${BASE}/${encodeURIComponent(id)}`)
}

export function upsertFileAccessPolicy(data: FileAccessPolicyUpsertRequest) {
  return request.put<ApiResponse<void>>(BASE, data)
}

export function deleteFileAccessPolicy(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${encodeURIComponent(id)}`)
}

export function refreshFileAccessPolicyRegistry() {
  return request.post<ApiResponse<void>>(`${BASE}/refresh`)
}

export const PERM_FileAccessPolicy = {
  view: 'infra:fileAccessPolicy:view',
  query: 'infra:fileAccessPolicy:query',
  add: 'infra:fileAccessPolicy:add',
  update: 'infra:fileAccessPolicy:update',
  delete: 'infra:fileAccessPolicy:delete',
}
