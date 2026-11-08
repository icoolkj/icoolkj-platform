/** 产品宇宙类型 */

export interface ProductVO {
  code: string
  name: string
  icon?: string
  sortNo?: number
  homePath?: string
}

export interface ProductManageVO {
  id?: string
  code?: string
  name?: string
  nameEn?: string
  icon?: string
  sortNo?: number
  status?: string
  remark?: string
  builtin?: boolean
  createdAt?: string
}

export interface ProductManagePageQuery {
  pageNo?: number
  pageSize?: number
  code?: string
  name?: string
  status?: string
}

export interface ProductForm {
  id?: string
  code?: string
  name?: string
  nameEn?: string
  icon?: string
  sortNo?: number
  status?: string
  remark?: string
}
