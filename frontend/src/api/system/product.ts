/** 产品宇宙 API */

import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  ProductForm,
  ProductManagePageQuery,
  ProductManageVO,
  ProductVO,
} from '@/types/system/product'

const BASE = '/system/product'

export function listMyProducts() {
  return request.get<ApiResponse<ProductVO[]>>(`${BASE}/mine`)
}

export function listProducts() {
  return request.get<ApiResponse<ProductVO[]>>(`${BASE}/list`)
}

export function pageProduct(params: ProductManagePageQuery) {
  return request.get<ApiResponse<PageResult<ProductManageVO>>>(`${BASE}/page`, { params })
}

export function getProduct(id: string) {
  return request.get<ApiResponse<ProductManageVO>>(`${BASE}/${id}`)
}

export function createProduct(data: ProductForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateProduct(data: ProductForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteProduct(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_Product = {
  view: 'system:product:view',
  list: 'system:product:list',
  add: 'system:product:add',
  update: 'system:product:update',
  delete: 'system:product:delete',
}

export const GEN_RESOURCE_KEY = 'system.product' as const

export const genCrudResource = defineGenCrudResource(PERM_Product, {
  page: pageProduct,
  get: getProduct,
  create: createProduct,
  update: updateProduct,
  delete: deleteProduct,
})
