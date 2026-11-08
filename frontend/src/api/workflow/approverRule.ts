/** 审批人规则 */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  WorkflowApproverRuleForm,
  WorkflowApproverRulePageQuery,
  WorkflowApproverRuleUpdateForm,
  WorkflowApproverRuleVO,
  WorkflowModelOptionVO,
} from '@/types/workflow/approverRule'

const BASE = '/workflow/approver-rule'

export function pageWorkflowApproverRule(params: WorkflowApproverRulePageQuery) {
  return request.get<ApiResponse<PageResult<WorkflowApproverRuleVO>>>(`${BASE}/page`, { params })
}

export function getWorkflowApproverRule(id: string) {
  return request.get<ApiResponse<WorkflowApproverRuleVO>>(`${BASE}/${id}`)
}

export function listWorkflowApproverRuleModelOptions() {
  return request.get<ApiResponse<WorkflowModelOptionVO[]>>(`${BASE}/model-options`)
}

export function createWorkflowApproverRule(data: WorkflowApproverRuleForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateWorkflowApproverRule(data: WorkflowApproverRuleUpdateForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteWorkflowApproverRule(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_WorkflowApproverRule = {
  view: 'workflow:approverRule:view',
  list: 'workflow:approverRule:list',
  query: 'workflow:approverRule:query',
  update: 'workflow:approverRule:update',
  delete: 'workflow:approverRule:delete',
}

export const GEN_RESOURCE_KEY = 'workflow.workflowApproverRule' as const

const PERM_WorkflowApproverRuleCrud = {
  add: PERM_WorkflowApproverRule.update,
  update: PERM_WorkflowApproverRule.update,
  delete: PERM_WorkflowApproverRule.delete,
}

export const genCrudResource = defineGenCrudResource(PERM_WorkflowApproverRuleCrud, {
  page: pageWorkflowApproverRule,
  get: getWorkflowApproverRule,
  create: createWorkflowApproverRule,
  update: updateWorkflowApproverRule,
  delete: deleteWorkflowApproverRule,
})
