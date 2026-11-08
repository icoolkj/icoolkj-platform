/** 条件表达式 UI 状态（设计器分支 cap 表单） */
export type ConditionExprMode = 'compare' | 'range'

export interface ConditionExprUi {
  mode: ConditionExprMode
  field: string
  op: string
  value: string
  value2: string
}

export const EMPTY_CONDITION_EXPR_UI: ConditionExprUi = {
  mode: 'compare',
  field: '',
  op: '>',
  value: '',
  value2: '',
}

const RANGE_PATTERN = /\$\{\s*([a-zA-Z_][\w]*)\s*(>|>=)\s*(.+?)\s*&&\s*\1\s*(<|<=)\s*(.+?)\s*\}/
const COMPARE_PATTERN = /\$\{\s*([a-zA-Z_][\w]*)\s*(>=|<=|==|!=|>|<)\s*(.+?)\s*\}/

/** 将 EL 解析为表单状态；无法识别时仅保留 compare 模式与空字段 */
export function parseConditionExpressionUi(expression?: string | null): ConditionExprUi {
  const expr = (expression || '').trim()
  if (!expr) return { ...EMPTY_CONDITION_EXPR_UI }

  const range = expr.match(RANGE_PATTERN)
  if (range) {
    return {
      mode: 'range',
      field: range[1],
      op: '>',
      value: range[3].trim(),
      value2: range[5].trim(),
    }
  }

  const compare = expr.match(COMPARE_PATTERN)
  if (compare) {
    return {
      mode: 'compare',
      field: compare[1],
      op: compare[2],
      value: compare[3].trim(),
      value2: '',
    }
  }

  return { ...EMPTY_CONDITION_EXPR_UI }
}

function formatConditionLiteral(raw: string): string {
  const t = raw.trim()
  if (!t) return t
  const numeric = /^-?\d+(\.\d+)?$/.test(t)
  return numeric ? t : `'${t.replace(/'/g, "\\'")}'`
}

/** 由表单状态生成 EL；字段或右值缺失时返回 null */
export function buildConditionExpression(ui: ConditionExprUi): string | null {
  if (!ui.field.trim()) return null

  if (ui.mode === 'range') {
    const lo = formatConditionLiteral(ui.value)
    const hi = formatConditionLiteral(ui.value2)
    if (!lo || !hi) return null
    return `\${${ui.field} > ${lo} && ${ui.field} <= ${hi}}`
  }

  const right = formatConditionLiteral(ui.value)
  if (!right) return null
  return `\${${ui.field} ${ui.op} ${right}}`
}

/** 区间模式下校验数值上下界（仅当两侧均为有限数字时） */
export function isValidConditionRange(ui: ConditionExprUi): boolean {
  if (ui.mode !== 'range') return true
  const lo = Number(ui.value)
  const hi = Number(ui.value2)
  if (!ui.value.trim() || !ui.value2.trim()) return true
  if (!Number.isFinite(lo) || !Number.isFinite(hi)) return true
  return lo < hi
}
