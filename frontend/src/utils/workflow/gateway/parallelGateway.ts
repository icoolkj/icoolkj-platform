import type { BranchLabelFns } from '@/types/workflow/design'
import {
  ensureParallelBranches,
  renumberParallelBranchLabels,
  type WorkflowDesignBranch,
  type WorkflowDesignNode,
} from '@/types/workflow/design'
import { isParallelGateway } from './gatewayTypes'

export function getParallelBranches(node: WorkflowDesignNode): WorkflowDesignBranch[] {
  return ensureParallelBranches(node).branches || []
}

export function newParallelBranchId(): string {
  return `p${Date.now().toString(36).slice(-4)}`
}

export function insertParallelBranch(
  root: WorkflowDesignNode,
  labels: BranchLabelFns,
  branchName: string,
): WorkflowDesignNode {
  const ensured = ensureParallelBranches({ ...root })
  const branches = [...(ensured.branches || [])]
  branches.push({ id: newParallelBranchId(), name: branchName, nodes: [] })
  return {
    ...ensured,
    branches: renumberParallelBranchLabels(branches, labels),
  }
}

export type ParallelBranchRemoveResult =
  { ok: true; node: WorkflowDesignNode } | { ok: false; reason: 'min_two' }

export function removeParallelBranch(
  root: WorkflowDesignNode,
  branchId: string,
  labels: BranchLabelFns,
): ParallelBranchRemoveResult {
  const ensured = ensureParallelBranches({ ...root })
  if ((ensured.branches || []).length <= 2) {
    return { ok: false, reason: 'min_two' }
  }
  const branches = renumberParallelBranchLabels(
    (ensured.branches || []).filter((b) => b.id !== branchId),
    labels,
  )
  return { ok: true, node: { ...ensured, branches } }
}

export function preserveParallelBranchesOnRootEdit(
  prev: WorkflowDesignNode,
  next: WorkflowDesignNode,
): WorkflowDesignNode {
  if (!isParallelGateway(next.nodeType)) return next
  return { ...next, branches: ensureParallelBranches(prev).branches }
}

export function validateParallelGatewayForDeploy(node: WorkflowDesignNode): 'min_two' | null {
  if (!isParallelGateway(node.nodeType)) return null
  return getParallelBranches(node).length < 2 ? 'min_two' : null
}
