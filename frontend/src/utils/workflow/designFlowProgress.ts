import type { WorkflowDesignNode } from '@/types/workflow/design'
import {
  ensureConditionBranches,
  ensureParallelBranches,
  extractDesignNodesFromJson,
} from '@/types/workflow/design'
import type { WorkflowProgressNodeVO } from '@/types/workflow/instance'

/** 审批流程图展示行：design 拓扑 + progress 染色 */
export type DesignFlowRow = {
  key: string
  name: string
  kind:
    | 'approve'
    | 'cc'
    | 'condition'
    | 'inclusive'
    | 'parallel'
    | 'delay'
    | 'service'
    | 'subprocess'
    | 'branch'
  depth: number
  nodeStatus?: string
  assigneeUserId?: string
  assigneeUserName?: string
  approved?: boolean
  comment?: string
}

export function buildDesignFlowRows(
  designJson: string | undefined,
  progressNodes: WorkflowProgressNodeVO[] | undefined,
): DesignFlowRow[] {
  const nodes = extractDesignNodesFromJson(designJson)
  if (!nodes.length) return []
  const byKey = new Map<string, WorkflowProgressNodeVO>()
  for (const p of progressNodes || []) {
    if (p.nodeKey) byKey.set(p.nodeKey, p)
  }
  const rows: DesignFlowRow[] = []
  walk(nodes, 0, byKey, rows)
  annotateBranchRows(rows)
  return rows
}

function walk(
  list: WorkflowDesignNode[],
  depth: number,
  byKey: Map<string, WorkflowProgressNodeVO>,
  rows: DesignFlowRow[],
) {
  for (const n of list) {
    const type = n.nodeType || 'approve'
    if (type === 'condition' || type === 'inclusive') {
      const kind = type === 'inclusive' ? 'inclusive' : 'condition'
      const prog = byKey.get(n.taskDefinitionKey)
      rows.push(rowOf(n, kind, depth, prog))
      for (const b of ensureConditionBranches(n).branches || []) {
        rows.push({
          key: `${n.taskDefinitionKey}:${b.id}`,
          name: b.name || b.id,
          kind: 'branch',
          depth: depth + 1,
        })
        walk(b.nodes || [], depth + 2, byKey, rows)
      }
      continue
    }
    if (type === 'parallel') {
      const prog = byKey.get(n.taskDefinitionKey)
      rows.push(rowOf(n, 'parallel', depth, prog))
      for (const b of ensureParallelBranches(n).branches || []) {
        rows.push({
          key: `${n.taskDefinitionKey}:${b.id}`,
          name: b.name || b.id,
          kind: 'branch',
          depth: depth + 1,
        })
        walk(b.nodes || [], depth + 2, byKey, rows)
      }
      continue
    }
    if (type === 'delay' || type === 'service' || type === 'subprocess') {
      const prog = byKey.get(n.taskDefinitionKey)
      rows.push(rowOf(n, type, depth, prog))
      continue
    }
    const prog = byKey.get(n.taskDefinitionKey)
    rows.push(rowOf(n, type === 'cc' ? 'cc' : 'approve', depth, prog))
  }
}

/** 给分支标题染色：已走 / 未走，避免未走路径看起来像还在待处理 */
function annotateBranchRows(rows: DesignFlowRow[]) {
  for (let i = 0; i < rows.length; i++) {
    const row = rows[i]
    if (row.kind !== 'branch') continue
    const childDepth = row.depth + 1
    let reached = false
    let skippedOnly = true
    let hasChild = false
    for (let j = i + 1; j < rows.length; j++) {
      const child = rows[j]
      if (child.depth <= row.depth) break
      if (child.depth !== childDepth) continue
      if (child.kind !== 'approve' && child.kind !== 'cc') continue
      hasChild = true
      const status = child.nodeStatus
      if (status === 'active' || status === 'completed' || status === 'rejected') {
        reached = true
        skippedOnly = false
      } else if (status && status !== 'skipped') {
        skippedOnly = false
      }
    }
    if (!hasChild) continue
    if (reached) {
      row.nodeStatus = 'completed'
    } else if (skippedOnly) {
      row.nodeStatus = 'skipped'
    }
  }
}

function rowOf(
  n: WorkflowDesignNode,
  kind: DesignFlowRow['kind'],
  depth: number,
  prog?: WorkflowProgressNodeVO,
): DesignFlowRow {
  return {
    key: n.taskDefinitionKey,
    name: prog?.nodeName || n.name || n.taskDefinitionKey,
    kind,
    depth,
    nodeStatus: prog?.nodeStatus,
    assigneeUserId: prog?.assigneeUserId,
    assigneeUserName: prog?.assigneeUserName,
    approved: prog?.approved,
    comment: prog?.comment,
  }
}
