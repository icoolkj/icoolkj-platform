/**
 * 工作流设计模型（JSON 权威）
 *
 * - 设计器 / 审批页进度：读写本结构
 * - Flowable：部署时由 BpmnNodeEmitterRegistry 生成 BPMN（部署产物，非主数据）
 *
 * 能力地图见 docs/workflow-design-nodes.md
 *
 * nodeType：
 * - approve / cc / condition（已有）
 * - parallel / inclusive（网关，branches[]）
 * - delay / service / subprocess（线性节点）
 *
 * approve.passMode：sequential | sequentialAll | any | all
 * 网关类统一用 branches[]；子节点 v1 仅 approve/cc。
 */

import { isAnyGateway, isConditionLikeGateway } from '@/utils/workflow/gateway/gatewayTypes'

export const WORKFLOW_DESIGN_SCHEMA_VERSION = 1 as const

/** 已落地设计节点类型 */
export type WorkflowDesignNodeType =
  'approve' | 'cc' | 'condition' | 'parallel' | 'inclusive' | 'delay' | 'service' | 'subprocess'

/** 审批通过方式：缺省 sequential = 单人任务 */
export type WorkflowPassMode = 'sequential' | 'sequentialAll' | 'any' | 'all'

export type WorkflowAssigneeStrategy = 'role' | 'starter_dept_role' | 'fixed_user' | 'expression'

/** 审批/抄送办理人（可读结构；落库时会同步 strategyType + strategyConfig） */
export interface WorkflowAssigneeSpec {
  strategyType: WorkflowAssigneeStrategy
  roleCodes?: string[]
  userId?: string
  /** expression 策略，如 starter */
  expr?: string
  resolveScope?: 'biz_tenant' | 'platform_tenant' | string
}

/** 条件/并行等网关的一条路径 */
export interface WorkflowDesignBranch {
  id: string
  name: string
  /** Flowable 表达式；parallel 无；默认路径可空 */
  expression?: string
  /** 网关 default 出口（仅 condition） */
  isDefault?: boolean
  /** 该路径节点（仅 approve/cc；当前不支持嵌套网关） */
  nodes: WorkflowDesignNode[]
}

/**
 * 单个设计节点。
 * taskDefinitionKey / variableName 对应 Flowable userTask id 与 assignee 变量。
 */
export interface WorkflowDesignNode {
  /** 节点类型，缺省按 approve */
  nodeType?: WorkflowDesignNodeType
  /** BPMN userTask / gateway 业务键（字母开头） */
  taskDefinitionKey: string
  /** 界面展示名称 */
  name: string
  /** 流程变量名（approve/cc）；网关可用 key 自身 */
  variableName: string
  /** @deprecated 优先用 assignee；保留兼容 deploy API */
  strategyType: string
  /** @deprecated JSON 字符串，优先用 assignee */
  strategyConfig: string
  resolveScope: string
  sortNo: number
  /**
   * 兼容旧版：条件表达式；有 branches 时与首条非默认分支 expression 同步。
   */
  conditionExpression?: string
  /** 条件/并行分叉 */
  branches?: WorkflowDesignBranch[]
  /**
   * 审批通过方式：sequential 单人；any 或签；all 会签。
   * 会签集合变量：{taskDefinitionKey}AssigneeList
   */
  passMode?: WorkflowPassMode
  /** 结构化审批人（权威可读） */
  assignee?: WorkflowAssigneeSpec
  /** 已落库规则 id（仅前端展示，不入库快照） */
  ruleId?: string
  /** 驳回后：end | previous | starter | starterEdit；缺省 end */
  rejectMode?: 'end' | 'previous' | 'starter' | 'starterEdit'
  /** ISO-8601 时限，如 PT24H；空=不挂定时器 */
  dueDuration?: string
  /** 超时动作：notify | rejectEnd；缺省 notify */
  timeoutAction?: 'notify' | 'rejectEnd' | 'autoApprove'
  /** 是否允许转办；缺省 true */
  allowTransfer?: boolean
  /** 是否允许委派；缺省 true */
  allowDelegate?: boolean
  /** 是否允许加签；缺省 true */
  allowAddSign?: boolean
  /** 节点 × 字段权限：fieldCode → R|W|H；未配置字段默认 R */
  fieldPerms?: Record<string, 'R' | 'W' | 'H'>
}

export type WorkflowFieldPerm = 'R' | 'W' | 'H'

/** design 段：竖向流程图权威快照 */
export interface WorkflowDesignDocument {
  schemaVersion: typeof WORKFLOW_DESIGN_SCHEMA_VERSION
  nodes: WorkflowDesignNode[]
  /** 条件表达式 fieldCode → 展示名（画布/审批流程图人读） */
  fieldLabels?: Record<string, string>
}

