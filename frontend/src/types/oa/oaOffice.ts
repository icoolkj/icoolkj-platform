export interface OaInstanceListVO {
  id: string
  categoryCode?: string
  instanceTitle?: string
  approveStatus?: string
  processInstanceId?: string
  starterUserId?: string
  submittedAt?: string
  finishedAt?: string
  createdAt?: string
  updatedAt?: string
  remark?: string
  /** Once any workflow cycle exists, delete is blocked; close instead. */
  hasApprovalHistory?: boolean
  /** 版本快照 allowWithdraw；非审批中为 false */
  allowWithdraw?: boolean
}

export interface OaMyInstancePageQuery {
  pageNo?: number
  pageSize?: number
  instanceTitle?: string
  categoryCode?: string
  approveStatus?: string
}
