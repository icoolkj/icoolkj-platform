/** 流程模型 API */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  WorkflowModelDeployPayload,
  WorkflowModelDeployResultVO,
  WorkflowModelDetailVO,
  WorkflowModelForm,
  WorkflowModelPageQuery,
  WorkflowModelSyncCatalogVO,
  WorkflowModelSyncPayload,
  WorkflowModelVO,
  WorkflowModelVersionVO,
} from '@/types/workflow/model'
import type { WorkflowModelDesignDeployPayload } from '@/types/workflow/design'

const BASE = '/workflow/model'

export function pageWorkflowModel(params: WorkflowModelPageQuery) {
  return request.get<ApiResponse<PageResult<WorkflowModelVO>>>(`${BASE}/page`, { params })
}

export function getWorkflowModel(id: string) {
  return request.get<ApiResponse<WorkflowModelDetailVO>>(`${BASE}/${id}`)
}

export function listWorkflowModelVersions(id: string) {
  return request.get<ApiResponse<WorkflowModelVersionVO[]>>(`${BASE}/${id}/versions`)
}

export function createWorkflowModel(data: WorkflowModelForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateWorkflowModel(data: WorkflowModelForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deployWorkflowModel(data: WorkflowModelDeployPayload) {
  return request.post<ApiResponse<WorkflowModelDeployResultVO>>(`${BASE}/deploy`, data)
}

export function listWorkflowModelSyncCatalog() {
  return request.get<ApiResponse<WorkflowModelSyncCatalogVO[]>>(`${BASE}/sync-catalog`)
}

export function syncWorkflowModelFromPlatform(data: WorkflowModelSyncPayload) {
  return request.post<ApiResponse<string>>(`${BASE}/sync`, data)
}

export function designDeployWorkflowModel(data: WorkflowModelDesignDeployPayload) {
  return request.post<ApiResponse<WorkflowModelDeployResultVO>>(`${BASE}/design-deploy`, data)
}

export const PERM_WorkflowModel = {
  view: 'workflow:model:view',
  list: 'workflow:model:list',
  query: 'workflow:model:query',
  update: 'workflow:model:update',
  deploy: 'workflow:model:deploy',
}

export const GEN_RESOURCE_KEY = 'workflow.workflowModel' as const

const PERM_WorkflowModelCrud = {
  add: PERM_WorkflowModel.update,
  update: PERM_WorkflowModel.update,
  delete: PERM_WorkflowModel.update,
  deploy: PERM_WorkflowModel.deploy,
  query: PERM_WorkflowModel.query,
}

export const genCrudResource = defineGenCrudResource(PERM_WorkflowModelCrud, {
  page: pageWorkflowModel,
  get: getWorkflowModel,
  create: createWorkflowModel,
  update: updateWorkflowModel,
})
