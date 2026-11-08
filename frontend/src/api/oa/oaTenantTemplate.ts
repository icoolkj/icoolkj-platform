/** OA 审批模板 API — 对齐 gen businessName oaTenantTemplate */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  OaFieldDefBatchSavePayload,
  OaFieldDefVO,
  OaPlatformTemplateOptionVO,
  OaTemplateVersionVO,
  OaTenantEnablePayload,
  OaTenantPublishReadinessVO,
  OaTenantSyncPreviewVO,
  OaTenantTemplateDetailVO,
  OaTenantTemplateForm,
  OaTenantTemplatePageQuery,
  OaTenantTemplateVO,
  OaVersionCreatePayload,
  OaVersionPublishPayload,
} from '@/types/oa/oaTenantTemplate'

export type { OaFieldDefVO, OaTemplateVersionVO }

const BASE = '/oa/tenantTemplate'

export function pageOaTenantTemplate(params: OaTenantTemplatePageQuery) {
  return request.get<ApiResponse<PageResult<OaTenantTemplateVO>>>(`${BASE}/page`, { params })
}

export function listOaPlatformTemplateOptions() {
  return request.get<ApiResponse<OaPlatformTemplateOptionVO[]>>(`${BASE}/platformOptions`)
}

export function getOaTenantTemplate(id: string) {
  return request.get<ApiResponse<OaTenantTemplateDetailVO>>(`${BASE}/${id}`)
}

export function listOaTenantTemplateVersions(id: string) {
  return request.get<ApiResponse<OaTemplateVersionVO[]>>(`${BASE}/${id}/versions`)
}

export function getOaTenantTemplateVersion(versionId: string) {
  return request.get<ApiResponse<OaTemplateVersionVO>>(`${BASE}/version/${versionId}`)
}

export function enableOaTenantTemplate(data: OaTenantEnablePayload) {
  return request.post<ApiResponse<string>>(`${BASE}/enable`, data)
}

export function updateOaTenantTemplate(data: OaTenantTemplateForm & { id: string }) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function createOaTenantDraftVersion(data: OaVersionCreatePayload) {
  return request.post<ApiResponse<string>>(`${BASE}/version/createDraft`, data)
}

export function previewOaTenantSyncFromPlatform(id: string) {
  return request.get<ApiResponse<OaTenantSyncPreviewVO>>(`${BASE}/${id}/syncPreview`)
}

export function syncOaTenantTemplateFromPlatform(id: string) {
  return request.post<ApiResponse<string>>(`${BASE}/${id}/syncFromPlatform`)
}

export function saveOaTenantFieldDefs(data: OaFieldDefBatchSavePayload) {
  return request.put<ApiResponse<void>>(`${BASE}/version/saveFields`, data)
}

export function getOaTenantPublishReadiness(versionId: string) {
  return request.get<ApiResponse<OaTenantPublishReadinessVO>>(
    `${BASE}/version/${versionId}/publish-readiness`,
  )
}

export function publishOaTenantVersion(data: OaVersionPublishPayload) {
  return request.post<ApiResponse<void>>(`${BASE}/version/publish`, data)
}

export const PERM_OaTenantTemplate = {
  view: 'oa:tenantTemplate:view',
  list: 'oa:tenantTemplate:list',
  query: 'oa:tenantTemplate:query',
  enable: 'oa:tenantTemplate:enable',
  update: 'oa:tenantTemplate:update',
  publish: 'oa:tenantTemplate:publish',
}

export const GEN_RESOURCE_KEY = 'oa.oaTenantTemplate' as const

const PERM_OaTenantTemplateCrud = {
  ...PERM_OaTenantTemplate,
  add: PERM_OaTenantTemplate.update,
  delete: PERM_OaTenantTemplate.update,
}

export const genCrudResource = defineGenCrudResource(PERM_OaTenantTemplateCrud, {
  page: pageOaTenantTemplate,
  get: getOaTenantTemplate,
  update: updateOaTenantTemplate,
})
