export interface OaFieldDefVO {
  id?: string
  fieldCode: string
  fieldName: string
  fieldAlias?: string
  fieldType: string
  componentType?: string
  dictType?: string
  fieldRequired?: string
  sortNo?: number
  fieldGroup?: string
  colSpan?: 1 | 2
  defaultValue?: string
  maxLength?: number
  platformLocked?: string
  usedInCondition?: string
  usedInList?: string
  promoteSlot?: string
  listSlotSync?: string
  originType?: string
  status?: string
  remark?: string
  placeholder?: string
  fieldPerm?: 'R' | 'W' | 'H'
}

export interface OaTemplateVersionVO {
  id: string
  templateId: string
  versionNo: number
  versionStatus: string
  wfModelId?: string
  wfModelVersionNo?: number
  platformTplVerId?: string
  publishedAt?: string
  remark?: string
  createdAt?: string
  fields?: OaFieldDefVO[]
  layoutJson?: string
  layoutSnapshotJson?: string
}

export interface OaPlatformTemplateVO {
  id: string
  categoryCode: string
  templateCode: string
  templateName: string
  wfModelId?: string
  latestVersionNo: number
  draftVersionNo?: number
  hasDraft?: boolean
  status: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface OaPlatformTemplateDetailVO extends OaPlatformTemplateVO {
  draftVersion?: OaTemplateVersionVO
  publishedVersion?: OaTemplateVersionVO
  versions?: OaTemplateVersionVO[]
}

export interface OaPlatformTemplatePageQuery {
  pageNo?: number
  pageSize?: number
  categoryCode?: string
  templateCode?: string
  templateName?: string
  status?: string
}

export interface OaPlatformTemplateForm {
  categoryCode: string
  templateCode: string
  templateName: string
  wfModelId?: string
  status?: string
  remark?: string
}

export interface OaVersionCreatePayload {
  templateId: string
  wfModelId?: string
  remark?: string
}

export interface OaVersionPublishPayload {
  versionId: string
}

export interface OaFieldDefBatchSavePayload {
  versionId: string
  fields: OaFieldDefVO[]
  layoutJson?: string | null
}
