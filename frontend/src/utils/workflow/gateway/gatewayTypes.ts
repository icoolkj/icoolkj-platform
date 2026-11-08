import { WORKFLOW_NODE_TYPES } from '@/constants/workflowDesign'
import type { WorkflowDesignNodeType } from '@/types/workflow/design'

/** 设计器网关种类（与 nodeType 一一对应） */
export type GatewayKind = 'condition' | 'inclusive' | 'parallel'

const GATEWAY_KINDS: GatewayKind[] = ['condition', 'inclusive', 'parallel']

export function isGatewayKind(value: string): value is GatewayKind {
  return (GATEWAY_KINDS as string[]).includes(value)
}

export function resolveGatewayKind(nodeType?: string): GatewayKind | null {
  if (nodeType === WORKFLOW_NODE_TYPES.CONDITION) return 'condition'
  if (nodeType === WORKFLOW_NODE_TYPES.INCLUSIVE) return 'inclusive'
  if (nodeType === WORKFLOW_NODE_TYPES.PARALLEL) return 'parallel'
  return null
}

/** 排他 / 包容：共享 branches + expression 模型 */
export function isConditionLikeGateway(nodeType?: string): boolean {
  return nodeType === WORKFLOW_NODE_TYPES.CONDITION || nodeType === WORKFLOW_NODE_TYPES.INCLUSIVE
}

export function isExclusiveGateway(nodeType?: string): boolean {
  return nodeType === WORKFLOW_NODE_TYPES.CONDITION
}

export function isInclusiveGateway(nodeType?: string): boolean {
  return nodeType === WORKFLOW_NODE_TYPES.INCLUSIVE
}

export function isParallelGateway(nodeType?: string): boolean {
  return nodeType === WORKFLOW_NODE_TYPES.PARALLEL
}

export function isAnyGateway(nodeType?: string): boolean {
  return resolveGatewayKind(nodeType) !== null
}

/** 分支 cap 编辑时写入的 nodeType（默认分支仅改名称） */
export function branchCapNodeType(rootType?: string): WorkflowDesignNodeType {
  return rootType === WORKFLOW_NODE_TYPES.INCLUSIVE ? 'inclusive' : 'condition'
}
