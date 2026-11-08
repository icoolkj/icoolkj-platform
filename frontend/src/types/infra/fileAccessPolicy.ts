export interface FileAccessPolicyVO {
  id?: string
  bizTypePattern: string
  authMode: string
  tenantScope: string
  visibility: string
  anonPurposes?: string
  permAnyOf?: string
  sortNo?: number
  enabled?: string
  remark?: string
  updatedAt?: string
  pinned?: boolean
}

export interface FileAccessPolicyUpsertRequest {
  id?: string
  bizTypePattern: string
  authMode: string
  tenantScope: string
  visibility: string
  anonPurposes?: string
  permAnyOf?: string
  sortNo?: number
  enabled?: string
  remark?: string
}
