import type {
  OaFieldDefBatchSavePayload,
  OaFieldDefVO,
  OaTemplateVersionVO,
  OaVersionCreatePayload,
  OaVersionPublishPayload,
} from '@/types/oa/oaPlatformTemplate'

export type {
  OaFieldDefVO,
  OaTemplateVersionVO,
  OaVersionCreatePayload,
  OaVersionPublishPayload,
  OaFieldDefBatchSavePayload,
}

export interface OaTenantTemplateVO {
  id: string
  categoryCode: string
  templateCode: string
  templateName: string
  platformTemplateId: string
  sourcePlatformTplVerId?: string
  latestVersionNo: number
  draftVersionNo?: number
  hasDraft?: boolean
  platformLatestVersionNo?: number
  platformOutdated?: boolean
  publishedTplVerId?: string
  enableStatus: string
  status: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface OaTenantTemplateDetailVO extends OaTenantTemplateVO {
  draftVersion?: OaTemplateVersionVO
  publishedVersion?: OaTemplateVersionVO
  versions?: OaTemplateVersionVO[]
  platformTemplateName?: string
  platformLatestVersionNo?: number
}

export interface OaPlatformTemplateOptionVO {
  id: string
  categoryCode: string
  templateCode: string
  templateName: string
  latestVersionNo: number
  publishedVersionId: string
}

export interface OaTenantTemplatePageQuery {
  pageNo?: number
  pageSize?: number
  categoryCode?: string
  templateCode?: string
  templateName?: string
  enableStatus?: string
  status?: string
}

export interface OaTenantTemplateForm {
  templateName: string
  status?: string
  remark?: string
}

export interface OaTenantEnablePayload {
  platformTemplateId: string
  templateName?: string
  remark?: string
}

export interface OaTenantFieldSyncDiffVO {
  changeType: 'ADDED' | 'REMOVED' | 'CHANGED' | 'KEPT_CUSTOM' | string
  fieldCode: string
  fieldName?: string
  currentSummary?: string
  afterSummary?: string
}

export interface OaTenantSyncPreviewVO {
  templateId: string
  templateName?: string
  platformLatestVersionNo?: number
  platformPublishedVersionId?: string
  currentSourcePlatformTplVerId?: string
  currentTenantVersionNo?: number
  currentTenantVersionStatus?: string
  hasChanges?: boolean
  addedCount?: number
  removedCount?: number
  changedCount?: number
  keptCustomCount?: number
  diffs?: OaTenantFieldSyncDiffVO[]
}

/** 发布齐套检查 */
export interface OaTenantPublishReadinessVO {
  ready: boolean
  fieldsReady: boolean
  modelSynced: boolean
  modelDeployed: boolean
  approversReady: boolean
  modelId?: string
  modelKey?: string
  modelVersionNo?: number
  userTaskKeys?: string[]
  missingApproverTaskKeys?: string[]
  messages?: string[]
}
