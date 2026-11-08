/** Mirrors platform seed `workflow.approve.quick-comments` (FE fallback). */

export const WORKFLOW_APPROVE_QUICK_COMMENTS_KEY = 'workflow.approve.quick-comments'

export interface QuickCommentItem {
  id: string
  labels: Record<string, string>
}

export interface QuickCommentsConfig {
  pass: QuickCommentItem[]
  reject: QuickCommentItem[]
}

export const DEFAULT_QUICK_COMMENTS: QuickCommentsConfig = {
  pass: [
    { id: 'pass_agree', labels: { 'zh-CN': '同意', 'en-US': 'Agree' } },
    { id: 'pass_noted', labels: { 'zh-CN': '同意，请知悉', 'en-US': 'Agreed. Noted.' } },
  ],
  reject: [
    {
      id: 'reject_docs',
      labels: { 'zh-CN': '请补充证明材料', 'en-US': 'Please provide supporting documents' },
    },
    {
      id: 'reject_date',
      labels: {
        'zh-CN': '日期有误，请修改后重提',
        'en-US': 'Dates look incorrect. Please revise and resubmit.',
      },
    },
    {
      id: 'reject_reason',
      labels: { 'zh-CN': '事由不清晰，请完善说明', 'en-US': 'Please clarify the reason.' },
    },
  ],
}

export function resolveQuickCommentLabel(item: QuickCommentItem, locale: string): string {
  const labels = item.labels || {}
  return labels[locale] ?? labels['zh-CN'] ?? Object.values(labels)[0] ?? ''
}

export function parseQuickCommentsConfig(raw?: string | null): QuickCommentsConfig | null {
  if (!raw?.trim()) return null
  try {
    const parsed = JSON.parse(raw) as Partial<QuickCommentsConfig>
    const pass = Array.isArray(parsed.pass) ? parsed.pass.filter(isQuickItem) : []
    const reject = Array.isArray(parsed.reject) ? parsed.reject.filter(isQuickItem) : []
    if (!pass.length && !reject.length) return null
    return { pass, reject }
  } catch {
    return null
  }
}

function isQuickItem(v: unknown): v is QuickCommentItem {
  if (!v || typeof v !== 'object') return false
  const o = v as QuickCommentItem
  return typeof o.id === 'string' && !!o.labels && typeof o.labels === 'object'
}
