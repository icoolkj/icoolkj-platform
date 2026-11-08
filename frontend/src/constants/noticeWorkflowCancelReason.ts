/** 公告调用工作流 cancel 的原因码（与后端 NoticeWorkflowCancelReasons 对齐） */
export const NOTICE_WORKFLOW_CANCEL_REASON = {
  withdraw: 'notice_withdraw',
  closeAfterReturn: 'notice_close_after_return',
} as const
