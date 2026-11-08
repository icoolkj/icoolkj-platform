import type { GenViewFieldCondition, GenViewRowAction, GenViewWorkflowWhen } from '@/types/tool/genView'
import {
  canCloseWorkflowBizRow,
  canDeleteWorkflowBizRow,
  canEditWorkflowBizRow,
  canSubmitWorkflowBizRow,
  canWithdrawWorkflowBizRow,
} from '@/composables/useWorkflowBizRowActions'

function matchesCondition(row: Record<string, unknown>, condition: GenViewFieldCondition) {
  const current = String(row[condition.field] ?? '')
  return condition.values.includes(current)
}

function matchesAllConditions(row: Record<string, unknown>, conditions: GenViewFieldCondition[]) {
  return conditions.every((c) => matchesCondition(row, c))
}

function evaluateWorkflowWhen(
  preset: GenViewWorkflowWhen,
  row: Record<string, unknown>,
  statusField = 'approveStatus',
): boolean {
  switch (preset) {
    case 'canEdit':
      return canEditWorkflowBizRow(row, statusField)
    case 'canSubmit':
      return canSubmitWorkflowBizRow(row, statusField)
    case 'canWithdraw':
      return canWithdrawWorkflowBizRow(row, statusField)
    case 'canDelete':
      return canDeleteWorkflowBizRow(row)
    case 'canClose':
      return canCloseWorkflowBizRow(row, statusField)
    default:
      return true
  }
}

/** 声明式行操作可见性（workflowWhen / showWhen / showWhenAll） */
export function evaluateRowActionVisible(
  action: GenViewRowAction,
  row: Record<string, unknown>,
  options?: { statusField?: string },
): boolean {
  if (action.showWhenAll?.length && !matchesAllConditions(row, action.showWhenAll)) {
    return false
  }
  if (action.showWhen && !matchesCondition(row, action.showWhen)) {
    return false
  }
  if (action.workflowWhen && !evaluateWorkflowWhen(action.workflowWhen, row, options?.statusField)) {
    return false
  }
  return true
}

/** 表格多选过滤（page.selectionFilter / toolbar batchDelete） */
export function evaluateSelectionFilter(
  preset: string | undefined,
  row: Record<string, unknown>,
): boolean {
  if (!preset || preset === 'none') return true
  if (preset === 'workflowCanDelete') return canDeleteWorkflowBizRow(row)
  if (preset === 'deletable') return (row as { deletable?: unknown }).deletable !== false
  return true
}