export interface WorkflowProcessConfig {
  schemaVersion?: typeof WORKFLOW_DESIGN_SCHEMA_VERSION
  startAcl?: {
    deptIds?: string[]
    roleCodes?: string[]
    userIds?: string[]
    postIds?: string[]
    /**
     * 平台跨租户可发起范围（预留）。本版 UI 仅展示说明，后端不参与校验。
     */
    tenantIds?: string[]
  }
  behavior?: {
    titleTemplate?: string
    summaryTemplate?: string
    allowWithdraw?: boolean
    commentRequired?: boolean
    autoDedup?: boolean
  }
  notify?: {
    onReturn?: boolean
    onReject?: boolean
    onTodo?: boolean
    onCc?: boolean
    onComplete?: boolean
    onTimeout?: boolean
    onUrge?: boolean
  }
  /** 权威设计快照 */
  design?: WorkflowDesignDocument
  /**
   * @deprecated 兼容旧版；读取时自动提升为 design.nodes
   */
  designNodes?: WorkflowDesignNode[]
}

export interface WorkflowModelDesignDeployPayload {
  modelId: string
  nodes: Omit<WorkflowDesignNode, 'ruleId'>[]
  processConfig?: string
  remark?: string
}

export function emptyProcessConfig(): WorkflowProcessConfig {
  return {
    schemaVersion: WORKFLOW_DESIGN_SCHEMA_VERSION,
    startAcl: { deptIds: [], roleCodes: [], userIds: [], postIds: [], tenantIds: [] },
    behavior: {
      titleTemplate: '{starter} · {processName}',
      summaryTemplate: '{starter} · {processName}',
      allowWithdraw: true,
      commentRequired: false,
      autoDedup: false,
    },
    notify: {
      onReturn: false,
      onReject: true,
      onTodo: true,
      onCc: false,
      onComplete: true,
      onTimeout: false,
      onUrge: false,
    },
  }
}

export function emptyDesignDocument(): WorkflowDesignDocument {
  return { schemaVersion: WORKFLOW_DESIGN_SCHEMA_VERSION, nodes: [] }
}

/** 运行时配置（去掉设计快照，供 ACL/通知解析） */
export function runtimeProcessConfig(cfg: WorkflowProcessConfig): WorkflowProcessConfig {
  return {
    schemaVersion: WORKFLOW_DESIGN_SCHEMA_VERSION,
    startAcl: cfg.startAcl ?? emptyProcessConfig().startAcl,
    behavior: cfg.behavior ?? emptyProcessConfig().behavior,
    notify: cfg.notify ?? emptyProcessConfig().notify,
  }
}

export function stripRuleId(node: WorkflowDesignNode): Omit<WorkflowDesignNode, 'ruleId'> {
  const { ruleId: _r, ...rest } = node
  if (rest.branches?.length) {
    return {
      ...rest,
      branches: rest.branches.map((b) => ({
        ...b,
        nodes: b.nodes.map((n) => stripRuleId(n) as WorkflowDesignNode),
      })),
    }
  }
  return rest
}

/** strategyConfig 字符串 ↔ assignee 结构 */
export function parseAssignee(
  node: Pick<WorkflowDesignNode, 'strategyType' | 'strategyConfig' | 'resolveScope' | 'assignee'>,
): WorkflowAssigneeSpec {
  if (node.assignee?.strategyType) {
    return {
      ...node.assignee,
      resolveScope: node.assignee.resolveScope || node.resolveScope || 'biz_tenant',
    }
  }
  const strategyType = (node.strategyType || 'role') as WorkflowAssigneeStrategy
  const spec: WorkflowAssigneeSpec = {
    strategyType,
    resolveScope: node.resolveScope || 'biz_tenant',
  }
  try {
    const cfg = JSON.parse(node.strategyConfig || '{}') as Record<string, unknown>
    if (Array.isArray(cfg.roleCodes)) spec.roleCodes = cfg.roleCodes as string[]
    if (cfg.userId != null) spec.userId = String(cfg.userId)
    if (cfg.expr != null) spec.expr = String(cfg.expr)
  } catch {
    /* ignore */
  }
  return spec
}

export function applyAssigneeToNode(
  node: WorkflowDesignNode,
  assignee: WorkflowAssigneeSpec,
): WorkflowDesignNode {
  const strategyType = assignee.strategyType
  let strategyConfig = '{}'
  if (strategyType === 'role' || strategyType === 'starter_dept_role') {
    strategyConfig = JSON.stringify({ roleCodes: assignee.roleCodes ?? [] })
  } else if (strategyType === 'fixed_user') {
    strategyConfig = JSON.stringify({ userId: assignee.userId ?? '' })
  } else if (strategyType === 'expression') {
    strategyConfig = JSON.stringify({ expr: assignee.expr ?? 'starter' })
  }
  return {
    ...node,
    strategyType,
    strategyConfig,
    resolveScope: assignee.resolveScope || node.resolveScope || 'biz_tenant',
    assignee: { ...assignee, resolveScope: assignee.resolveScope || 'biz_tenant' },
  }
}

