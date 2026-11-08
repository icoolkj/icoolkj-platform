import type { WorkflowApproverRuleVO } from '@/types/workflow/approverRule'
import { WORKFLOW_NODE_TYPES } from '@/constants/workflowDesign'
import type { WorkflowDesignNode, WorkflowDesignNodeType } from '@/types/workflow/design'
import {
  defaultApproveNode,
  type DesignNodeFactoryLabels,
} from '@/utils/workflow/designNodeFactory'

/**
 * @deprecated 产品路径禁止 BPMN→design 反解析。仅脚本/迁移工具可引用。
 * 见 docs/architecture/29、30。
 */
export function extractUserTasksFromBpmn(
  bpmnXml?: string,
): { id: string; name: string; variableName?: string }[] {
  if (!bpmnXml) return []
  const tasks: { id: string; name: string; variableName?: string }[] = []
  const re = /<userTask\b([^>]*)\/?>/gi
  let m: RegExpExecArray | null
  while ((m = re.exec(bpmnXml)) != null) {
    const attrs = m[1] || ''
    const id = attr(attrs, 'id')
    if (!id) continue
    const name = attr(attrs, 'name') || id
    const assignee = attr(attrs, 'flowable:assignee') || attr(attrs, 'assignee')
    let variableName: string | undefined
    const varMatch = assignee?.match(/\$\{([^}]+)\}/)
    if (varMatch) variableName = varMatch[1]
    tasks.push({ id, name, variableName })
  }
  return tasks
}

function attr(attrs: string, name: string): string | undefined {
  const re = new RegExp(`${name}\\s*=\\s*["']([^"']+)["']`, 'i')
  const m = attrs.match(re)
  return m?.[1]
}

/** 仅从审批人规则还原线性节点（无网关拓扑；产品兜底用） */
export function buildNodesFromRules(rules: WorkflowApproverRuleVO[]): WorkflowDesignNode[] {
  const sorted = [...rules].sort((a, b) => (a.sortNo ?? 0) - (b.sortNo ?? 0))
  return sorted
    .filter((r) => r.taskDefinitionKey)
    .map((r, i) => {
      const key = r.taskDefinitionKey!
      const isCc =
        key.toLowerCase() === WORKFLOW_NODE_TYPES.CC ||
        key.toLowerCase().startsWith(WORKFLOW_NODE_TYPES.CC)
      return {
        nodeType: (isCc
          ? WORKFLOW_NODE_TYPES.CC
          : WORKFLOW_NODE_TYPES.APPROVE) as WorkflowDesignNodeType,
        taskDefinitionKey: key,
        name: key,
        variableName: r.variableName || `${key}UserId`,
        strategyType: r.strategyType || 'role',
        strategyConfig: r.strategyConfig || '{"roleCodes":["dept_approver"]}',
        resolveScope: r.resolveScope || 'biz_tenant',
        sortNo: r.sortNo ?? i + 1,
        ruleId: r.id,
      }
    })
}

/** @deprecated 使用 designNodeFactory；保留签名以免旧引用断裂 */
export function legacyDefaultApproveNode(
  index: number,
  labels: DesignNodeFactoryLabels,
): WorkflowDesignNode {
  return defaultApproveNode(index, labels)
}
