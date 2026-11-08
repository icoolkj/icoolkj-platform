import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'

export interface OaInstanceVO {
  id: string
  categoryCode?: string
  instanceTitle?: string
  approveStatus?: string
  processInstanceId?: string
  processDefinitionKey?: string
  starterUserId?: string
  submittedAt?: string
  finishedAt?: string
  status?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
  formData?: Record<string, unknown>
  fields?: OaFieldDefVO[]
  layoutSnapshotJson?: string
}

export interface OaInstanceFormSavePayload {
  id?: string
  formData: Record<string, unknown>
  remark?: string
}

export interface OaInstanceFormSchemaVO {
  categoryCode?: string
  templateName?: string
  tenantTemplateId?: string
  tenantTplVerId?: string
  versionNo?: number
  fields: OaFieldDefVO[]
  appendRemark?: boolean
  layoutSnapshotJson?: string
}

export interface OaInstanceApproveDetailVO {
  document?: OaInstanceVO
  taskId?: string
  processInstanceId?: string
  approveStatus?: string
  instance?: Record<string, unknown>
  fields?: OaFieldDefVO[]
  formData?: Record<string, unknown>
}