export function normalizePassMode(raw?: string | null): WorkflowPassMode {
  const v = (raw || 'sequential').trim().toLowerCase()
  if (v === 'sequentialall') return 'sequentialAll'
  if (v === 'any' || v === 'all') return v
  return 'sequential'
}

/** 缺省：条件1 + 默认 */
export type BranchLabelFns = {
  yes: (n: number) => string
  default: string
  parallel: (n: number) => string
}

const NEUTRAL_BRANCH_LABELS: BranchLabelFns = {
  yes: (n) => `branch${n}`,
  default: 'default',
  parallel: (n) => `parallel${n}`,
}

export function defaultConditionBranches(
  yesExpression = '',
  labels: BranchLabelFns = NEUTRAL_BRANCH_LABELS,
): WorkflowDesignBranch[] {
  return [
    { id: 'yes', name: labels.yes(1), expression: yesExpression, isDefault: false, nodes: [] },
    { id: 'default', name: labels.default, expression: '', isDefault: true, nodes: [] },
  ]
}

/** 缺省两路并行 */
export function defaultParallelBranches(
  labels: BranchLabelFns = NEUTRAL_BRANCH_LABELS,
): WorkflowDesignBranch[] {
  return [
    { id: 'p1', name: labels.parallel(1), nodes: [] },
    { id: 'p2', name: labels.parallel(2), nodes: [] },
  ]
}

/** 按顺序重编号条件名（仅覆盖空名 / 自动生成名，保留用户自定义） */
export function renumberConditionBranchLabels(
  branches: WorkflowDesignBranch[],
  labels: BranchLabelFns = NEUTRAL_BRANCH_LABELS,
): WorkflowDesignBranch[] {
  let yesIndex = 0
  return branches.map((b) => {
    if (b.isDefault) {
      if (isAutoDefaultBranchName(b.name, labels)) {
        return { ...b, name: labels.default }
      }
      return { ...b }
    }
    yesIndex += 1
    if (isAutoYesBranchName(b.name, labels)) {
      return { ...b, name: labels.yes(yesIndex) }
    }
    return { ...b }
  })
}

/** 并行路径重命名（仅覆盖空名 / 自动生成名） */
export function renumberParallelBranchLabels(
  branches: WorkflowDesignBranch[],
  labels: BranchLabelFns = NEUTRAL_BRANCH_LABELS,
): WorkflowDesignBranch[] {
  return branches.map((b, i) => {
    const nextName = isAutoParallelBranchName(b.name, labels) ? labels.parallel(i + 1) : b.name
    return {
      ...b,
      isDefault: false,
      expression: undefined,
      name: nextName,
    }
  })
}

function isAutoYesBranchName(name: string | undefined, labels: BranchLabelFns): boolean {
  const n = (name || '').trim()
  if (!n) return true
  if (/^branch\d+$/i.test(n)) return true
  for (let i = 1; i <= 64; i++) {
    if (n === labels.yes(i)) return true
  }
  return false
}

function isAutoDefaultBranchName(name: string | undefined, labels: BranchLabelFns): boolean {
  const n = (name || '').trim()
  if (!n) return true
  return n === labels.default || /^default$/i.test(n)
}

function isAutoParallelBranchName(name: string | undefined, labels: BranchLabelFns): boolean {
  const n = (name || '').trim()
  if (!n) return true
  if (/^parallel\d+$/i.test(n)) return true
  for (let i = 1; i <= 64; i++) {
    if (n === labels.parallel(i)) return true
  }
  return false
}

/** 条件网关严谨校验问题（部署前硬拦） */
export type ConditionValidationIssue =
  | { code: 'empty_expression'; label: string }
  | { code: 'duplicate_expression'; expression: string }
  | { code: 'invalid_range'; field: string; low: number; high: number }
  | {
      code: 'unreachable_gt'
      field: string
      earlier: number
      later: number
      earlierPri: number
      laterPri: number
    }

const SIMPLE_GT = /^\$\{\s*([a-zA-Z_][\w]*)\s*(>=|>)\s*(-?\d+(?:\.\d+)?)\s*\}$/
/** 画布生成：${f > a && f <= b}；亦兼容双 ${} 拼接 */
const RANGE_GT_LTE =
  /^\$\{\s*([a-zA-Z_][\w]*)\s*>\s*(-?\d+(?:\.\d+)?)\s*&&\s*\1\s*<=\s*(-?\d+(?:\.\d+)?)\s*\}$|^\$\{\s*([a-zA-Z_][\w]*)\s*>\s*(-?\d+(?:\.\d+)?)\s*\}\s*&&\s*\$\{\s*\4\s*<=\s*(-?\d+(?:\.\d+)?)\s*\}$/
const SIMPLE_COMPARE = /^\$\{\s*([a-zA-Z_][\w]*)\s*(>=|<=|==|!=|>|<)\s*(.+?)\s*\}$/

