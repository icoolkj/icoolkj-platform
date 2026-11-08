/** 工作流 cancel 原因码（与后端 WorkflowCancelReasons 对齐） */
export const WORKFLOW_CANCEL_REASON = {
  starterWithdraw: 'starter_withdraw',
  default: 'cancelled',
} as const
