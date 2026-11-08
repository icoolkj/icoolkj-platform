import { WORKFLOW_BRANCH_IDS, WORKFLOW_NODE_TYPES } from '@/constants/workflowDesign'
import type { WorkflowDesignNode } from '@/types/workflow/design'

/** 默认节点文案（由设计器注入 i18n，禁止工厂内写死中文） */
export type DesignNodeFactoryLabels = {
  approveFirst: string
  approveNth: (n: number) => string
  cc: string
  condition: string
  parallel: string
  inclusive: string
  delay: string
  service: string
  subprocess: string
  branchYes: (n: number) => string
  branchDefault: string
  parallelBranch: (n: number) => string
}

export function defaultApproveNode(
  index: number,
  labels: DesignNodeFactoryLabels,
): WorkflowDesignNode {
  const key = index === 1 ? 'deptLeaderApprove' : `approve${index}`
  return {
    nodeType: WORKFLOW_NODE_TYPES.APPROVE,
    taskDefinitionKey: key,
    name: index === 1 ? labels.approveFirst : labels.approveNth(index),
    variableName: `${key}UserId`,
    strategyType: 'starter_dept_role',
    strategyConfig: '{"roleCodes":["dept_approver"]}',
    resolveScope: 'biz_tenant',
    sortNo: index,
  }
}

export function defaultCcNode(index: number, labels: DesignNodeFactoryLabels): WorkflowDesignNode {
  const key = `cc${index}`
  return {
    nodeType: WORKFLOW_NODE_TYPES.CC,
    taskDefinitionKey: key,
    name: labels.cc,
    variableName: `${key}UserId`,
    strategyType: 'role',
    strategyConfig: '{"roleCodes":["tenant_admin"]}',
    resolveScope: 'biz_tenant',
    sortNo: index,
  }
}

export function defaultConditionNode(
  index: number,
  labels: DesignNodeFactoryLabels,
): WorkflowDesignNode {
  return defaultConditionLikeNode(WORKFLOW_NODE_TYPES.CONDITION, index, labels)
}

export function defaultParallelNode(
  index: number,
  labels: DesignNodeFactoryLabels,
): WorkflowDesignNode {
  const key = `par${index}`
  return {
    nodeType: WORKFLOW_NODE_TYPES.PARALLEL,
    taskDefinitionKey: key,
    name: labels.parallel,
    variableName: key,
    strategyType: 'expression',
    strategyConfig: '{}',
    resolveScope: 'biz_tenant',
    branches: [
      { id: 'p1', name: labels.parallelBranch(1), nodes: [] },
      { id: 'p2', name: labels.parallelBranch(2), nodes: [] },
    ],
    sortNo: index,
  }
}

export function defaultInclusiveNode(
  index: number,
  labels: DesignNodeFactoryLabels,
): WorkflowDesignNode {
  return defaultConditionLikeNode(WORKFLOW_NODE_TYPES.INCLUSIVE, index, labels)
}

function defaultConditionLikeNode(
  nodeType: typeof WORKFLOW_NODE_TYPES.CONDITION | typeof WORKFLOW_NODE_TYPES.INCLUSIVE,
  index: number,
  labels: DesignNodeFactoryLabels,
): WorkflowDesignNode {
  const key = nodeType === WORKFLOW_NODE_TYPES.INCLUSIVE ? `incl${index}` : `cond${index}`
  return {
    nodeType,
    taskDefinitionKey: key,
    name: nodeType === WORKFLOW_NODE_TYPES.INCLUSIVE ? labels.inclusive : labels.condition,
    variableName: key,
    strategyType: 'expression',
    strategyConfig: '{}',
    resolveScope: 'biz_tenant',
    conditionExpression: '',
    branches: [
      {
        id: WORKFLOW_BRANCH_IDS.YES,
        name: labels.branchYes(1),
        expression: '',
        isDefault: false,
        nodes: [],
      },
      {
        id: WORKFLOW_BRANCH_IDS.DEFAULT,
        name: labels.branchDefault,
        expression: '',
        isDefault: true,
        nodes: [],
      },
    ],
    sortNo: index,
  }
}

export function defaultDelayNode(
  index: number,
  labels: DesignNodeFactoryLabels,
): WorkflowDesignNode {
  const key = `delay${index}`
  return {
    nodeType: WORKFLOW_NODE_TYPES.DELAY,
    taskDefinitionKey: key,
    name: labels.delay,
    variableName: key,
    strategyType: 'expression',
    strategyConfig: '{}',
    resolveScope: 'biz_tenant',
    dueDuration: 'PT1H',
    sortNo: index,
  }
}

export function defaultServiceNode(
  index: number,
  labels: DesignNodeFactoryLabels,
): WorkflowDesignNode {
  const key = `svc${index}`
  return {
    nodeType: WORKFLOW_NODE_TYPES.SERVICE,
    taskDefinitionKey: key,
    name: labels.service,
    variableName: key,
    strategyType: 'expression',
    strategyConfig: '{"delegateExpression":"${workflowNoOpService.execute(execution)}"}',
    resolveScope: 'biz_tenant',
    sortNo: index,
  }
}

export function defaultSubprocessNode(
  index: number,
  labels: DesignNodeFactoryLabels,
): WorkflowDesignNode {
  const key = `sub${index}`
  return {
    nodeType: WORKFLOW_NODE_TYPES.SUBPROCESS,
    taskDefinitionKey: key,
    name: labels.subprocess,
    variableName: key,
    strategyType: 'expression',
    strategyConfig: '{"calledElement":""}',
    resolveScope: 'biz_tenant',
    sortNo: index,
  }
}