const COMPARE_OP_LABEL: Record<string, string> = {
  '>=': '≥',
  '<=': '≤',
  '==': '=',
  '!=': '≠',
  '>': '>',
  '<': '<',
}

function resolveFieldLabel(
  fieldCode: string,
  fieldLabelOf?: ((code: string) => string) | Record<string, string>,
): string {
  if (!fieldLabelOf) return fieldCode
  if (typeof fieldLabelOf === 'function') {
    return fieldLabelOf(fieldCode) || fieldCode
  }
  return fieldLabelOf[fieldCode] || fieldCode
}

/**
 * 将简单 EL 转为人读摘要（画布展示）；解析失败返回 null，由调用方回退裸 EL。
 * @param fieldLabelOf fieldCode → 展示名，或查找函数
 */
export function formatConditionExpressionHuman(
  expression: string | undefined | null,
  fieldLabelOf?: ((fieldCode: string) => string) | Record<string, string>,
): string | null {
  const expr = (expression || '').trim()
  if (!expr) return null
  const range = parseRangeGtLte(expr)
  if (range) {
    const label = resolveFieldLabel(range.field, fieldLabelOf)
    return `${label} > ${range.low} ≤ ${range.high}`
  }
  const m = expr.match(SIMPLE_COMPARE)
  if (m) {
    const field = m[1]
    const op = COMPARE_OP_LABEL[m[2]] || m[2]
    const value = m[3].trim()
    const label = resolveFieldLabel(field, fieldLabelOf)
    return `${label} ${op} ${value}`
  }
  return null
}

/**
 * 排他网关分支严谨性检查（按当前排序=优先级）：
 * - 条件表达式非空
 * - 表达式不可完全重复
 * - 区间 a&lt;x≤b 须 a&lt;b
 * - 同字段两条「开区间下界」且前序阈值更小 → 后条永不可达（须拦截）
 */
export function validateConditionBranches(
  branches: WorkflowDesignBranch[],
): ConditionValidationIssue[] {
  const issues: ConditionValidationIssue[] = []
  const yes = branches.filter((b) => !b.isDefault)
  const seen = new Map<string, string>()

  for (const b of yes) {
    const expr = (b.expression || '').trim()
    if (!expr) {
      issues.push({ code: 'empty_expression', label: b.name || '' })
      continue
    }
    if (seen.has(expr)) {
      issues.push({ code: 'duplicate_expression', expression: expr })
    } else {
      seen.set(expr, b.name || '')
    }
    const range = parseRangeGtLte(expr)
    if (range && !(range.low < range.high)) {
      issues.push({
        code: 'invalid_range',
        field: range.field,
        low: range.low,
        high: range.high,
      })
    }
  }

  type Bound = { field: string; num: number; pri: number }
  const lowerBounds: Bound[] = []
  yes.forEach((b, pri) => {
    const expr = (b.expression || '').trim()
    const simple = expr.match(SIMPLE_GT)
    if (simple) {
      lowerBounds.push({ field: simple[1], num: Number(simple[3]), pri: pri + 1 })
    }
    // 区间表达式有上界截断，不参与「开口下界吞噬」检测
  })

  for (let i = 0; i < lowerBounds.length; i++) {
    for (let j = i + 1; j < lowerBounds.length; j++) {
      const a = lowerBounds[i]
      const b = lowerBounds[j]
      if (a.field !== b.field) continue
      if (a.num < b.num) {
        issues.push({
          code: 'unreachable_gt',
          field: a.field,
          earlier: a.num,
          later: b.num,
          earlierPri: a.pri,
          laterPri: b.pri,
        })
      }
    }
  }
  return issues
}

function parseRangeGtLte(expr: string): { field: string; low: number; high: number } | null {
  const m = expr.match(RANGE_GT_LTE)
  if (!m) return null
  if (m[1]) {
    return { field: m[1], low: Number(m[2]), high: Number(m[3]) }
  }
  return { field: m[4], low: Number(m[5]), high: Number(m[6]) }
}

/** 保证 condition / inclusive 至少一条条件 + 默认 */
export function ensureConditionBranches(node: WorkflowDesignNode): WorkflowDesignNode {
  const type = node.nodeType || 'approve'
  if (!isConditionLikeGateway(type)) return node
  const expr = (node.conditionExpression || '').trim()
  if (node.branches?.length) {
    let branches = node.branches.map((b, i) => ({
      ...b,
      id: b.id || (b.isDefault ? 'default' : `yes${i + 1}`),
      nodes: (b.nodes || []).map((n, ni) => normalizeDesignNode(n, ni)),
    }))
    const hasYes = branches.some((b) => !b.isDefault)
    const hasDef = branches.some((b) => b.isDefault)
    if (!hasYes) {
      branches = [
        {
          id: 'yes',
          name: NEUTRAL_BRANCH_LABELS.yes(1),
          expression: expr,
          isDefault: false,
          nodes: [],
        },
        ...branches,
      ]
    }
    if (!hasDef) {
      branches = [
        ...branches,
        {
          id: 'default',
          name: NEUTRAL_BRANCH_LABELS.default,
          expression: '',
          isDefault: true,
          nodes: [],
        },
      ]
    }
    const yesList = branches.filter((b) => !b.isDefault)
    const defList = branches.filter((b) => b.isDefault)
    branches = renumberConditionBranchLabels([...yesList, ...defList.slice(0, 1)])
    const firstYes = branches.find((b) => !b.isDefault)
    if (firstYes && !firstYes.expression?.trim() && expr) {
      firstYes.expression = expr
    }
    const syncedExpr = (firstYes?.expression || expr).trim()
    return { ...node, conditionExpression: syncedExpr, branches }
  }
  return {
    ...node,
    conditionExpression: expr,
    branches: defaultConditionBranches(expr),
  }
}

