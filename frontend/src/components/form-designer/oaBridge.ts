import {
  cloneFormDocument,
  createEmptyFormDocument,
  createFieldDef,
  flattenFields,
  nextFormNodeId,
  normalizeSpan,
  parseLayoutPersistence,
  toLayoutPersistence,
} from '@/components/form-designer/schemaUtils'
import type {
  FormComponentType,
  FormDocument,
  FormFieldDef,
  FormFieldPresentationRef,
  FormFieldType,
  FormLayoutNode,
} from '@/types/form-designer/schema'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'
import { YES } from '@/constants/flags'

export type FieldPermMap = Map<string, 'R' | 'W' | 'H'> | Record<string, 'R' | 'W' | 'H'>

export interface ComposeBoundOptions {
  /** Append field-pool codes missing from layout as root field nodes. Default true. */
  appendMissingFields?: boolean
  /** Keep empty card nodes. Default true (V1.2). */
  keepEmptyCards?: boolean
}

export interface ComposeBoundInput {
  /** Full contract pool — never a writable-only subset. */
  fields: OaFieldDefVO[]
  layoutJson?: string | null
  /** Runtime perms by fieldCode (optional). */
  fieldPerms?: FieldPermMap
  options?: ComposeBoundOptions
}

export interface RuntimePolicyOptions {
  /** Force all visible fields to R. */
  readonly?: boolean
  /** Drop H fields from containers; keep empty cards. Default true. */
  hideHidden?: boolean
}

function mapComponentType(raw?: string): FormComponentType {
  const v = (raw || 'input').toLowerCase()
  if (v === 'rich-editor' || v === 'richtext') return 'richText'
  if (v === 'daterange' || v === 'date-range') return 'dateRange'
  const allowed: FormComponentType[] = [
    'input',
    'textarea',
    'richText',
    'select',
    'radio',
    'checkbox',
    'date',
    'datetime',
    'dateRange',
    'number',
    'switch',
    'file',
    'user',
    'dept',
  ]
  return (allowed.includes(v as FormComponentType) ? v : 'input') as FormComponentType
}

function mapFieldType(raw?: string, componentType?: FormComponentType): FormFieldType {
  const v = (raw || '').toLowerCase()
  if (v === 'richtext') return 'richText'
  if (v === 'daterange' || v === 'date-range') return 'dateRange'
  const allowed: FormFieldType[] = [
    'string',
    'int',
    'decimal',
    'date',
    'datetime',
    'dateRange',
    'bool',
    'file',
    'richText',
    'ref',
  ]
  if (allowed.includes(v as FormFieldType)) {
    return v as FormFieldType
  }
  if (componentType === 'number') return 'int'
  if (componentType === 'date') return 'date'
  if (componentType === 'datetime') return 'datetime'
  if (componentType === 'dateRange') return 'dateRange'
  if (componentType === 'switch' || componentType === 'checkbox') return 'bool'
  if (componentType === 'file') return 'file'
  if (componentType === 'richText') return 'richText'
  if (componentType === 'user' || componentType === 'dept') return 'ref'
  return 'string'
}

function permMapOf(perms?: FieldPermMap): Map<string, 'R' | 'W' | 'H'> {
  if (!perms) return new Map()
  if (perms instanceof Map) return perms
  return new Map(Object.entries(perms))
}

export function oaFieldToFormField(field: OaFieldDefVO): FormFieldDef {
  const componentType = mapComponentType(field.componentType)
  return createFieldDef({
    id: field.id || nextFormNodeId('f'),
    fieldCode: field.fieldCode,
    fieldName: field.fieldName,
    fieldType: mapFieldType(field.fieldType, componentType),
    componentType,
    required: field.fieldRequired === YES || field.fieldRequired === '1',
    dictType: field.dictType,
    maxLength: field.maxLength,
    defaultValue: field.defaultValue,
    span: 24,
    platformLocked: field.platformLocked === YES || field.platformLocked === '1',
    fieldPerm: field.fieldPerm,
  })
}

function hydrateRef(
  ref: FormFieldPresentationRef,
  live: FormFieldDef,
  perms: Map<string, 'R' | 'W' | 'H'>,
): FormFieldDef {
  const perm = perms.get(live.fieldCode) ?? live.fieldPerm
  return {
    ...live,
    span: normalizeSpan(ref.span),
    placeholder: ref.placeholder ?? live.placeholder,
    fieldPerm: perm,
  }
}

/**
 * Bound Composition API (V1.2): full field pool + presentation layout → FormDocument.
 */
