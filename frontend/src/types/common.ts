export interface ApiResponse<T> {
  code: number
  message: string
  /** Stable i18n key when message was localized server-side */
  messageKey?: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNo: number
  pageSize: number
}

/** Excel import all-or-nothing result (matches backend ExcelImportResult). */
export interface ExcelImportResult {
  total: number
  success: number
  skipped: number
  /** True failure count (may exceed errors.length when capped). */
  errorTotal: number
  errors: ExcelImportRowError[]
}

export interface ExcelImportRowError {
  rowNumber: number
  message: string
}

import type { LayoutSettings } from '@/types/layout'

export interface LoginUser {
  userId: string
  username: string
  nickname?: string
  tenantId: string
  tenantCode: string
  permissions: string[]
  avatarFileId?: string
  avatarUrl?: string
  avatarUrlSm?: string
  avatarUrlLg?: string
  locale?: string
  theme?: string
  layout?: Partial<LayoutSettings>
  /** 初始/重置密码后需先修改密码 */
  mustChangePassword?: boolean
  /** 是否已绑定 MFA */
  mfaEnabled?: boolean
  /** 会话要求强制绑定 MFA */
  mustEnrollMfa?: boolean
}
