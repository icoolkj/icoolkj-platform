import request, { type SilentRequestConfig } from '@/utils/request'
import {
  defineGenCrudResource,
  wrapExportFileAsync,
  wrapImportFileAsync,
} from '@/lib/gen-crud'
import type { ApiResponse, ExcelImportResult, PageResult } from '@/types/common'
import type { UserForm, UserPageQuery, UserVO } from '@/types/system/user'
import type { UserAvatarPolicy, UserProfile } from '@/types/profile'

const BASE = '/system/user'

export function pageUser(params: UserPageQuery) {
  return request.get<ApiResponse<PageResult<UserVO>>>(`${BASE}/page`, { params })
}

export function getUser(id: string, config?: SilentRequestConfig) {
  return request.get<ApiResponse<UserVO>>(`${BASE}/${id}`, config)
}

export async function createUser(data: UserForm) {
  const payload = { ...data }
  if (typeof payload.password === 'string' && payload.password.trim()) {
    const { encryptPasswordForTransport } = await import('@/utils/passwordTransport')
    payload.password = await encryptPasswordForTransport(payload.password)
  }
  return request.post<ApiResponse<string>>(`${BASE}/create`, payload)
}

export function updateUser(data: UserForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteUser(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export function importUser(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<ExcelImportResult>>(`${BASE}/import`, formData)
}

/** Async import �?returns taskId; poll GET /infra/asyncTask/mine/{id} for resultData. */
export function importUserAsync(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<{ taskId: string }>>(`${BASE}/import-async`, formData)
}

/** Async export �?returns taskId; poll mine/{id} then download staged fileId. */
export function exportUserAsync(params: UserPageQuery) {
  return request.post<ApiResponse<{ taskId: string }>>(`${BASE}/export-async`, null, { params })
}

export function getUserRoleIds(userId: string) {
  return request.get<ApiResponse<string[]>>(`${BASE}/${userId}/roleIds`)
}

export function assignUserRoles(userId: string, roleIds: string[]) {
  return request.put<ApiResponse<void>>(`${BASE}/${userId}/roles`, { roleIds })
}

/** 重置为系统标准初始密码（或传�?newPassword）；下次登录须改�?*/
export async function resetUserPassword(userId: string, newPassword?: string) {
  let body: { newPassword?: string } = {}
  if (newPassword?.trim()) {
    const { encryptPasswordForTransport } = await import('@/utils/passwordTransport')
    body = { newPassword: await encryptPasswordForTransport(newPassword) }
  }
  return request.put<ApiResponse<void>>(`${BASE}/${userId}/password/reset`, body)
}

/** 解除登录锁定（清 DB + Redis 失败计数，并踢会话） */
export function unlockUser(userId: string) {
  return request.put<ApiResponse<void>>(`${BASE}/${userId}/unlock`)
}

export function fetchUserAvatarPolicy() {
  return request
    .get<ApiResponse<UserAvatarPolicy>>(`${BASE}/avatar-policy`)
    .then((res) => res.data.data)
}

export function uploadUserAvatar(userId: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request
    .post<ApiResponse<UserProfile>>(`${BASE}/${userId}/avatar`, formData)
    .then((res) => res.data.data)
}

export const PERM_User = {
  view: 'system:user:view',
  list: 'system:user:list',
  add: 'system:user:add',
  update: 'system:user:update',
  delete: 'system:user:delete',
  export: 'system:user:export',
  import: 'system:user:import',
  resetPassword: 'system:user:resetPassword',
  unlock: 'system:user:unlock',
}

/** @deprecated use PERM_User */
export const PERM_USER = PERM_User

export const GEN_RESOURCE_KEY = 'system.user' as const

export const genCrudResource = defineGenCrudResource(PERM_User, {
  page: pageUser,
  get: getUser,
  create: createUser,
  update: updateUser,
  delete: deleteUser,
  exportFileAsync: wrapExportFileAsync(exportUserAsync),
  importFileAsync: wrapImportFileAsync(importUserAsync),
  importFile: async (file) => {
    const res = await importUser(file)
    return res.data.data
  },
})
