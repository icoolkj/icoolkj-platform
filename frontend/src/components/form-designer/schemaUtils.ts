import type {
  FormComponentType,
  FormDocument,
  FormFieldDef,
  FormFieldPresentationRef,
  FormFieldType,
  FormLayoutNode,
  FormLayoutPersistence,
  FormMeta,
  FormPersistedLayoutNode,
} from '@/types/form-designer/schema'
import {
  DEFAULT_FORM_META,
  FORM_DOCUMENT_SCHEMA_VERSION,
  FORM_LAYOUT_PRESENTATION_VERSION,
} from '@/types/form-designer/schema'

let seq = 0

export function nextFormNodeId(prefix = 'n'): string {
  seq += 1
  return `${prefix}_${Date.now().toString(36)}_${seq}`
}

export function createEmptyFormDocument(meta?: Partial<FormMeta>): FormDocument {
  return {
    schemaVersion: FORM_DOCUMENT_SCHEMA_VERSION,
    form: { ...DEFAULT_FORM_META, ...meta },
    nodes: [],
  }
}

export function createFieldDef(
  partial: Partial<FormFieldDef> & Pick<FormFieldDef, 'fieldCode' | 'fieldName' | 'componentType'>,
): FormFieldDef {
  return {
    id: partial.id ?? nextFormNodeId('f'),
    fieldCode: partial.fieldCode,
    fieldName: partial.fieldName,
    fieldType: partial.fieldType ?? inferFieldType(partial.componentType),
    componentType: partial.componentType,
    required: partial.required ?? false,
    dictType: partial.dictType,
    maxLength: partial.maxLength,
    defaultValue: partial.defaultValue,
    placeholder: partial.placeholder,
    span: partial.span !== undefined ? normalizeSpan(partial.span) : 24,
    platformLocked: partial.platformLocked,
    fieldPerm: partial.fieldPerm,
  }
}

export function inferFieldType(componentType: FormComponentType): FormFieldType {
  switch (componentType) {
    case 'number':
      return 'int'
    case 'date':
      return 'date'
    case 'datetime':
      return 'datetime'
    case 'dateRange':
      return 'dateRange'
    case 'switch':
    case 'checkbox':
      return 'bool'
    case 'file':
      return 'file'
    case 'richText':
      return 'richText'
    case 'user':
    case 'dept':
      return 'ref'
    default:
      return 'string'
  }
}

/** Migrate-only: legacy layout logical 1|2|3 → 24-grid (thirds). Not a hot-path API. */
function legacyColSpanToSpan(colSpan?: number): number {
  if (colSpan === 3) return 24
  if (colSpan === 2) return 16
  return 8
}

export function normalizeSpan(raw?: number): number {
  const n = Number(raw)
  if (!Number.isFinite(n)) return 24
  return Math.min(24, Math.max(1, Math.round(n)))
}

/** Resolve presentation span (span-only SoT after presentationVersion 2). */
export function resolveFieldSpan(field: Pick<FormFieldDef, 'span'>): number {
  return normalizeSpan(field.span ?? 24)
}

/** Map field presentation span to Element Plus 24-grid. */
export function elColSpan(field: FormFieldDef, device: 'pc' | 'tablet' | 'mobile'): number {
  if (device === 'mobile') {
    return 24
  }
  const span = resolveFieldSpan(field)
  if (device === 'tablet') {
    return span >= 12 ? 24 : 12
  }
  return span
}

/** Sum of child spans in a row/card (for designer hints). */
export function sumChildrenSpan(children: FormFieldDef[]): number {
  return children.reduce((acc, c) => acc + resolveFieldSpan(c), 0)
}

export function flattenFields(doc: FormDocument): FormFieldDef[] {
  const out: FormFieldDef[] = []
  for (const node of doc.nodes) {
    if (node.type === 'field') {
      out.push(node.field)
    } else if (node.type === 'row' || node.type === 'card') {
      out.push(...node.children)
    }
  }
  return out
}

export function cloneFormDocument(doc: FormDocument): FormDocument {
  return JSON.parse(JSON.stringify(doc)) as FormDocument
}

