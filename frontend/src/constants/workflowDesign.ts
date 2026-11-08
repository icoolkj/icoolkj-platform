/** 工作流设计节点类型 / passMode 前端契约（与后端 DesignNodeTypes 对齐） */
export const WORKFLOW_NODE_TYPES = {
  APPROVE: 'approve',
  CC: 'cc',
  CONDITION: 'condition',
  PARALLEL: 'parallel',
  INCLUSIVE: 'inclusive',
  DELAY: 'delay',
  SERVICE: 'service',
  SUBPROCESS: 'subprocess',
} as const

export type WorkflowNodeTypeCode = (typeof WORKFLOW_NODE_TYPES)[keyof typeof WORKFLOW_NODE_TYPES]

/** 加号菜单可插入类型 */
export const WORKFLOW_ADDABLE_NODE_TYPES = [
  WORKFLOW_NODE_TYPES.APPROVE,
  WORKFLOW_NODE_TYPES.CC,
  WORKFLOW_NODE_TYPES.CONDITION,
  WORKFLOW_NODE_TYPES.PARALLEL,
  WORKFLOW_NODE_TYPES.INCLUSIVE,
  WORKFLOW_NODE_TYPES.DELAY,
  WORKFLOW_NODE_TYPES.SERVICE,
  WORKFLOW_NODE_TYPES.SUBPROCESS,
] as const

export const WORKFLOW_PASS_MODES = {
  SEQUENTIAL: 'sequential',
  SEQUENTIAL_ALL: 'sequentialAll',
  ANY: 'any',
  ALL: 'all',
} as const

export const WORKFLOW_BRANCH_IDS = {
  YES: 'yes',
  DEFAULT: 'default',
} as const