/** 保证 parallel 至少两路 */
export function ensureParallelBranches(node: WorkflowDesignNode): WorkflowDesignNode {
  if (node.nodeType !== 'parallel') return node
  let branches: WorkflowDesignBranch[] = (node.branches || []).map((b, i) => ({
    ...b,
    id: b.id || `p${i + 1}`,
    isDefault: false,
    expression: undefined,
    nodes: (b.nodes || []).map((n, ni) => normalizeDesignNode(n, ni)),
  }))
  if (branches.length < 2) {
    branches = [...branches, ...defaultParallelBranches()].slice(0, Math.max(2, branches.length))
    while (branches.length < 2) {
      const n = branches.length + 1
      branches.push({
        id: `p${n}`,
        name: NEUTRAL_BRANCH_LABELS.parallel(n),
        isDefault: false,
        nodes: [],
      })
    }
  }
  branches = renumberParallelBranchLabels(branches)
  return {
    ...node,
    variableName: node.taskDefinitionKey,
    strategyType: 'expression',
    strategyConfig: '{}',
    branches,
  }
}

function isLinearTechnicalType(type?: string) {
  return type === 'delay' || type === 'service' || type === 'subprocess'
}

/** 规范化节点：补齐 assignee / 分支 / passMode */
export function normalizeDesignNode(node: WorkflowDesignNode, index = 0): WorkflowDesignNode {
  const nodeType = node.nodeType || 'approve'
  const sortNo = node.sortNo || index + 1

  // service / delay / subprocess：禁止走办理人 rewrite（会把 delegateExpression 打成 {expr:starter}）
  if (isLinearTechnicalType(nodeType)) {
    const technical: WorkflowDesignNode = {
      ...node,
      nodeType,
      sortNo,
      variableName: node.variableName || node.taskDefinitionKey,
      strategyType: 'expression',
      resolveScope: node.resolveScope || 'biz_tenant',
      passMode: undefined,
      assignee: undefined,
      branches: undefined,
    }
    if (nodeType === 'service') {
      technical.strategyConfig = ensureServiceStrategyConfig(node.strategyConfig)
    } else if (nodeType === 'subprocess') {
      technical.strategyConfig = ensureSubprocessStrategyConfig(node.strategyConfig)
    } else if (nodeType === 'delay') {
      technical.strategyConfig = node.strategyConfig?.trim() || '{}'
      if (!technical.dueDuration?.trim()) {
        technical.dueDuration = 'PT1H'
      }
    }
    return technical
  }

  const assignee = parseAssignee(node)
  let withAssignee = applyAssigneeToNode(
    {
      ...node,
      nodeType,
      sortNo,
      variableName: node.variableName || `${node.taskDefinitionKey}UserId`,
      resolveScope: node.resolveScope || 'biz_tenant',
      passMode: normalizePassMode(node.passMode),
    },
    assignee,
  )
  if (isConditionLikeGateway(withAssignee.nodeType)) {
    withAssignee = ensureConditionBranches({
      ...withAssignee,
      variableName: withAssignee.taskDefinitionKey,
      strategyType: 'expression',
      strategyConfig: '{}',
      passMode: undefined,
    })
  } else if (withAssignee.nodeType === 'parallel') {
    withAssignee = ensureParallelBranches({
      ...withAssignee,
      passMode: undefined,
    })
  } else if (withAssignee.nodeType === 'cc') {
    withAssignee = { ...withAssignee, passMode: undefined }
  }
  if (withAssignee.nodeType === 'approve') {
    withAssignee = {
      ...withAssignee,
      rejectMode: normalizeRejectMode(withAssignee.rejectMode),
      timeoutAction: withAssignee.timeoutAction || 'notify',
      allowTransfer: withAssignee.allowTransfer !== false,
      allowDelegate: withAssignee.allowDelegate !== false,
      allowAddSign: withAssignee.allowAddSign !== false,
      fieldPerms: normalizeFieldPerms(withAssignee.fieldPerms),
    }
  } else if (withAssignee.nodeType === 'cc') {
    withAssignee = {
      ...withAssignee,
      fieldPerms: normalizeFieldPerms(withAssignee.fieldPerms),
    }
  }
  return withAssignee
}

