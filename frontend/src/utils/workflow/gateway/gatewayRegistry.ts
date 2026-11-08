import {
  ensureConditionBranches,
  ensureParallelBranches,
  type WorkflowDesignNode,
} from '@/types/workflow/design'
import {
  applyConditionBranchCap,
  getConditionBranches,
  insertConditionBranch,
  moveConditionBranch,
  preserveConditionBranchesOnRootEdit,
  removeConditionBranch,
  validateConditionGatewayForDeploy,
  type ApplyConditionBranchCapInput,
  type ApplyConditionBranchCapResult,
  type ConditionBranchRemoveResult,
} from './conditionGateway'
import {
  getParallelBranches,
  insertParallelBranch,
  preserveParallelBranchesOnRootEdit,
  removeParallelBranch,
  validateParallelGatewayForDeploy,
  type ParallelBranchRemoveResult,
} from './parallelGateway'
import {
  branchCapNodeType,
  isConditionLikeGateway,
  isParallelGateway,
  resolveGatewayKind,
  type GatewayKind,
} from './gatewayTypes'

export interface GatewayStrategy {
  kind: GatewayKind
  /** 补齐 branches 缺省结构 */
  ensure: (node: WorkflowDesignNode) => WorkflowDesignNode
  getBranches: (node: WorkflowDesignNode) => ReturnType<typeof getConditionBranches>
  preserveBranchesOnRootEdit: (
    prev: WorkflowDesignNode,
    next: WorkflowDesignNode,
  ) => WorkflowDesignNode
  validateForDeploy: (node: WorkflowDesignNode) => unknown
}

const CONDITION_LIKE_STRATEGY: GatewayStrategy = {
  kind: 'condition',
  ensure: ensureConditionBranches,
  getBranches: getConditionBranches,
  preserveBranchesOnRootEdit: preserveConditionBranchesOnRootEdit,
  validateForDeploy: validateConditionGatewayForDeploy,
}

const PARALLEL_STRATEGY: GatewayStrategy = {
  kind: 'parallel',
  ensure: ensureParallelBranches,
  getBranches: getParallelBranches,
  preserveBranchesOnRootEdit: preserveParallelBranchesOnRootEdit,
  validateForDeploy: validateParallelGatewayForDeploy,
}

const GATEWAY_REGISTRY: Record<GatewayKind, GatewayStrategy> = {
  condition: CONDITION_LIKE_STRATEGY,
  inclusive: { ...CONDITION_LIKE_STRATEGY, kind: 'inclusive' },
  parallel: PARALLEL_STRATEGY,
}

export function getGatewayStrategy(nodeType?: string): GatewayStrategy | null {
  const kind = resolveGatewayKind(nodeType)
  return kind ? GATEWAY_REGISTRY[kind] : null
}

export function ensureGatewayNode(node: WorkflowDesignNode): WorkflowDesignNode {
  const strategy = getGatewayStrategy(node.nodeType)
  return strategy ? strategy.ensure({ ...node }) : node
}

export function preserveGatewayBranchesOnRootEdit(
  prev: WorkflowDesignNode,
  next: WorkflowDesignNode,
): WorkflowDesignNode {
  const strategy = getGatewayStrategy(next.nodeType)
  return strategy ? strategy.preserveBranchesOnRootEdit(prev, next) : next
}

export {
  applyConditionBranchCap,
  branchCapNodeType,
  getConditionBranches,
  getParallelBranches,
  insertConditionBranch,
  insertParallelBranch,
  isConditionLikeGateway,
  isParallelGateway,
  moveConditionBranch,
  preserveConditionBranchesOnRootEdit,
  preserveParallelBranchesOnRootEdit,
  removeConditionBranch,
  removeParallelBranch,
  resolveGatewayKind,
  validateConditionGatewayForDeploy,
  validateParallelGatewayForDeploy,
}
export type {
  ApplyConditionBranchCapInput,
  ApplyConditionBranchCapResult,
  ConditionBranchRemoveResult,
  GatewayKind,
  ParallelBranchRemoveResult,
}
