/** OA cancel 原因码（与后端 OaWorkflowCancelReasons 对齐；withdraw 仅历史时间线） */
export const OA_WORKFLOW_CANCEL_REASON = {
  /** @deprecated 运行中撤回已改 starter_withdraw；保留给旧流转记录 */
  withdraw: 'oa_instance_withdraw',
  closeAfterReturn: 'oa_close_after_return',
} as const
