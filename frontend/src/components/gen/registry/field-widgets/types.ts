import type { DictOption } from '@/types/system/dict'
import type { GenViewFormField, GenViewQueryField } from '@/types/tool/genView'
import type { UserProfile } from '@/types/profile'

/** 字段 Widget 统一上下文 — 由 GenFormField 组装后传入子组件 */
export type GenFieldWidgetContext = {
  config: GenViewQueryField | GenViewFormField
  formConfig: GenViewFormField
  modelValue?: unknown
  placeholder?: string
  mode: 'query' | 'form'
  disabled?: boolean
  dictOptions: DictOption[]
  dictModelValue: unknown
  getDict?: (dictType: string) => DictOption[]
  getFieldOptions?: (field: string) => DictOption[] | undefined
  fieldStyle?: Record<string, string>
  treeData?: Record<string, unknown>[]
  treeProps: { label: string; value: string; children: string }
  formModel?: Record<string, unknown>
  gp: (key: string, params?: Record<string, string | number>) => string
  dateRangeModel?: [string, string]
  dateRangeStartPlaceholder: string
  dateRangeEndPlaceholder: string
  checkboxValues: string[]
  emitDictValue: (value: unknown) => void
  emitDateRange: (value: unknown) => void
  emitCheckboxValues: (values: string[]) => void
}

export type GenFieldWidgetEmits = {
  'update:modelValue': [value: unknown]
  enter: []
  'avatar-uploaded': [payload: UserProfile]
}

export const genFieldWidgetProps = [
  'ctx',
] as const