const DEFAULT_SERVICE_STRATEGY =
  '{"delegateExpression":"${workflowNoOpService.execute(execution)}"}'

function ensureServiceStrategyConfig(raw?: string): string {
  const trimmed = (raw || '').trim()
  if (!trimmed) return DEFAULT_SERVICE_STRATEGY
  try {
    const cfg = JSON.parse(trimmed) as Record<string, unknown>
    if (typeof cfg.delegateExpression === 'string' && cfg.delegateExpression.trim()) {
      return trimmed
    }
  } catch {
    /* fall through */
  }
  return DEFAULT_SERVICE_STRATEGY
}

function ensureSubprocessStrategyConfig(raw?: string): string {
  const trimmed = (raw || '').trim()
  if (!trimmed) return '{"calledElement":""}'
  try {
    JSON.parse(trimmed)
    return trimmed
  } catch {
    return '{"calledElement":""}'
  }
}

function normalizeFieldPerms(
  raw?: Record<string, string>,
): Record<string, WorkflowFieldPerm> | undefined {
  if (!raw || typeof raw !== 'object') return undefined
  const out: Record<string, WorkflowFieldPerm> = {}
  for (const [code, perm] of Object.entries(raw)) {
    if (!code?.trim()) continue
    const p = String(perm || 'R')
      .trim()
      .toUpperCase()
    out[code.trim()] = p === 'W' || p === 'H' ? p : 'R'
  }
  return Object.keys(out).length ? out : undefined
}

function normalizeRejectMode(mode?: string): 'end' | 'previous' | 'starter' | 'starterEdit' {
  const m = (mode || 'end').trim()
  if (m === 'previous' || m === 'starter' || m === 'starterEdit') return m
  return 'end'
}

export function normalizeDesignDocument(
  nodes: WorkflowDesignNode[],
  fieldLabels?: Record<string, string> | null,
): WorkflowDesignDocument {
  const labels = sanitizeFieldLabels(fieldLabels)
  return {
    schemaVersion: WORKFLOW_DESIGN_SCHEMA_VERSION,
    nodes: nodes.map((n, i) => normalizeDesignNode(stripRuleId(n) as WorkflowDesignNode, i)),
    ...(Object.keys(labels).length ? { fieldLabels: labels } : {}),
  }
}

function sanitizeFieldLabels(fieldLabels?: Record<string, string> | null): Record<string, string> {
  const out: Record<string, string> = {}
  if (!fieldLabels) return out
  for (const [code, name] of Object.entries(fieldLabels)) {
    const key = (code || '').trim()
    const label = (name || '').trim()
    if (!key || !label) continue
    out[key] = label
  }
  return out
}

/** 从 designJson / process_config 取出条件字段展示名 */
export function extractFieldLabelsFromJson(json?: string | null): Record<string, string> {
  if (!json?.trim()) return {}
  try {
    const parsed = JSON.parse(json) as WorkflowProcessConfig | WorkflowDesignDocument
    const asProcess = parsed as WorkflowProcessConfig
    if (asProcess.design?.fieldLabels) {
      return sanitizeFieldLabels(asProcess.design.fieldLabels)
    }
    const asDoc = parsed as WorkflowDesignDocument
    return sanitizeFieldLabels(asDoc.fieldLabels)
  } catch {
    return {}
  }
}

/** 从 process_config 或独立 design 文档取出设计节点 */
export function extractDesignNodes(
  cfg?: WorkflowProcessConfig | WorkflowDesignDocument | null,
): WorkflowDesignNode[] {
  if (!cfg) return []
  const asProcess = cfg as WorkflowProcessConfig
  if (asProcess.design?.nodes?.length) {
    return asProcess.design.nodes.map((n, i) => normalizeDesignNode(n, i))
  }
  if (asProcess.designNodes?.length) {
    return asProcess.designNodes.map((n, i) => normalizeDesignNode(n, i))
  }
  const asDoc = cfg as WorkflowDesignDocument
  if (Array.isArray(asDoc.nodes) && asDoc.nodes.length) {
    return asDoc.nodes.map((n, i) => normalizeDesignNode(n, i))
  }
  return []
}

/** 解析详情 designJson / process_config JSON 字符串 */
export function extractDesignNodesFromJson(json?: string | null): WorkflowDesignNode[] {
  if (!json?.trim()) return []
  try {
    return extractDesignNodes(JSON.parse(json) as WorkflowProcessConfig | WorkflowDesignDocument)
  } catch {
    return []
  }
}

/** 组装可持久化的完整 process_config（含 design 快照） */
export function buildPersistProcessConfig(
  runtime: WorkflowProcessConfig,
  nodes: WorkflowDesignNode[],
  fieldLabels?: Record<string, string> | null,
): WorkflowProcessConfig {
  const base = runtimeProcessConfig(runtime)
  const prevLabels = runtime.design?.fieldLabels
  return {
    ...base,
    schemaVersion: WORKFLOW_DESIGN_SCHEMA_VERSION,
    design: normalizeDesignDocument(nodes, fieldLabels ?? prevLabels),
  }
}

