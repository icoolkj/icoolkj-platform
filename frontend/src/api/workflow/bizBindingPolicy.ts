/** 业务绑定策略 */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  WorkflowBizBindingPolicyForm,
  WorkflowBizBindingPolicyPageQuery,
  WorkflowBizBindingPolicyUpdateForm,
  WorkflowBizBindingPolicyVO,
} from '@/types/workflow/bizBindingPolicy'

const BASE = '/workflow/biz-binding-policy'

export function pageWorkflowBizBindingPolicy(params: WorkflowBizBindingPolicyPageQuery) {
  return request.get<ApiResponse<PageResult<WorkflowBizBindingPolicyVO>>>(`${BASE}/page`, {
    params,
  })
}

export function getWorkflowBizBindingPolicy(id: string) {
  return request.get<ApiResponse<WorkflowBizBindingPolicyVO>>(`${BASE}/${id}`)
}

export function createWorkflowBizBindingPolicy(data: WorkflowBizBindingPolicyForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateWorkflowBizBindingPolicy(data: WorkflowBizBindingPolicyUpdateForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteWorkflowBizBindingPolicy(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_WorkflowBizBindingPolicy = {
  view: 'workflow:bizBindingPolicy:view',
  list: 'workflow:bizBindingPolicy:list',
  query: 'workflow:bizBindingPolicy:query',
  update: 'workflow:bizBindingPolicy:update',
  delete: 'workflow:bizBindingPolicy:delete',
}

export const GEN_RESOURCE_KEY = 'workflow.workflowBizBindingPolicy' as const

const PERM_WorkflowBizBindingPolicyCrud = {
  add: PERM_WorkflowBizBindingPolicy.update,
  update: PERM_WorkflowBizBindingPolicy.update,
  delete: PERM_WorkflowBizBindingPolicy.delete,
}

export const genCrudResource = defineGenCrudResource(PERM_WorkflowBizBindingPolicyCrud, {
  page: pageWorkflowBizBindingPolicy,
  get: getWorkflowBizBindingPolicy,
  create: createWorkflowBizBindingPolicy,
  update: updateWorkflowBizBindingPolicy,
  delete: deleteWorkflowBizBindingPolicy,
})
