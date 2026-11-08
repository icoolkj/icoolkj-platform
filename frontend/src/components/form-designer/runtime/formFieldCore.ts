/**
 * Shared field render / validate kernel (V1.2 Runtime Host Contract).
 * FormRuntime and OaDynamicForm must consume this — no dual rule sources.
 */
import type { FormItemRule, FormRules } from 'element-plus'
import { FILE_BIZ_OA_INSTANCE } from '@/constants/file'
import type {
  FormComponentType,
  FormFieldDef,
  FormPreviewDevice,
  FormRuntimeHostContext,
} from '@/types/form-designer/schema'
import type { GenViewFormField } from '@/types/tool/genView'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'
import { isYes } from '@/constants/flags'
import { elColSpan as elColSpanOf } from '@/components/form-designer/schemaUtils'

export { elColSpanOf as elColSpan }

export type FormCoreComponent = GenViewFormField['component'] | 'switch'

export function mapFormComponent(componentType?: string): FormCoreComponent {
  const c = (componentType || 'input').toLowerCase()
  if (c === 'number' || c === 'input-number') return 'input-number'
  if (c === 'textarea') return 'textarea'
  if (c === 'richtext' || c === 'rich-editor') return 'rich-editor'
  if (c === 'select') return 'select'
  if (c === 'radio') return 'radio'
  if (c === 'checkbox') return 'checkbox'
  if (c === 'date') return 'date'
  if (c === 'datetime') return 'datetime'
  if (c === 'daterange' || c === 'date-range') return 'daterange'
  if (c === 'switch') return 'switch'
  if (c === 'file') return 'file'
  if (c === 'user') return 'user'
  if (c === 'dept') return 'dept'
  return 'input'
}

/** Empty HTML shells from editors count as blank for required checks. */
export function isBlankRichText(value: unknown): boolean {
  if (value == null) return true
  const text = String(value).trim()
  if (!text || text.toLowerCase() === 'null') return true
  return /^(?:\s|<p>(?:\s|&nbsp;|<br\s*\/?>)*<\/p>|<div>(?:\s|&nbsp;|<br\s*\/?>)*<\/div>|<br\s*\/?>)*$/i.test(
    text,
  )
}

/** FormRuntime renders el-switch; GenFormField path maps switch → radio. */
export function isNativeSwitch(componentType?: string): boolean {
  return (componentType || '').toLowerCase() === 'switch'
}

export function isSelectLikeComponent(component: FormCoreComponent | string): boolean {
  return (
    component === 'select' ||
    component === 'radio' ||
    component === 'date' ||
    component === 'datetime' ||
    component === 'daterange' ||
    component === 'checkbox' ||
    component === 'user' ||
    component === 'dept'
  )
}

/** Non-text values: skip string maxLength rules. */
function skipMaxLength(component: FormCoreComponent): boolean {
  return (
    component === 'file' ||
    component === 'rich-editor' ||
    component === 'daterange' ||
    component === 'user' ||
    component === 'dept' ||
    component === 'switch'
  )
}

function isNonEmptyArray(value: unknown): boolean {
  return Array.isArray(value) && value.length > 0
}

function isCompleteDateRange(value: unknown): boolean {
  return (
    Array.isArray(value) &&
    value.length === 2 &&
    Boolean(value[0]) &&
    Boolean(value[1])
  )
}

function isNonEmptyOrgRef(value: unknown): boolean {
  if (!Array.isArray(value) || !value.length) return false
  return value.some((item) => {
    if (item == null) return false
    if (typeof item === 'string') return Boolean(item.trim())
    if (typeof item === 'object' && 'id' in item) {
      return Boolean(String((item as { id?: unknown }).id ?? '').trim())
    }
    return false
  })
}

export function toGenFormConfig(
  field: Pick<FormFieldDef, 'fieldCode' | 'fieldType' | 'componentType' | 'dictType' | 'maxLength'>,
  host?: FormRuntimeHostContext,
): GenViewFormField {
  const componentType =
    field.fieldType === 'richText' ? 'richText' : field.componentType
  const mapped = mapFormComponent(componentType)
  // GenFormField: switch not in union — use radio for gen / L0 path
  const genComponent: GenViewFormField['component'] =
    mapped === 'switch' ? 'radio' : mapped
  const base: GenViewFormField = {
    field: field.fieldCode,
    component: genComponent,
    dictType: field.dictType,
    maxLength: field.maxLength,
    rows: genComponent === 'textarea' ? 3 : undefined,
    span: 24,
  }
  if (genComponent === 'file' || genComponent === 'rich-editor') {
    // OA host injects oa_instance + infra；公告 Gen view 自带 bizType/downloadScope
    base.bizType = host?.fileBizType || (genComponent === 'file' ? FILE_BIZ_OA_INSTANCE : undefined)
    base.downloadScope = host?.fileDownloadScope || (genComponent === 'file' ? 'infra' : undefined)
  }
  return base
}

export function toGenFormConfigFromOa(
  field: OaFieldDefVO,
  host?: FormRuntimeHostContext,
): GenViewFormField {
  const fieldTypeRaw = (field.fieldType || '').toLowerCase()
  return toGenFormConfig(
    {
      fieldCode: field.fieldCode!,
      fieldType: fieldTypeRaw === 'richtext' ? 'richText' : 'string',
      componentType: (field.componentType || 'input') as FormComponentType,
      dictType: field.dictType,
      maxLength: field.maxLength,
    },
    host,
  )
}

export interface BuildRulesOptions {
  host?: FormRuntimeHostContext
  /** i18n fallbacks when host resolvers absent */
  pleaseInput?: (label: string) => string
  pleaseSelect?: (label: string) => string
  /** Global readonly — skip required rules */
  readonly?: boolean
}