export function parseFormDocument(raw: unknown): FormDocument {
  if (!raw || typeof raw !== 'object') {
    throw new Error('invalid_form_document')
  }
  const doc = raw as FormDocument
  if (doc.schemaVersion !== 1 || !doc.form || !Array.isArray(doc.nodes)) {
    throw new Error('unsupported_form_document')
  }
  return doc
}

function toPresentationRef(field: FormFieldDef): FormFieldPresentationRef {
  return {
    fieldCode: field.fieldCode,
    span: resolveFieldSpan(field),
    placeholder: field.placeholder,
  }
}

/** Strip contract mirrors → Layout Persistence Contract (presentationVersion 2). */
export function toLayoutPersistence(doc: FormDocument): FormLayoutPersistence {
  const nodes: FormPersistedLayoutNode[] = []
  for (const node of doc.nodes) {
    if (node.type === 'divider') {
      nodes.push({ type: 'divider', id: node.id, title: node.title })
      continue
    }
    if (node.type === 'field') {
      nodes.push({
        type: 'field',
        id: node.id,
        ...toPresentationRef(node.field),
      })
      continue
    }
    if (node.type === 'row') {
      nodes.push({
        type: 'row',
        id: node.id,
        children: node.children.map(toPresentationRef),
      })
      continue
    }
    nodes.push({
      type: 'card',
      id: node.id,
      title: node.title,
      children: node.children.map(toPresentationRef),
    })
  }
  return {
    schemaVersion: FORM_DOCUMENT_SCHEMA_VERSION,
    presentationVersion: FORM_LAYOUT_PRESENTATION_VERSION,
    form: {
      labelWidth: doc.form.labelWidth ?? DEFAULT_FORM_META.labelWidth,
      labelPosition: doc.form.labelPosition ?? DEFAULT_FORM_META.labelPosition,
      size: doc.form.size ?? DEFAULT_FORM_META.size,
      labelAlign: doc.form.labelAlign ?? DEFAULT_FORM_META.labelAlign,
      hideRequiredAsterisk: Boolean(doc.form.hideRequiredAsterisk),
    },
    nodes,
  }
}

/**
 * One-time v1 → v2 upgrade (migrate-only). Hot path after v2: clamp span only.
 * Rules: see docs/architecture/36-form-designer-sdk.md presentationVersion 2.
 */
export function upgradeLayoutToV2(raw: unknown): FormLayoutPersistence {
  if (!raw || typeof raw !== 'object') {
    throw new Error('invalid_form_layout')
  }
  const doc = raw as {
    schemaVersion?: number
    presentationVersion?: number
    form?: FormMeta
    nodes?: unknown[]
  }
  if (doc.schemaVersion !== 1 || !doc.form || !Array.isArray(doc.nodes)) {
    throw new Error('unsupported_form_layout')
  }
  const fromV1 = doc.presentationVersion !== FORM_LAYOUT_PRESENTATION_VERSION
  const nodes: FormPersistedLayoutNode[] = []
  for (const item of doc.nodes) {
    if (!item || typeof item !== 'object') continue
    const node = item as Record<string, unknown>
    const type = node.type as string
    const id = typeof node.id === 'string' ? node.id : nextFormNodeId('node')
    if (type === 'divider') {
      nodes.push({
        type: 'divider',
        id,
        title: typeof node.title === 'string' ? node.title : undefined,
      })
      continue
    }
    if (type === 'field') {
      const fieldObj = (node.field as Record<string, unknown> | undefined) ?? node
      const fieldCode = String(fieldObj.fieldCode ?? '')
      if (!fieldCode) continue
      nodes.push({
        type: 'field',
        id,
        fieldCode,
        span: resolveUpgradedSpan(fieldObj, { fromV1, role: 'root' }),
        placeholder:
          typeof fieldObj.placeholder === 'string' ? fieldObj.placeholder : undefined,
      })
      continue
    }
    if (type === 'row' || type === 'card') {
      const childrenRaw = Array.isArray(node.children) ? node.children : []
      const children: FormFieldPresentationRef[] = []
      for (const c of childrenRaw) {
        if (!c || typeof c !== 'object') continue
        const child = c as Record<string, unknown>
        const fieldCode = String(child.fieldCode ?? '')
        if (!fieldCode) continue
        children.push({
          fieldCode,
          span: resolveUpgradedSpan(child, { fromV1, role: 'child' }),
          placeholder: typeof child.placeholder === 'string' ? child.placeholder : undefined,
        })
      }
      if (type === 'row') {
        nodes.push({ type: 'row', id, children })
      } else {
        nodes.push({
          type: 'card',
          id,
          title: typeof node.title === 'string' ? node.title : undefined,
          children,
        })
      }
    }
  }
  return {
    schemaVersion: FORM_DOCUMENT_SCHEMA_VERSION,
    presentationVersion: FORM_LAYOUT_PRESENTATION_VERSION,
    form: { ...DEFAULT_FORM_META, ...doc.form },
    nodes,
  }
}

