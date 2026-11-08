/**
 * Cross-host form document (PC designer → runtime → future App).
 * Presentation SoT for L2; field rows remain data-contract SoT when embedded in OA.
 *
 * Two shapes (V1.3.1 / presentationVersion 2):
 * - FormDocument: in-memory design/runtime (full FormFieldDef)
 * - FormLayoutPersistence: stored layout_json (fieldCode + span + placeholder only)
 */
export type FormPreviewDevice = 'pc' | 'tablet' | 'mobile'

export type FormFieldType =
  | 'string'
  | 'int'
  | 'decimal'
  | 'date'
  | 'datetime'
  | 'dateRange'
  | 'bool'
  | 'file'
  | 'richText'
  /** Organizational reference (user / dept pickers). */
  | 'ref'

export type FormComponentType =
  | 'input'
  | 'textarea'
  | 'richText'
  | 'select'
  | 'radio'
  | 'checkbox'
  | 'date'
  | 'datetime'
  | 'dateRange'
  | 'number'
  | 'switch'
  | 'file'
  | 'user'
  | 'dept'

export interface FormMeta {
  labelWidth: number
  labelPosition: 'left' | 'top'
  /** Element Plus form size (not CSS). */
  size?: 'default' | 'small' | 'large'
  /** Label text align when labelPosition=left. */
  labelAlign?: 'left' | 'right'
  hideRequiredAsterisk?: boolean
  /** Optional host hint; not required for v1 documents. */
  host?: {
    mode: 'sandbox' | 'oa'
    templateVersionId?: string
  }
}

/** Atomic field definition (maps toward OaFieldDefVO / GenFormField). */
export interface FormFieldDef {
  id: string
  fieldCode: string
  fieldName: string
  fieldType: FormFieldType
  componentType: FormComponentType
  required?: boolean
  dictType?: string
  maxLength?: number
  defaultValue?: string
  placeholder?: string
  /** Element Plus 24-grid span (PC authority). Layout SoT when layout_json exists. */
  span?: number
  /** Bound mirror from OA field row; layout cannot clear locked fields. */
  platformLocked?: boolean
  /** Runtime-only node permission; never persist into layout_json. */
  fieldPerm?: 'R' | 'W' | 'H'
}

export interface FormFieldNode {
  type: 'field'
  id: string
  field: FormFieldDef
}

/** One row with fields sharing an el-row (grid). */
export interface FormRowNode {
  type: 'row'
  id: string
  children: FormFieldDef[]
}

/** Presentation-only card container (no fieldCode). */
export interface FormCardNode {
  type: 'card'
  id: string
  title?: string
  children: FormFieldDef[]
}

/** Presentation-only divider. */
export interface FormDividerNode {
  type: 'divider'
  id: string
  title?: string
}

export type FormLayoutNode = FormFieldNode | FormRowNode | FormCardNode | FormDividerNode

/** In-memory design / runtime document. */
export interface FormDocument {
  schemaVersion: 1
  form: FormMeta
  nodes: FormLayoutNode[]
}

/** Presentation-only field ref stored in layout_json (no contract fields). */
export interface FormFieldPresentationRef {
  fieldCode: string
  /** 1–24 Element Plus span (required after presentationVersion 2). */
  span: number
  placeholder?: string
}

export interface FormPersistedFieldNode {
  type: 'field'
  id: string
  fieldCode: string
  span: number
  placeholder?: string
}

export interface FormPersistedRowNode {
  type: 'row'
  id: string
  children: FormFieldPresentationRef[]
}

export interface FormPersistedCardNode {
  type: 'card'
  id: string
  title?: string
  children: FormFieldPresentationRef[]
}

export interface FormPersistedDividerNode {
  type: 'divider'
  id: string
  title?: string
}

export type FormPersistedLayoutNode =
  | FormPersistedFieldNode
  | FormPersistedRowNode
  | FormPersistedCardNode
  | FormPersistedDividerNode

/**
 * Stored layout_json / layout_snapshot_json (presentationVersion 2).
 * schemaVersion = document structure; presentationVersion = grid contract.
 */
export interface FormLayoutPersistence {
  schemaVersion: 1
  presentationVersion: 2
  form: FormMeta
  nodes: FormPersistedLayoutNode[]
}

export const FORM_DOCUMENT_SCHEMA_VERSION = 1 as const
export const FORM_LAYOUT_PRESENTATION_VERSION = 2 as const

export const DEFAULT_FORM_META: FormMeta = {
  labelWidth: 100,
  labelPosition: 'left',
  size: 'default',
  labelAlign: 'right',
  hideRequiredAsterisk: false,
}

/** DnD payload between palette / pool / canvas. */
export type FormDesignerDragPayload =
  | { kind: 'palette'; key: string; containerId?: string }
  | { kind: 'pool'; fieldCode: string; containerId?: string }
  | { kind: 'reorder'; index: number }
  | { kind: 'childReorder'; containerId: string; fromIndex: number }

export const FORM_DESIGNER_DND_MIME = 'application/x-icoolkj-form-designer'

/** Runtime host resolvers (OA i18n etc.). */
export interface FormRuntimeHostContext {
  resolveLabel?: (field: FormFieldDef) => string
  resolvePlaceholder?: (field: FormFieldDef) => string
  resolveRequiredMessage?: (field: FormFieldDef) => string
  resolveMaxLengthMessage?: (field: FormFieldDef, max: number) => string
  /** OA file biz type; defaults applied by formFieldCore when component is file. */
  fileBizType?: string
  fileDownloadScope?: 'infra' | 'notice'
}
