/**
 * 业务列表 / 已发申请行操作可见性（与 WorkflowBizApproveStatus 对齐）。
 *
 * - 业务列表（公告、OA 草稿）：看 approveStatus → 关闭/编辑/提交
 * - 已发申请（工作流）：看流程 status → 仅撤回；关闭不在此页
 */
export function hasWorkflowBizHistory(row: Record<string, unknown>): boolean {
  return row.hasApprovalHistory === true || !!row.processInstanceId
}

export function canDeleteWorkflowBizRow(row: Record<string, unknown>): boolean {
  return !hasWorkflowBizHistory(row)
}

/** 业务列表：有历史且非运行中/已关闭 → 可关闭（写业务表 approve_status） */
export function canCloseWorkflowBizRow(row: Record<string, unknown>, statusField = 'approveStatus'): boolean {
  if (!hasWorkflowBizHistory(row)) return false
  const status = String(row[statusField] ?? '')
  return status !== 'running' && status !== 'closed' && status !== ''
}

export function canEditWorkflowBizRow(row: Record<string, unknown>, statusField = 'approveStatus'): boolean {
  const status = String(row[statusField] ?? 'draft')
  return status === 'draft' || status === 'rejected' || status === 'cancelled' || status === 'returned' || status === ''
}

export function canSubmitWorkflowBizRow(row: Record<string, unknown>, statusField = 'approveStatus'): boolean {
  const status = String(row[statusField] ?? 'draft')
  if (status === 'returned') return true
  return status === 'draft' || status === 'rejected' || status === 'cancelled' || status === ''
}

export function canWithdrawWorkflowBizRow(row: Record<string, unknown>, statusField = 'approveStatus'): boolean {
  return String(row[statusField] ?? '') === 'running' && row.allowWithdraw !== false
}

/** 已发申请撤回：统一走 workflow cancel，看流程 running */
export function canWithdrawSubmittedInstanceRow(row: Record<string, unknown>): boolean {
  return String(row.status ?? '') === 'running' && row.allowWithdraw !== false
}