function labelOfField(field: FormFieldDef, host?: FormRuntimeHostContext): string {
  const fromResolver = host?.resolveLabel?.(field)?.trim()
  if (fromResolver) return fromResolver
  return field.fieldName || field.fieldCode || ''
}

export function labelOfFormField(field: FormFieldDef, host?: FormRuntimeHostContext): string {
  return labelOfField(field, host)
}

export function placeholderOfFormField(
  field: FormFieldDef,
  host?: FormRuntimeHostContext,
  pleaseSelect?: (label: string) => string,
  pleaseInput?: (label: string) => string,
): string {
  if (field.placeholder?.trim()) return field.placeholder
  const fromResolver = host?.resolvePlaceholder?.(field)?.trim()
  if (fromResolver) return fromResolver
  const name = labelOfField(field, host)
  const component = mapFormComponent(field.componentType)
  if (isSelectLikeComponent(component) || isNativeSwitch(field.componentType)) {
    return pleaseSelect?.(name) || name
  }
  return pleaseInput?.(name) || name
}

export function buildRulesForFields(
  fields: FormFieldDef[],
  options: BuildRulesOptions = {},
): FormRules {
  const result: FormRules = {}
  const host = options.host
  for (const field of fields) {
    if (!field.fieldCode) continue
    // Hidden / read-only / global readonly: no write validation
    if (field.fieldPerm === 'H' || field.fieldPerm === 'R' || options.readonly) {
      continue
    }
    const itemRules: FormItemRule[] = []
    const label = labelOfField(field, host)
    const component = mapFormComponent(field.componentType)
    if (field.required) {
      const message =
        host?.resolveRequiredMessage?.(field)?.trim() ||
        (isSelectLikeComponent(component) || isNativeSwitch(field.componentType)
          ? options.pleaseSelect?.(label) || label
          : options.pleaseInput?.(label) || label)
      if (component === 'file') {
        itemRules.push({
          required: true,
          validator: (_rule, value, callback) => {
            if (isNonEmptyArray(value)) callback()
            else callback(new Error(message))
          },
          trigger: ['blur', 'change'],
        })
      } else if (component === 'rich-editor') {
        itemRules.push({
          required: true,
          validator: (_rule, value, callback) => {
            if (isBlankRichText(value)) callback(new Error(message))
            else callback()
          },
          trigger: ['blur', 'change'],
        })
      } else if (component === 'daterange') {
        itemRules.push({
          required: true,
          validator: (_rule, value, callback) => {
            if (isCompleteDateRange(value)) callback()
            else callback(new Error(message))
          },
          trigger: ['blur', 'change'],
        })
      } else if (component === 'user' || component === 'dept') {
        itemRules.push({
          required: true,
          validator: (_rule, value, callback) => {
            if (isNonEmptyOrgRef(value)) callback()
            else callback(new Error(message))
          },
          trigger: ['blur', 'change'],
        })
      } else {
        itemRules.push({
          required: true,
          message,
          trigger: ['blur', 'change'],
        })
      }
    }
    // HTML length ≠ visible text; OA richText body lives in content_text (mediumtext)
    if (field.maxLength && field.maxLength > 0 && !skipMaxLength(component)) {
      const max = field.maxLength
      const message =
        host?.resolveMaxLengthMessage?.(field, max)?.trim() || `${label} (${max})`
      itemRules.push({
        max,
        message,
        trigger: 'blur',
      })
    }
    if (itemRules.length) {
      result[field.fieldCode] = itemRules
    }
  }
  return result
}

/** Rules from OA field rows (fallback path). */
export function buildRulesForOaFields(
  fields: OaFieldDefVO[],
  options: BuildRulesOptions & {
    resolveLabel?: (field: OaFieldDefVO) => string
    resolveRequiredMessage?: (field: OaFieldDefVO) => string
    resolveMaxLengthMessage?: (field: OaFieldDefVO, max: number) => string
  } = {},
): FormRules {
  const mapped: FormFieldDef[] = fields
    .filter((f) => f.fieldCode)
    .map((f) => {
      const componentType = (f.componentType || 'input') as FormComponentType
      const fieldTypeRaw = (f.fieldType || '').toLowerCase()
      const fieldType =
        fieldTypeRaw === 'richtext' || componentType === 'richText'
          ? ('richText' as const)
          : ('string' as const)
      return {
        id: f.id || f.fieldCode!,
        fieldCode: f.fieldCode!,
        fieldName: options.resolveLabel?.(f)?.trim() || f.fieldName || f.fieldCode!,
        fieldType,
        componentType,
        required: isYes(f.fieldRequired),
        dictType: f.dictType,
        maxLength: f.maxLength,
        fieldPerm: f.fieldPerm,
      }
    })
  return buildRulesForFields(mapped, {
    host: {
      resolveLabel: (field) => field.fieldName,
      resolveRequiredMessage: (field) => {
        const oa = fields.find((f) => f.fieldCode === field.fieldCode)
        return oa ? options.resolveRequiredMessage?.(oa)?.trim() || '' : ''
      },
      resolveMaxLengthMessage: (field, max) => {
        const oa = fields.find((f) => f.fieldCode === field.fieldCode)
        return oa ? options.resolveMaxLengthMessage?.(oa, max)?.trim() || '' : ''
      },
    },
    pleaseInput: options.pleaseInput,
    pleaseSelect: options.pleaseSelect,
    readonly: options.readonly,
  })
}

/** Map a 24-grid span value for the given preview device. */
export function gridSpan(span: number | undefined, device: FormPreviewDevice): number {
  return elColSpanOf({ span: span ?? 24 } as FormFieldDef, device)
}
