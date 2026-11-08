/** 工作流任务类型 */

export interface WorkflowTaskVO {
  taskId?: string
  taskName?: string
  taskDefinitionKey?: string
  processInstanceId?: string
  processDefinitionKey?: string
  processDefinitionName?: string
  bizType?: string
  bizId?: string
  bizTitle?: string
  starterUserId?: string
  starterUserName?: string
  formRoute?: string
  createTime?: string
  dueDate?: string
  endTime?: string
  approveResult?: string
  approved?: boolean
  /** 是否抄送任务 */
  ccTask?: boolean
  /** 抄送是否已读 */
  ccRead?: boolean
}

export interface WorkflowParallelAssigneeVO {
  taskId?: string
  userId?: string
  userName?: string
}

export interface WorkflowTaskDetailVO extends WorkflowTaskVO {
  commentList?: WorkflowCommentVO[]
  variables?: Record<string, unknown>
  rejectMode?: 'end' | 'previous' | 'starter' | 'starterEdit'
  allowTransfer?: boolean
  allowDelegate?: boolean
  allowAddSign?: boolean
  /** 会签/或签且允许加签时，可减签其他活跃办理人 */
  allowRemoveSign?: boolean
  parallelAssignees?: WorkflowParallelAssigneeVO[]
}

export interface WorkflowCommentVO {
  userId?: string
  userName?: string
  message?: string
  time?: string
}

export interface WorkflowTaskPageQuery {
  pageNo?: number
  pageSize?: number
  bizTitle?: string
  bizType?: string
  processDefinitionKey?: string
  taskName?: string
  startTimeBegin?: string
  startTimeEnd?: string
}

export interface WorkflowTaskCompletePayload {
  approved: boolean
  comment?: string
  /** 仅节点 fieldPerms=W 的字段会被后端合并 */
  formData?: Record<string, unknown>
}

export interface WorkflowTaskTransferPayload {
  targetUserId: string
  comment?: string
}

export interface WorkflowTaskDelegatePayload {
  targetUserId: string
  comment?: string
  delegateUntil?: string
}

export interface WorkflowTaskJumpPayload {
  targetActivityId: string
  comment?: string
}

export interface WorkflowTaskUrgePayload {
  comment?: string
}

export interface WorkflowTaskAddSignPayload {
  mode: 'before' | 'after'
  userIds: string[]
  comment?: string
}

export interface WorkflowTaskRemoveSignPayload {
  userIds: string[]
  comment?: string
}