/** 深度遍历所有 approve/cc（含分支内；不含 service/delay 等自动节点） */
export function walkLeafNodes(
  nodes: WorkflowDesignNode[],
  visit: (node: WorkflowDesignNode) => void,
): void {
  for (const n of nodes) {
    const type = n.nodeType || 'approve'
    if (isConditionLikeGateway(type)) {
      for (const b of ensureConditionBranches(n).branches || []) {
        walkLeafNodes(b.nodes, visit)
      }
      continue
    }
    if (type === 'parallel') {
      for (const b of ensureParallelBranches(n).branches || []) {
        walkLeafNodes(b.nodes, visit)
      }
      continue
    }
    if (type === 'approve' || type === 'cc') {
      visit(n)
    }
  }
}

/** 收集树上所有 taskDefinitionKey（含网关自身） */
export function collectAllTaskKeys(nodes: WorkflowDesignNode[]): string[] {
  const keys: string[] = []
  const walk = (list: WorkflowDesignNode[]) => {
    for (const n of list) {
      keys.push(n.taskDefinitionKey)
      const type = n.nodeType || 'approve'
      if (isConditionLikeGateway(type)) {
        for (const b of ensureConditionBranches(n).branches || []) walk(b.nodes)
      } else if (type === 'parallel') {
        for (const b of ensureParallelBranches(n).branches || []) walk(b.nodes)
      }
    }
  }
  walk(nodes)
  return keys
}

/** 仅 approve/cc 的 taskDefinitionKey（审批人规则 / 发布门禁用；与后端 DesignLeafTask 对齐） */
export function collectLeafTaskKeys(nodes: WorkflowDesignNode[]): string[] {
  const keys: string[] = []
  walkLeafNodes(nodes, (n) => {
    if (n.taskDefinitionKey) keys.push(n.taskDefinitionKey)
  })
  return keys
}

export function assigneeListVarName(taskDefinitionKey: string): string {
  return `${taskDefinitionKey}AssigneeList`
}

export type AssigneeSummaryLabels = {
  conditionBranches: (total: number, filled: number) => string
  parallelPaths: (n: number) => string
  passAny: string
  passAll: string
  passSequentialAll?: string
  listSep?: string
  /** expression=starter 时展示 */
  starter?: string
}

/** 办理人 ID → 可读名称（用户昵称 / 角色名 / 部门名 / 岗位名） */
export type AssigneeNameResolvers = {
  userNameOf?: (userId: string) => string
  roleNameOf?: (roleCode: string) => string
  deptNameOf?: (deptId: string) => string
  postNameOf?: (postIdOrCode: string) => string
}

/** 设计树中出现的办理人引用，供批量解析名称 */
export type DesignAssigneeRefs = {
  userIds: string[]
  roleCodes: string[]
  deptIds: string[]
  postIds: string[]
}

function pushUnique(target: string[], value?: string | null) {
  const v = (value || '').trim()
  if (v && !target.includes(v)) target.push(v)
}

function parseStrategyConfigRecord(raw?: string | null): Record<string, unknown> {
  try {
    const cfg = JSON.parse(raw || '{}') as Record<string, unknown>
    return cfg && typeof cfg === 'object' ? cfg : {}
  } catch {
    return {}
  }
}

/** 从 strategyConfig 额外收集部门/岗位（兼容未来扩展字段） */
function collectOrgRefsFromConfig(cfg: Record<string, unknown>, refs: DesignAssigneeRefs) {
  if (cfg.deptId != null) pushUnique(refs.deptIds, String(cfg.deptId))
  if (Array.isArray(cfg.deptIds)) {
    for (const id of cfg.deptIds) pushUnique(refs.deptIds, String(id))
  }
  if (cfg.postId != null) pushUnique(refs.postIds, String(cfg.postId))
  if (Array.isArray(cfg.postIds)) {
    for (const id of cfg.postIds) pushUnique(refs.postIds, String(id))
  }
  if (Array.isArray(cfg.postCodes)) {
    for (const code of cfg.postCodes) pushUnique(refs.postIds, String(code))
  }
}