function resolveUpgradedSpan(
  obj: Record<string, unknown>,
  ctx: { fromV1: boolean; role: 'root' | 'child' },
): number {
  const hasSpan = obj.span !== undefined && obj.span !== null && obj.span !== ''
  const hasCol = obj.colSpan !== undefined && obj.colSpan !== null && obj.colSpan !== ''

  if (!ctx.fromV1) {
    if (hasSpan) return normalizeSpan(Number(obj.span))
    return ctx.role === 'child' ? 8 : 24
  }

  // --- v1 → v2 migrate ---
  if (hasSpan) {
    const n = Number(obj.span)
    if (n === 1 || n === 2 || n === 3) {
      return legacyColSpanToSpan(n)
    }
    let span = normalizeSpan(n)
    if (ctx.role === 'root' && span === 8) {
      return 24
    }
    return span
  }
  if (hasCol) {
    let span = legacyColSpanToSpan(Number(obj.colSpan))
    if (ctx.role === 'root' && span === 8) {
      return 24
    }
    return span
  }
  return ctx.role === 'child' ? 8 : 24
}

/** Read persistence: always returns presentationVersion 2 (upgrades v1 once). */
export function parseLayoutPersistence(raw: unknown): FormLayoutPersistence {
  return upgradeLayoutToV2(raw)
}

const MAX_CONTAINER_CHILDREN = 6

/**
 * Create-path defaults (not runtime heal):
 * - into row/card → span 8 when incoming is full-width (≥16)
 * - root → span 24 when incoming looks like pool default
 */
export function appendFieldPreferContainer(
  doc: FormDocument,
  field: FormFieldDef,
  containerId?: string,
  atIndex?: number,
): FormDocument {
  const next = cloneFormDocument(doc)
  if (containerId) {
    const container = next.nodes.find((n) => n.id === containerId)
    if (
      container &&
      (container.type === 'card' || container.type === 'row') &&
      container.children.length < MAX_CONTAINER_CHILDREN
    ) {
      const child = { ...field }
      if (resolveFieldSpan(child) >= 16) {
        child.span = 8
      }
      container.children.push(child)
      return next
    }
  }
  const root = { ...field, span: 24 }
  const node: FormLayoutNode = { type: 'field', id: nextFormNodeId('node'), field: root }
  if (atIndex === undefined || atIndex < 0 || atIndex > next.nodes.length) {
    next.nodes.push(node)
  } else {
    next.nodes.splice(atIndex, 0, node)
  }
  return next
}

export function appendFieldNode(doc: FormDocument, field: FormFieldDef): FormDocument {
  const next = cloneFormDocument(doc)
  next.nodes.push({ type: 'field', id: nextFormNodeId('node'), field })
  return next
}

export function appendLayoutNode(doc: FormDocument, node: FormLayoutNode): FormDocument {
  const next = cloneFormDocument(doc)
  next.nodes.push(node)
  return next
}

export function insertLayoutNode(
  doc: FormDocument,
  node: FormLayoutNode,
  atIndex?: number,
): FormDocument {
  const next = cloneFormDocument(doc)
  if (atIndex === undefined || atIndex < 0 || atIndex > next.nodes.length) {
    next.nodes.push(node)
  } else {
    next.nodes.splice(atIndex, 0, node)
  }
  return next
}

function normalizeFieldPatch(patch: Partial<FormFieldDef>): Partial<FormFieldDef> {
  const next = { ...patch }
  if (next.span !== undefined) {
    next.span = normalizeSpan(next.span)
  }
  return next
}

