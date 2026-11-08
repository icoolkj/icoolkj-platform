/** AUTO-GENERATED — 岗位表 API */
import request, { type SilentRequestConfig } from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type { PostForm, PostPageQuery, PostVO } from '@/types/system/post'

const BASE = '/system/post'

export function pagePost(params: PostPageQuery, config?: SilentRequestConfig) {
  return request.get<ApiResponse<PageResult<PostVO>>>(`${BASE}/page`, { ...config, params })
}

export function getPost(id: string, config?: SilentRequestConfig) {
  return request.get<ApiResponse<PostVO>>(`${BASE}/${id}`, config)
}

export function createPost(data: PostForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updatePost(data: PostForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deletePost(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_Post = {
  view: 'system:post:view',
  list: 'system:post:list',
  add: 'system:post:add',
  update: 'system:post:update',
  delete: 'system:post:delete',
}

/** @deprecated use PERM_Post */
export const PERM_POST = PERM_Post

export const GEN_RESOURCE_KEY = 'system.post' as const

export const genCrudResource = defineGenCrudResource(PERM_Post, {
  page: pagePost,
  get: getPost,
  create: createPost,
  update: updatePost,
  delete: deletePost,
})
