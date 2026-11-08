import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type {
  ChangePasswordPayload,
  UserAvatarPolicy,
  UserProfile,
  UserProfileUpdatePayload,
} from '@/types/profile'

export function fetchProfile() {
  return request.get<ApiResponse<UserProfile>>('/auth/profile').then((res) => res.data.data)
}

export function fetchAvatarPolicy() {
  return request
    .get<ApiResponse<UserAvatarPolicy>>('/auth/profile/avatar-policy')
    .then((res) => res.data.data)
}

export function updateProfile(data: UserProfileUpdatePayload) {
  return request.put<ApiResponse<UserProfile>>('/auth/profile', data).then((res) => res.data.data)
}

export function uploadProfileAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request
    .post<ApiResponse<UserProfile>>('/auth/profile/avatar', formData)
    .then((res) => res.data.data)
}

export async function changePassword(data: ChangePasswordPayload) {
  const { encryptPasswordForTransport } = await import('@/utils/passwordTransport')
  const payload = {
    oldPassword: await encryptPasswordForTransport(data.oldPassword),
    newPassword: await encryptPasswordForTransport(data.newPassword),
  }
  return request.put<ApiResponse<void>>('/auth/password', payload).then((res) => res.data.data)
}