/** Mutate field in place (keeps object identity — important for designer perf). */
export function applyFieldPatch(field: FormFieldDef, patch: Partial<FormFieldDef>) {
  const normalized = normalizeFieldPatch(patch)
  Object.assign(field, normalized)
  if (normalized.componentType && !normalized.fieldType) {
    field.fieldType = inferFieldType(normalized.componentType)
  }
}

function patchFieldInList(list: FormFieldDef[], fieldId: string, patch: Partial<FormFieldDef>) {
  const child = list.find((c) => c.id === fieldId)
  if (!child) {
    return false
  }
  applyFieldPatch(child, patch)
  return true
}

export function updateFieldInDocument(
  doc: FormDocument,
  fieldId: string,
  patch: Partial<FormFieldDef>,
): FormDocument {
  const next = cloneFormDocument(doc)
  for (const node of next.nodes) {
    if (node.type === 'field' && node.field.id === fieldId) {
      applyFieldPatch(node.field, patch)
      return next
    }
    if (node.type === 'row' || node.type === 'card') {
      if (patchFieldInList(node.children, fieldId, patch)) {
        return next
      }
    }
  }
  return next
}

export function updateNodeMeta(
  doc: FormDocument,
  nodeId: string,
  patch: { title?: string },
): FormDocument {
  const next = cloneFormDocument(doc)
  const node = next.nodes.find((n) => n.id === nodeId)
  if (!node) {
    return next
  }
  if (node.type === 'card' || node.type === 'divider') {
    if (patch.title !== undefined) {
      node.title = patch.title
    }
  }
  return next
}

export function removeNode(doc: FormDocument, nodeId: string): FormDocument {
  const next = cloneFormDocument(doc)
  next.nodes = next.nodes.filter((n) => n.id !== nodeId)
  return next
}

/** Remove one field from a row/card; empty row is removed, empty card kept. */
export function removeChildFromContainer(
  doc: FormDocument,
  containerId: string,
  fieldId: string,
): FormDocument {
  const next = cloneFormDocument(doc)
  const idx = next.nodes.findIndex((n) => n.id === containerId)
  if (idx < 0) return next
  const container = next.nodes[idx]
  if (!container || (container.type !== 'row' && container.type !== 'card')) {
    return next
  }
  container.children = container.children.filter((c) => c.id !== fieldId)
  if (container.type === 'row' && container.children.length === 0) {
    next.nodes.splice(idx, 1)
  }
  return next
}

export function moveNode(doc: FormDocument, nodeId: string, delta: -1 | 1): FormDocument {
  const next = cloneFormDocument(doc)
  const idx = next.nodes.findIndex((n) => n.id === nodeId)
  if (idx < 0) {
    return next
  }
  const target = idx + delta
  if (target < 0 || target >= next.nodes.length) {
    return next
  }
  const [item] = next.nodes.splice(idx, 1)
  next.nodes.splice(target, 0, item)
  return next
}

export function reorderNodes(
  doc: FormDocument,
  fromIndex: number,
  toIndex: number,
): FormDocument {
  const next = cloneFormDocument(doc)
  if (
    fromIndex === toIndex ||
    fromIndex < 0 ||
    toIndex < 0 ||
    fromIndex >= next.nodes.length ||
    toIndex >= next.nodes.length
  ) {
    return next
  }
  const [item] = next.nodes.splice(fromIndex, 1)
  next.nodes.splice(toIndex, 0, item)
  return next
}

export function findLayoutNode(doc: FormDocument, nodeId: string): FormLayoutNode | undefined {
  return doc.nodes.find((n) => n.id === nodeId)
}

export function findFieldById(doc: FormDocument, fieldId: string): FormFieldDef | undefined {
  for (const field of flattenFields(doc)) {
    if (field.id === fieldId) return field
  }
  return undefined
}

/** Find parent row/card id for a field, if nested. */
export function findFieldContainerId(doc: FormDocument, fieldId: string): string | undefined {
  for (const node of doc.nodes) {
    if (node.type === 'row' || node.type === 'card') {
      if (node.children.some((c) => c.id === fieldId)) {
        return node.id
      }
    }
  }
  return undefined
}

