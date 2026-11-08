export interface FileProfileVO {
  profileCode: string
  profileName: string
  profileNameI18n?: Record<string, string>
  categoryTag?: string
  status?: string
  remark?: string
  ruleTenantId?: string
  tenantOverride?: boolean
  allowedExts?: string[]
  allowedContentTypes?: string[]
  required?: boolean
  minNum?: number
  maxNum?: number
  minSizeBytes?: number
  maxSizeBytes?: number
  sortNo?: number
}

export interface FileProfileUpsertRequest {
  targetTenantId?: string
  profileCode: string
  profileName?: string
  profileNameI18n?: Record<string, string>
  categoryTag?: string
  status?: string
  remark?: string
  allowedExts?: string
  allowedContentTypes?: string
  required?: string
  minNum?: number
  maxNum?: number
  minSizeBytes?: number
  maxSizeBytes?: number
  sortNo?: number
}

export interface FileProfileBatchDeleteRequest {
  targetTenantId?: string
  profileCodes: string[]
}

export interface FileCatalogItemVO {
  profileCode: string
  profileName?: string
  required?: string
  minNum?: number
  maxNum?: number
  sortNo?: number
  status?: string
  /** role → priority；停用槽位不参与 chrome 解析 */
  chromeRoles?: Record<string, number>
  resolvedProfile?: FileProfileVO
}

export interface FileCatalogVO {
  catalogCode: string
  catalogName: string
  catalogNameI18n?: Record<string, string>
  status?: string
  remark?: string
  tenantOverride?: boolean
  items?: FileCatalogItemVO[]
}

export interface FileCatalogItemUpsertRequest {
  profileCode: string
  required?: string
  minNum?: number | null
  maxNum?: number | null
  sortNo?: number
  status?: string
  chromeRoles?: Record<string, number>
  remark?: string
}

export interface FileCatalogUpsertRequest {
  targetTenantId?: string
  catalogCode: string
  catalogName: string
  catalogNameI18n?: Record<string, string>
  status?: string
  remark?: string
  items?: FileCatalogItemUpsertRequest[]
}
