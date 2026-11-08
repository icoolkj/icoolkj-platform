/** OA 平台审批模板 API — 对齐 gen businessName oaPlatformTemplate */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  OaFieldDefBatchSavePayload,
  OaPlatformTemplateDetailVO,
  OaPlatformTemplateForm,
  OaPlatformTemplatePageQuery,
  OaPlatformTemplateVO,
  OaTemplateVersionVO,
  OaVersionCreatePayload,
  OaVersionPublishPayload,
} from '@/types/oa/oaPlatformTemplate'

const BASE = '/oa/platformTemplate'

export function pageOaPlatformTemplate(params: OaPlatformTemplatePageQuery) {
  return request.get<ApiResponse<PageResult<OaPlatformTemplateVO>>>(`${BASE}/page`, { params })
}

export function getOaPlatformTemplate(id: string) {
  return request.get<ApiResponse<OaPlatformTemplateDetailVO>>(`${BASE}/${id}`)
}

export function listOaPlatformTemplateModelOptions() {
  return request.get<
    ApiResponse<{ value: string; label: string; modelKey?: string; bizType?: string }[]>
  >(`${BASE}/model-options`)
}

export function listOaPlatformTemplateVersions(id: string) {
  return request.get<ApiResponse<OaTemplateVersionVO[]>>(`${BASE}/${id}/versions`)
}

export function getOaPlatformTemplateVersion(versionId: string) {
  return request.get<ApiResponse<OaTemplateVersionVO>>(`${BASE}/version/${versionId}`)
}

export function createOaPlatformTemplate(data: OaPlatformTemplateForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateOaPlatformTemplate(data: OaPlatformTemplateForm & { id: string }) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function createOaPlatformDraftVersion(data: OaVersionCreatePayload) {
  return request.post<ApiResponse<string>>(`${BASE}/version/createDraft`, data)
}

export function saveOaPlatformFieldDefs(data: OaFieldDefBatchSavePayload) {
  return request.put<ApiResponse<void>>(`${BASE}/version/saveFields`, data)
}

export function publishOaPlatformVersion(data: OaVersionPublishPayload) {
  return request.post<ApiResponse<void>>(`${BASE}/version/publish`, data)
}

export const PERM_OaPlatformTemplate = {
  view: 'oa:platformTemplate:view',
  list: 'oa:platformTemplate:list',
  query: 'oa:platformTemplate:query',
  update: 'oa:platformTemplate:update',
  publish: 'oa:platformTemplate:publish',
}

export const GEN_RESOURCE_KEY = 'oa.oaPlatformTemplate' as const

const PERM_OaPlatformTemplateCrud = {
  ...PERM_OaPlatformTemplate,
  add: PERM_OaPlatformTemplate.update,
  delete: PERM_OaPlatformTemplate.update,
}

export const genCrudResource = defineGenCrudResource(PERM_OaPlatformTemplateCrud, {
  page: pageOaPlatformTemplate,
  get: getOaPlatformTemplate,
})
