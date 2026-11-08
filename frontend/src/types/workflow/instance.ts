/** 工作流实例类型 */

export interface WorkflowInstanceVO {
  instanceId?: string
  processDefinitionKey?: string
  processDefinitionName?: string
  bizType?: string
  bizId?: string
  bizTitle?: string
  status?: string
  starterUserId?: string
  starterUserName?: string
  startTime?: string
  endTime?: string
  currentTaskName?: string
  currentAssigneeUserNames?: string
  assigneeUnresolved?: boolean
  formRoute?: string
  /** 运行态 allowWithdraw；非 running 为 false */
  allowWithdraw?: boolean
  /**
   * 业务表审批/归档状态（与流程 status 分离）。
   * 关闭只改此字段；已关闭时已发申请不应再显示「关闭」。
   */
  bizApproveStatus?: string
}

export interface WorkflowHistoricTaskVO {
  taskId?: string
  taskName?: string
  taskDefinitionKey?: string
  nodeStatus?: string
  assigneeUserId?: string
  assigneeUserName?: string
  approved?: boolean
  comment?: string
  startTime?: string
  endTime?: string
  cycleNo?: number
  processInstanceId?: string
  /** transfer / delegate / withdraw / jump …；空=普通审批 */
  actionType?: string
  /** 系统动作原因码；前端 gen i18n 解析，不进 sys_i18n_message */
  reasonCode?: string
  toUserId?: string
  toUserName?: string
}

export interface WorkflowProcessCycleVO {
  cycleNo?: number
  processInstanceId?: string
  status?: string
  starterUserId?: string
  starterUserName?: string
  startTime?: string
  endTime?: string
}

export interface WorkflowProgressNodeVO {
  nodeKey?: string
  nodeName?: string
  nodeStatus?: 'pending' | 'active' | 'completed' | 'rejected' | 'skipped' | string
  assigneeUserId?: string
  assigneeUserName?: string
  approved?: boolean
  comment?: string
  taskId?: string
  startTime?: string
  endTime?: string
}

export interface WorkflowInstanceDetailVO extends WorkflowInstanceVO {
  tasks?: WorkflowHistoricTaskVO[]
  /**
   * 实例版本 design 快照；审批流程图优先 JSON 渲染，再用 progressNodes 染色。
   */
  designJson?: string
  progressNodes?: WorkflowProgressNodeVO[]
  processCycles?: WorkflowProcessCycleVO[]
  currentTaskId?: string
  variables?: Record<string, unknown>
  /** 版本快照 behavior.allowWithdraw */
  allowWithdraw?: boolean
  /** 版本快照 behavior.commentRequired */
  commentRequired?: boolean
  /** 版本快照 notify.onUrge */
  onUrge?: boolean
}

export interface WorkflowInstancePageQuery {
  pageNo?: number
  pageSize?: number
  bizTitle?: string
  bizType?: string
  status?: string
  /** Y = 仅无人办理 */
  unresolvedOnly?: string
}

export interface WorkflowInstanceCancelPayload {
  reason?: string
}
