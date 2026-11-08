/** 工作流实例 API */
import request from '@/utils/request'
import {
  defineReadOnlyGenCrudResource,
} from '@/lib/gen-crud'
import type { GenCrudPerm } from '@/types/gen/crudApi'
import type { GenCrudResource } from '@/components/gen/registry/crud-resources/registry'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  WorkflowInstanceCancelPayload,
  WorkflowInstanceDetailVO,
  WorkflowInstancePageQuery,
  WorkflowInstanceVO,
} from '@/types/workflow/instance'

const BASE = '/workflow/instance'

/** 已发申请：当前用户发起的业务单据（每 biz 仅最新一轮主流程） */
export function pageWorkflowSubmitted(params: WorkflowInstancePageQuery) {
  return request.get<ApiResponse<PageResult<WorkflowInstanceVO>>>(`${BASE}/submitted/page`, { params })
}

export function pageWorkflowTenant(params: WorkflowInstancePageQuery) {
  return request.get<ApiResponse<PageResult<WorkflowInstanceVO>>>(`${BASE}/tenant/page`, { params })
}

export function getWorkflowInstance(instanceId: string) {
  return request.get<ApiResponse<WorkflowInstanceDetailVO>>(`${BASE}/${instanceId}`)
}

export function cancelWorkflowInstance(instanceId: string) {
  return request.post<ApiResponse<void>>(`${BASE}/${instanceId}/cancel`)
}

export function terminateWorkflowInstance(
  instanceId: string,
  data?: WorkflowInstanceCancelPayload,
) {
  return request.post<ApiResponse<void>>(`${BASE}/${instanceId}/terminate`, data ?? {})
}

export const PERM_WorkflowSubmitted = {
  view: 'workflow:submitted:view',
  list: 'workflow:submitted:list',
  cancel: 'workflow:instance:cancel',
  query: 'workflow:instance:query',
}

export const PERM_WorkflowMonitor = {
  view: 'workflow:monitor:view',
  list: 'workflow:monitor:list',
  query: 'workflow:instance:query',
  terminate: 'workflow:instance:terminate',
}

const PERM_WorkflowSubmittedCrud: GenCrudPerm = {
  query: PERM_WorkflowSubmitted.list,
  cancel: PERM_WorkflowSubmitted.cancel,
}

const PERM_WorkflowMonitorCrud: GenCrudPerm = {
  add: PERM_WorkflowMonitor.list,
  update: PERM_WorkflowMonitor.list,
  delete: PERM_WorkflowMonitor.list,
  terminate: PERM_WorkflowMonitor.terminate,
}

export const GEN_CRUD_RESOURCES: Record<string, GenCrudResource> = {
  'workflow.officeSubmitted': defineReadOnlyGenCrudResource(
    PERM_WorkflowSubmittedCrud,
    pageWorkflowSubmitted,
  ),
  'workflow.workflowMonitor': defineReadOnlyGenCrudResource(
    PERM_WorkflowMonitorCrud,
    pageWorkflowTenant,
  ),
}