export function reorderChildrenInContainer(
  doc: FormDocument,
  containerId: string,
  fromIndex: number,
  toIndex: number,
): FormDocument {
  const next = cloneFormDocument(doc)
  const container = next.nodes.find((n) => n.id === containerId)
  if (!container || (container.type !== 'row' && container.type !== 'card')) {
    return next
  }
  const list = container.children
  if (
    fromIndex === toIndex ||
    fromIndex < 0 ||
    toIndex < 0 ||
    fromIndex >= list.length ||
    toIndex >= list.length
  ) {
    return next
  }
  const [item] = list.splice(fromIndex, 1)
  list.splice(toIndex, 0, item)
  return next
}

export function moveChildInContainer(
  doc: FormDocument,
  containerId: string,
  fieldId: string,
  delta: -1 | 1,
): FormDocument {
  const next = cloneFormDocument(doc)
  const container = next.nodes.find((n) => n.id === containerId)
  if (!container || (container.type !== 'row' && container.type !== 'card')) {
    return next
  }
  const idx = container.children.findIndex((c) => c.id === fieldId)
  if (idx < 0) return next
  const target = idx + delta
  if (target < 0 || target >= container.children.length) return next
  const [item] = container.children.splice(idx, 1)
  container.children.splice(target, 0, item)
  return next
}

export function nodeHasLockedField(node: FormLayoutNode): boolean {
  if (node.type === 'field') {
    return Boolean(node.field.platformLocked)
  }
  if (node.type === 'row' || node.type === 'card') {
    return node.children.some((c) => c.platformLocked)
  }
  return false
}

export type DemoFormLabels = {
  basic: string
  title: string
  details: string
  amount: string
  applyDate: string
  priority: string
  reason: string
  urgent: string
}

const DEFAULT_DEMO_LABELS: DemoFormLabels = {
  basic: 'Basic',
  title: 'Title',
  details: 'Details',
  amount: 'Amount',
  applyDate: 'Apply Date',
  priority: 'Priority',
  reason: 'Reason',
  urgent: 'Urgent',
}

export function buildDemoFormDocument(labels?: Partial<DemoFormLabels>): FormDocument {
  const L = { ...DEFAULT_DEMO_LABELS, ...labels }
  const doc = createEmptyFormDocument()
  return {
    ...doc,
    nodes: [
      {
        type: 'divider',
        id: nextFormNodeId('node'),
        title: L.basic,
      },
      {
        type: 'field',
        id: nextFormNodeId('node'),
        field: createFieldDef({
          fieldCode: 'title',
          fieldName: L.title,
          componentType: 'input',
          required: true,
          maxLength: 100,
          placeholder: L.title,
          span: 24,
        }),
      },
      {
        type: 'card',
        id: nextFormNodeId('node'),
        title: L.details,
        children: [
          createFieldDef({
            fieldCode: 'amount',
            fieldName: L.amount,
            componentType: 'number',
            fieldType: 'decimal',
            span: 8,
          }),
          createFieldDef({
            fieldCode: 'applyDate',
            fieldName: L.applyDate,
            componentType: 'date',
            span: 8,
          }),
          createFieldDef({
            fieldCode: 'priority',
            fieldName: L.priority,
            componentType: 'select',
            dictType: 'sys_common_status',
            span: 8,
          }),
        ],
      },
      {
        type: 'row',
        id: nextFormNodeId('node'),
        children: [
          createFieldDef({
            fieldCode: 'reason',
            fieldName: L.reason,
            componentType: 'textarea',
            maxLength: 500,
            span: 16,
          }),
          createFieldDef({
            fieldCode: 'urgent',
            fieldName: L.urgent,
            componentType: 'switch',
            span: 8,
          }),
        ],
      },
    ],
  }
}

export function createModelFromDocument(doc: FormDocument): Record<string, unknown> {
  const model: Record<string, unknown> = {}
  for (const field of flattenFields(doc)) {
    if (field.defaultValue !== undefined && field.defaultValue !== '') {
      model[field.fieldCode] = field.defaultValue
    } else {
      if (field.componentType === 'switch') {
        model[field.fieldCode] = false
      } else if (field.componentType === 'user' || field.componentType === 'dept') {
        model[field.fieldCode] = []
      } else {
        model[field.fieldCode] = undefined
      }
    }
  }
  return model
}
