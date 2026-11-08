/** 工作流任务 API */
import request from '@/utils/request'
import {
  defineReadOnlyGenCrudResource,
} from '@/lib/gen-crud'
import type { GenCrudPerm } from '@/types/gen/crudApi'
import type { GenCrudResource } from '@/components/gen/registry/crud-resources/registry'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  WorkflowTaskAddSignPayload,
  WorkflowTaskCompletePayload,
  WorkflowTaskDelegatePayload,
  WorkflowTaskDetailVO,
  WorkflowTaskJumpPayload,
  WorkflowTaskPageQuery,
  WorkflowTaskRemoveSignPayload,
  WorkflowTaskTransferPayload,
  WorkflowTaskUrgePayload,
  WorkflowTaskVO,
} from '@/types/workflow/task'

const BASE = '/workflow/task'

export function pageWorkflowTodo(params: WorkflowTaskPageQuery) {
  return request.get<ApiResponse<PageResult<WorkflowTaskVO>>>(`${BASE}/todo/page`, { params })
}

export function pageWorkflowDone(params: WorkflowTaskPageQuery) {
  return request.get<ApiResponse<PageResult<WorkflowTaskVO>>>(`${BASE}/done/page`, { params })
}

export function pageWorkflowCc(params: WorkflowTaskPageQuery) {
  return request.get<ApiResponse<PageResult<WorkflowTaskVO>>>(`${BASE}/cc/page`, { params })
}

export function getWorkflowTask(taskId: string) {
  return request.get<ApiResponse<WorkflowTaskDetailVO>>(`${BASE}/${taskId}`)
}

export function completeWorkflowTask(taskId: string, data: WorkflowTaskCompletePayload) {
  return request.post<ApiResponse<void>>(`${BASE}/${taskId}/complete`, data)
}

export function transferWorkflowTask(taskId: string, data: WorkflowTaskTransferPayload) {
  return request.post<ApiResponse<void>>(`${BASE}/${taskId}/transfer`, data)
}

export function delegateWorkflowTask(taskId: string, data: WorkflowTaskDelegatePayload) {
  return request.post<ApiResponse<void>>(`${BASE}/${taskId}/delegate`, data)
}

export function jumpWorkflowTask(taskId: string, data: WorkflowTaskJumpPayload) {
  return request.post<ApiResponse<void>>(`${BASE}/${taskId}/jump`, data)
}

export function markWorkflowCcRead(taskId: string) {
  return request.post<ApiResponse<void>>(`${BASE}/${taskId}/cc-read`)
}

export function urgeWorkflowTask(taskId: string, data?: WorkflowTaskUrgePayload) {
  return request.post<ApiResponse<void>>(`${BASE}/${taskId}/urge`, data ?? {})
}

export function addSignWorkflowTask(taskId: string, data: WorkflowTaskAddSignPayload) {
  return request.post<ApiResponse<void>>(`${BASE}/${taskId}/add-sign`, data)
}

export function removeSignWorkflowTask(taskId: string, data: WorkflowTaskRemoveSignPayload) {
  return request.post<ApiResponse<void>>(`${BASE}/${taskId}/remove-sign`, data)
}

export const PERM_WorkflowTodo = {
  view: 'workflow:todo:view',
  list: 'workflow:todo:list',
  query: 'workflow:task:query',
  complete: 'workflow:task:complete',
  transfer: 'workflow:task:transfer',
  delegate: 'workflow:task:delegate',
  jump: 'workflow:task:jump',
  urge: 'workflow:task:urge',
  addSign: 'workflow:task:addSign',
  removeSign: 'workflow:task:removeSign',
}

export const PERM_WorkflowDone = {
  view: 'workflow:done:view',
  list: 'workflow:done:list',
}

export const PERM_WorkflowCc = {
  view: 'workflow:cc:view',
  list: 'workflow:cc:list',
  complete: 'workflow:task:complete',
}

export const PERM_WorkflowApproveNotice = {
  view: 'workflow:approve:notice:view',
}

const PERM_WorkflowTodoCrud: GenCrudPerm = {
  add: PERM_WorkflowTodo.list,
  update: PERM_WorkflowTodo.list,
  delete: PERM_WorkflowTodo.list,
  complete: PERM_WorkflowTodo.complete,
}

const PERM_WorkflowDoneCrud: GenCrudPerm = {
  add: PERM_WorkflowDone.list,
  update: PERM_WorkflowDone.list,
  delete: PERM_WorkflowDone.list,
}

const PERM_WorkflowCcCrud: GenCrudPerm = {
  add: PERM_WorkflowCc.list,
  update: PERM_WorkflowCc.list,
  delete: PERM_WorkflowCc.list,
  complete: PERM_WorkflowCc.complete,
}

export const GEN_CRUD_RESOURCES: Record<string, GenCrudResource> = {
  'workflow.workflowTaskTodo': defineReadOnlyGenCrudResource(
    PERM_WorkflowTodoCrud,
    pageWorkflowTodo,
  ),
  'workflow.workflowTaskDone': defineReadOnlyGenCrudResource(
    PERM_WorkflowDoneCrud,
    pageWorkflowDone,
  ),
  'workflow.workflowTaskCc': defineReadOnlyGenCrudResource(
    PERM_WorkflowCcCrud,
    pageWorkflowCc,
  ),
}
