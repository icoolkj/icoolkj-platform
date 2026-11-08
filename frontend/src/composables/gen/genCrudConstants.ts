import type { GenViewFormField, GenViewQueryField } from '@/types/tool/genView'

export function resolveGenTreeData(
  field: GenViewFormField | GenViewQueryField,
  treeSources: Record<string, Record<string, unknown>[]>,
) {
  const component = field.component
  const treeSource = 'treeSource' in field ? field.treeSource : undefined
  if (component !== 'tree-select' && !treeSource) return []
  const key = treeSource ?? 'self'
  return treeSources[key] ?? []
}

export function resolveGenTreeProps(field: GenViewFormField | GenViewQueryField) {
  const label = 'treeLabelField' in field && field.treeLabelField ? field.treeLabelField : 'label'
  const value = 'treeValueField' in field && field.treeValueField ? field.treeValueField : 'id'
  return { label, value, children: 'children' as const }
}

export const GEN_DETAIL_DIALOG_ID = 'GenDetailDialog'

export const GEN_DIALOG_ID_PROP: Record<string, string> = {
  GenDetailDialog: 'id',
  GenUserRoleDialog: 'userId',
  GenMenuAuthDialog: 'roleId',
  GenNoticeDetailDialog: 'noticeId',
  GenSiteMessageDetailDialog: 'messageId',
  GenAiInvokeLogDetailDialog: 'id',
  GenAiDdlLogDetailDialog: 'id',
  GenAiChatLogDetailDialog: 'id',
}

export const GEN_INTERACTIVE_DIALOG_CHROME = new Set(['GenUserRoleDialog', 'GenMenuAuthDialog'])