export function collectDesignAssigneeRefs(nodes: WorkflowDesignNode[]): DesignAssigneeRefs {
  const refs: DesignAssigneeRefs = {
    userIds: [],
    roleCodes: [],
    deptIds: [],
    postIds: [],
  }
  const walk = (list: WorkflowDesignNode[]) => {
    for (const n of list || []) {
      const a = parseAssignee(n)
      const cfg = parseStrategyConfigRecord(n.strategyConfig)
      collectOrgRefsFromConfig(cfg, refs)
      if (a.strategyType === 'fixed_user') {
        pushUnique(refs.userIds, a.userId)
      } else if (a.strategyType === 'role' || a.strategyType === 'starter_dept_role') {
        for (const code of a.roleCodes || []) pushUnique(refs.roleCodes, code)
      } else if (a.strategyType === 'expression') {
        const expr = (a.expr || '').trim()
        // 表达式直接写用户 ID 时一并解析
        if (/^\d{10,}$/.test(expr)) pushUnique(refs.userIds, expr)
      }
      if (isConditionLikeGateway(n.nodeType)) {
        for (const b of ensureConditionBranches(n).branches || []) walk(b.nodes || [])
      } else if (n.nodeType === 'parallel') {
        for (const b of ensureParallelBranches(n).branches || []) walk(b.nodes || [])
      }
    }
  }
  walk(nodes)
  return refs
}

function normalizeNameResolvers(
  roleNameOfOrResolvers?: ((code: string) => string) | AssigneeNameResolvers,
): AssigneeNameResolvers {
  if (typeof roleNameOfOrResolvers === 'function') {
    return { roleNameOf: roleNameOfOrResolvers }
  }
  return roleNameOfOrResolvers || {}
}

function formatOrgIds(
  ids: string[] | undefined,
  nameOf?: (id: string) => string,
  sep = ', ',
): string {
  if (!ids?.length) return ''
  return ids.map((id) => nameOf?.(id) || id).join(sep)
}

/** 办理人摘要（文案由调用方注入；名称解析优先昵称/角色名/部门名/岗位名） */
export function formatAssigneeSummary(
  node: WorkflowDesignNode,
  roleNameOfOrResolvers?: ((code: string) => string) | AssigneeNameResolvers,
  labels?: AssigneeSummaryLabels,
): string {
  const resolvers = normalizeNameResolvers(roleNameOfOrResolvers)
  const type = node.nodeType || 'approve'
  if (isConditionLikeGateway(type)) {
    const ensured = ensureConditionBranches(node)
    const yesList = (ensured.branches || []).filter((b) => !b.isDefault)
    if (!yesList.length) return node.conditionExpression || ''
    if (yesList.length === 1) return yesList[0].expression || node.conditionExpression || ''
    const filled = yesList.filter((b) => b.expression?.trim()).length
    return labels?.conditionBranches(yesList.length, filled) ?? `${yesList.length} branches`
  }
  if (type === 'parallel') {
    const n = ensureParallelBranches(node).branches?.length || 0
    return labels?.parallelPaths(n) ?? `${n} paths`
  }
  if (type === 'delay') {
    return node.dueDuration?.trim() || '—'
  }
  if (type === 'service' || type === 'subprocess') {
    const cfg = (node.strategyConfig || '').trim()
    return cfg.length > 48 ? `${cfg.slice(0, 48)}…` : cfg || '—'
  }
  const a = parseAssignee(node)
  const cfg = parseStrategyConfigRecord(node.strategyConfig)
  const sep = labels?.listSep ?? ', '
  let base = '—'
  if (a.strategyType === 'fixed_user') {
    const uid = a.userId || ''
    base = (uid && resolvers.userNameOf?.(uid)) || uid || '—'
  } else if (a.strategyType === 'expression') {
    const expr = (a.expr || 'starter').trim() || 'starter'
    if (expr === 'starter') base = labels?.starter || 'starter'
    else if (/^\d{10,}$/.test(expr)) base = resolvers.userNameOf?.(expr) || expr
    else base = expr
  } else {
    const parts: string[] = []
    const roles = a.roleCodes || []
    if (roles.length) {
      parts.push(roles.map((c) => resolvers.roleNameOf?.(c) || c).join(sep))
    }
    const deptIds = [
      ...(cfg.deptId != null ? [String(cfg.deptId)] : []),
      ...(Array.isArray(cfg.deptIds) ? cfg.deptIds : []).map((id) => String(id)),
    ]
    const deptLabel = formatOrgIds(deptIds, resolvers.deptNameOf, sep)
    if (deptLabel) parts.push(deptLabel)
    const postIds = [
      ...(cfg.postId != null ? [String(cfg.postId)] : []),
      ...(Array.isArray(cfg.postIds) ? cfg.postIds : []).map((id) => String(id)),
      ...(Array.isArray(cfg.postCodes) ? cfg.postCodes : []).map((id) => String(id)),
    ]
    const postLabel = formatOrgIds(postIds, resolvers.postNameOf, sep)
    if (postLabel) parts.push(postLabel)
    if (parts.length) base = parts.join(sep)
  }
  const mode = normalizePassMode(node.passMode)
  if (mode === 'any') return `${base} · ${labels?.passAny ?? 'any'}`
  if (mode === 'all') return `${base} · ${labels?.passAll ?? 'all'}`
  if (mode === 'sequentialAll') return `${base} · ${labels?.passSequentialAll ?? 'sequentialAll'}`
  return base
}

export function isGatewayNodeType(type?: string): boolean {
  return isAnyGateway(type)
}
