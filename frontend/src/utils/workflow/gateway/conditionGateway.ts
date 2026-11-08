import type { BranchLabelFns } from '@/types/workflow/design'
import {
  ensureConditionBranches,
  renumberConditionBranchLabels,
  validateConditionBranches,
  type ConditionValidationIssue,
  type WorkflowDesignBranch,
  type WorkflowDesignNode,
} from '@/types/workflow/design'
import { isConditionLikeGateway } from './gatewayTypes'

export function getConditionBranches(node: WorkflowDesignNode): WorkflowDesignBranch[] {
  return ensureConditionBranches(node).branches || []
}

export function countYesBranches(branches: WorkflowDesignBranch[]): number {
  return branches.filter((b) => !b.isDefault).length
}

export function yesBranchPriority(branches: WorkflowDesignBranch[], branchId: string): number {
  const list = branches.filter((b) => !b.isDefault)
  const idx = list.findIndex((b) => b.id === branchId)
  return idx >= 0 ? idx + 1 : 1
}

export function syncRootConditionExpression(branches: WorkflowDesignBranch[]): string {
  return branches.find((b) => !b.isDefault)?.expression?.trim() || ''
}

export function newConditionBranchId(): string {
  return `br${Date.now().toString(36).slice(-4)}`
}

/** 在默认分支前插入一条空条件分支 */
export function insertConditionBranch(
  root: WorkflowDesignNode,
  labels: BranchLabelFns,
  branchName: string,
): { node: WorkflowDesignNode; branchId: string } {
  const ensured = ensureConditionBranches({ ...root })
  const branches = [...(ensured.branches || [])]
  const defIdx = branches.findIndex((b) => b.isDefault)
  const branchId = newConditionBranchId()
  const inserted: WorkflowDesignBranch = {
    id: branchId,
    name: branchName,
    expression: '',
    isDefault: false,
    nodes: [],
  }
  if (defIdx >= 0) branches.splice(defIdx, 0, inserted)
  else branches.push(inserted)
  const next = renumberConditionBranchLabels(branches, labels)
  return {
    node: { ...ensured, branches: next },
    branchId,
  }
}

export type ConditionBranchRemoveResult =
  { ok: true; node: WorkflowDesignNode } | { ok: false; reason: 'default' | 'min_one' }

export function removeConditionBranch(
  root: WorkflowDesignNode,
  branchId: string,
  labels: BranchLabelFns,
): ConditionBranchRemoveResult {
  const ensured = ensureConditionBranches({ ...root })
  const target = ensured.branches?.find((b) => b.id === branchId)
  if (!target || target.isDefault) return { ok: false, reason: 'default' }
  if (countYesBranches(ensured.branches || []) <= 1) {
    return { ok: false, reason: 'min_one' }
  }
  const branches = renumberConditionBranchLabels(
    (ensured.branches || []).filter((b) => b.id !== branchId),
    labels,
  )
  return {
    ok: true,
    node: {
      ...ensured,
      branches,
      conditionExpression: syncRootConditionExpression(branches),
    },
  }
}

export function moveConditionBranch(
  root: WorkflowDesignNode,
  branchId: string,
  dir: -1 | 1,
  labels: BranchLabelFns,
): WorkflowDesignNode | null {
  const ensured = ensureConditionBranches({ ...root })
  const yes = (ensured.branches || []).filter((b) => !b.isDefault)
  const def = (ensured.branches || []).filter((b) => b.isDefault)
  const idx = yes.findIndex((b) => b.id === branchId)
  const j = idx + dir
  if (idx < 0 || j < 0 || j >= yes.length) return null
  const copy = [...yes]
  ;[copy[idx], copy[j]] = [copy[j], copy[idx]]
  const branches = renumberConditionBranchLabels([...copy, ...def], labels)
  return {
    ...ensured,
    branches,
    conditionExpression: syncRootConditionExpression(branches),
  }
}

export type ApplyConditionBranchCapInput = {
  root: WorkflowDesignNode
  branchId: string
  name: string
  expression: string
  labels: BranchLabelFns
}

export type ApplyConditionBranchCapResult =
  | { ok: true; node: WorkflowDesignNode }
  | { ok: false; reason: 'empty_expression' | 'invalid_range'; issue?: ConditionValidationIssue }

export function applyConditionBranchCap(
  input: ApplyConditionBranchCapInput,
): ApplyConditionBranchCapResult {
  const ensured = ensureConditionBranches({ ...input.root })
  const target = ensured.branches?.find((b) => b.id === input.branchId)
  if (!target) return { ok: false, reason: 'empty_expression' }

  const isDefault = !!target.isDefault
  const expr = input.expression.trim()
  if (!isDefault && !expr) return { ok: false, reason: 'empty_expression' }

  const branches = renumberConditionBranchLabels(
    (ensured.branches || []).map((b) => {
      if (b.id !== input.branchId) return b
      return {
        ...b,
        name: input.name.trim() || b.name,
        expression: isDefault ? '' : expr,
      }
    }),
    input.labels,
  )

  const issue = validateConditionBranches(branches)[0]
  if (issue) return { ok: false, reason: 'invalid_range', issue }

  return {
    ok: true,
    node: {
      ...ensured,
      branches,
      conditionExpression: syncRootConditionExpression(branches),
    },
  }
}

/** 根节点改名/改 key 时保留分支结构 */
export function preserveConditionBranchesOnRootEdit(
  prev: WorkflowDesignNode,
  next: WorkflowDesignNode,
): WorkflowDesignNode {
  if (!isConditionLikeGateway(next.nodeType)) return next
  const branches = ensureConditionBranches(prev).branches
  return {
    ...next,
    branches,
    conditionExpression: syncRootConditionExpression(branches || []),
  }
}

export function validateConditionGatewayForDeploy(
  node: WorkflowDesignNode,
): ConditionValidationIssue | 'no_yes_branch' | null {
  if (!isConditionLikeGateway(node.nodeType)) return null
  const branches = getConditionBranches(node).filter((b) => !b.isDefault)
  if (!branches.length) return 'no_yes_branch'
  return validateConditionBranches(getConditionBranches(node))[0] || null
}