export function composeBoundDocument(input: ComposeBoundInput): FormDocument {
  const appendMissing = input.options?.appendMissingFields !== false
  const keepEmptyCards = input.options?.keepEmptyCards !== false
  const pool = input.fields.map(oaFieldToFormField)
  const byCode = new Map(pool.map((f) => [f.fieldCode, f]))
  const perms = permMapOf(input.fieldPerms)
  // Also accept fieldPerm already on VO rows
  for (const f of pool) {
    if (f.fieldPerm && !perms.has(f.fieldCode)) {
      perms.set(f.fieldCode, f.fieldPerm)
    }
  }

  if (input.layoutJson?.trim()) {
    try {
      const persisted = parseLayoutPersistence(JSON.parse(input.layoutJson))
      const used = new Set<string>()
      const nodes: FormLayoutNode[] = []
      for (const node of persisted.nodes) {
        if (node.type === 'divider') {
          nodes.push({ type: 'divider', id: node.id || nextFormNodeId('node'), title: node.title })
          continue
        }
        if (node.type === 'field') {
          const live = byCode.get(node.fieldCode)
          if (!live) continue
          used.add(live.fieldCode)
          nodes.push({
            type: 'field',
            id: node.id || nextFormNodeId('node'),
            field: hydrateRef(node, live, perms),
          })
          continue
        }
        if (node.type === 'row' || node.type === 'card') {
          const children: FormFieldDef[] = []
          for (const ref of node.children) {
            const live = byCode.get(ref.fieldCode)
            if (!live) continue
            used.add(live.fieldCode)
            children.push(hydrateRef(ref, live, perms))
          }
          if (node.type === 'row') {
            if (!children.length) continue
            nodes.push({ type: 'row', id: node.id || nextFormNodeId('node'), children })
          } else if (children.length || keepEmptyCards) {
            nodes.push({
              type: 'card',
              id: node.id || nextFormNodeId('node'),
              title: node.title,
              children,
            })
          }
        }
      }
      if (appendMissing) {
        for (const f of pool) {
          if (!used.has(f.fieldCode)) {
            const perm = perms.get(f.fieldCode)
            nodes.push({
              type: 'field',
              id: nextFormNodeId('node'),
              field: perm ? { ...f, fieldPerm: perm } : f,
            })
          }
        }
      }
      return {
        schemaVersion: 1,
        form: persisted.form,
        nodes,
      }
    } catch {
      /* fall through */
    }
  }

  const doc = createEmptyFormDocument()
  doc.nodes = pool.map((field) => {
    const perm = perms.get(field.fieldCode)
    return {
      type: 'field' as const,
      id: nextFormNodeId('node'),
      field: perm ? { ...field, fieldPerm: perm, span: 24 } : { ...field, span: 24 },
    }
  })
  return doc
}

/** Persist presentation only (Layout Persistence Contract). */
export function persistLayout(doc: FormDocument): string {
  const cleaned = cloneFormDocument(doc)
  for (const field of flattenFields(cleaned)) {
    delete field.fieldPerm
  }
  return JSON.stringify(toLayoutPersistence(cleaned))
}

export function validateBoundDocument(
  doc: FormDocument,
  fieldPool: OaFieldDefVO[],
): { ok: true } | { ok: false; reason: string } {
  const codes = new Set(fieldPool.map((f) => f.fieldCode))
  const seen = new Set<string>()
  for (const field of flattenFields(doc)) {
    if (!codes.has(field.fieldCode)) {
      return { ok: false, reason: `unknown_field:${field.fieldCode}` }
    }
    if (seen.has(field.fieldCode)) {
      return { ok: false, reason: `duplicate_field:${field.fieldCode}` }
    }
    seen.add(field.fieldCode)
  }
  return { ok: true }
}

/** Apply readonly / hide-H policy for runtime hosts. Keeps empty cards. */
export function applyRuntimePolicy(
  doc: FormDocument,
  options: RuntimePolicyOptions = {},
): FormDocument {
  const hideHidden = options.hideHidden !== false
  const next = cloneFormDocument(doc)
  next.nodes = next.nodes
    .map((node) => {
      if (node.type === 'divider') {
        return node
      }
      if (node.type === 'field') {
        if (hideHidden && node.field.fieldPerm === 'H') return null
        if (options.readonly) {
          return { ...node, field: { ...node.field, fieldPerm: 'R' as const } }
        }
        return node
      }
      const children = node.children
        .filter((c) => !(hideHidden && c.fieldPerm === 'H'))
        .map((c) => (options.readonly ? { ...c, fieldPerm: 'R' as const } : c))
      if (node.type === 'row' && !children.length) return null
      return { ...node, children }
    })
    .filter((n): n is NonNullable<typeof n> => Boolean(n))
  return next
}

/** Inject runtime fieldPerm into document fields (not persisted). */
export function mergeFieldPermsIntoDocument(
  doc: FormDocument,
  fields: OaFieldDefVO[],
): FormDocument {
  const perms: Record<string, 'R' | 'W' | 'H'> = {}
  for (const f of fields) {
    if (f.fieldPerm) perms[f.fieldCode] = f.fieldPerm
  }
  return composeBoundDocument({
    fields,
    layoutJson: persistLayout(doc),
    fieldPerms: perms,
    options: { appendMissingFields: false, keepEmptyCards: true },
  })
}

/**
 * @deprecated Use composeBoundDocument — kept for call-site stability.
 */
export function fieldsToDocument(
  fields: OaFieldDefVO[],
  layoutJson?: string | null,
): FormDocument {
  return composeBoundDocument({ fields, layoutJson })
}

/**
 * @deprecated Use persistLayout — kept for call-site stability.
 */
export function documentToLayoutJson(doc: FormDocument): string {
  return persistLayout(doc)
}

export function unusedFieldsInPool(
  doc: FormDocument,
  fieldPool: OaFieldDefVO[],
): OaFieldDefVO[] {
  const used = new Set(flattenFields(doc).map((f) => f.fieldCode))
  return fieldPool.filter((f) => !used.has(f.fieldCode))
}
