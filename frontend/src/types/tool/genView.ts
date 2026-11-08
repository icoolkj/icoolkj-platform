export interface GenViewMeta {
  businessName: string
  apiModule: string
  /**
   * CRUD 资源注册键，默认 `{apiModule}.{businessName}`。
   * 解析见 crudResourceRegistry / GenRuntimePage。
   */
  resourceKey?: string
  backendModule?: string
  permissionPrefix?: string
  className?: string
}

export interface GenViewQueryField {
  field: string
  component: 'input' | 'select' | 'date' | 'tree-select'
  queryType?: 'EQ' | 'LIKE'
  dictType?: string
  width?: number
  treeSource?: string
  treeLabelField?: string
  treeValueField?: string
}

export interface GenViewTableField {
  field: string
  component:
    | 'text'
    | 'dict-tag'
    | 'dict-tags'
    | 'icon'
    | 'image'
    | 'datetime'
    | 'switch'
    | 'audit-i18n'
    | 'tag'
  dictType?: string
  /** Used with component audit-i18n: modules | businessTypes */
  auditCategory?: 'modules' | 'businessTypes'
  imageField?: string
  downloadScope?: 'infra' | 'notice'
  activeValue?: string
  inactiveValue?: string
  minWidth?: number
  width?: number
  align?: 'left' | 'center' | 'right'
  showOverflowTooltip?: boolean
}

/** Single-field visibility rule (showWhen / showWhenAll item) */
export interface GenViewFieldCondition {
  field: string
  values: string[]
}

/** Preset workflow row-action visibility (maps to useWorkflowBizRowActions) */
export type GenViewWorkflowWhen =
  | 'canEdit'
  | 'canSubmit'
  | 'canWithdraw'
  | 'canDelete'
  | 'canClose'

/** view.json extensions.treeSources entry — resolved via gen/blocks/treeSourceRegistry */
export interface GenViewTreeSourceConfig {
  /** Registered api key, e.g. pageDept */
  apiKey: string
  parentField?: string
  rootValue?: string
  labelField: string
  valueField?: string
  /** gp() key for synthetic root node label; default treeRoot */
  rootLabelKey?: string
  pageSize?: number
  /** When false, return flat tree only (no synthetic root). Default true. */
  wrapRoot?: boolean
}

export interface GenViewFormField {
  field: string
  component:
    | 'input'
    | 'textarea'
    | 'select'
    | 'radio'
    | 'checkbox'
    | 'datetime'
    | 'date'
    | 'daterange'
    | 'input-number'
    | 'tree-select'
    | 'password'
    | 'icon-picker'
    | 'avatar-picker'
    | 'cover-picker'
    | 'rich-editor'
    | 'file'
    | 'user'
    | 'dept'
  insert?: boolean
  edit?: boolean
  insertOnly?: boolean
  disabledOnEdit?: boolean
  span?: number
  dictType?: string
  rows?: number
  maxLength?: number
  valueFormat?: string
  min?: number
  defaultValue?: unknown
  showWhen?: GenViewFieldCondition
  /** Key into GenCrudPage/GenTreeCrudPage treeSources for tree-select */
  treeSource?: string
  treeLabelField?: string
  treeValueField?: string
  /**
   * cover-picker / file：上传 bizType（必填，由业务 view.json 或调用方声明）。
   * GenFormField 不提供业务默认值。
   */
  bizType?: string
  /** cover-picker / file：下载作用域；省略时 GenFormField 用技术默认 infra */
  downloadScope?: 'infra' | 'notice'
}

export interface GenViewTreeActions {
  edit?: boolean
  delete?: boolean
  addChild?: boolean
  /** Skip add-child when row[field] is in values */
  addChildExclude?: { field: string; values: string[] }
  expandCollapse?: boolean
  width?: number
}

export interface GenViewTreeConfig {
  parentField: string
  rootValue?: string
  rowKey?: string
  labelField?: string
  sortField?: string
  pageSize?: number
  defaultExpandAll?: boolean
  actions?: GenViewTreeActions
}

export interface GenViewPanelSchema {
  /**
   * Optional Excel path segment override for this panel
   * (MasterDetail detail often differs from root meta.businessName).
   */
  businessName?: string
  /** Panel-local toolbar (MasterDetail); falls back to root extensions on master only */
  toolbarActions?: GenViewToolbarAction[]
  query?: {
    fields?: GenViewQueryField[]
  }
  table?: {
    fields?: GenViewTableField[]
    actions?: { edit?: boolean; delete?: boolean; width?: number }
  }
  form?: {
    mode?: 'dialog' | 'drawer' | 'page'
    dialogWidth?: number
    labelWidth?: number
    fields?: GenViewFormField[]
  }
}

/** Declared row action — builtin edit/delete/detail or registered handler/dialog widget */
export interface GenViewRowAction {
  id: string
  /** Framework-handled edit / delete / detail (view.detailDialog) */
  builtin?: 'edit' | 'delete' | 'detail'
  /** i18n page key via gp() */
  labelKey?: string
  /** Key into GenCrudPerm (update, delete, resetPassword, …) */
  permKey?: string
  type?: 'primary' | 'danger' | 'warning' | 'success'
  /** Registered handler id, e.g. user.resetPassword */
  handler?: string
  /** Registered dialog widget id, e.g. GenUserRoleDialog / GenNoticeDetailDialog */
  dialog?: string
  /** Row field bound to dialog (default: id) */
  dialogRowKey?: string
  /** Dialog component prop name for the id (default: id, or widget map) */
  dialogIdProp?: string
  /** Show this action even when page is readOnly (e.g. audit / detail) */
  allowOnReadOnly?: boolean
  /** Hide unless row[field] is in values */
  showWhen?: GenViewFieldCondition
  /** Hide unless all conditions match (AND) */
  showWhenAll?: GenViewFieldCondition[]
  /** Preset workflow visibility (approveStatus-driven) */
  workflowWhen?: GenViewWorkflowWhen
  /** Disable (still visible) when row[field] is in values */
  disabledWhen?: GenViewFieldCondition
}

/** Declared toolbar action — e.g. builtin Excel export / import */
export interface GenViewToolbarAction {
  id: string
  /** Framework-handled toolbar actions */
  builtin?: 'export' | 'import' | 'batchDelete'
  /** Require table row selection (batchDelete) */
  selectionRequired?: boolean
  /** Filter selectable rows / batch delete targets */
  selectionFilter?: 'workflowCanDelete' | 'none'
  /** i18n page key via gp(); fallback common.export / common.import */
  labelKey?: string
  /** Key into GenCrudPerm (export, import, …) */
  permKey?: string
  type?: 'primary' | 'danger' | 'warning' | 'success' | 'default'
  /** Optional fallback only; server X-Download-Filename / Content-Disposition wins */
  fileName?: string
  /**
   * Export API path override. Default: `/{apiModule}/{businessName}/export`
   * (same query filters as the current list).
   */
  exportPath?: string
  /** Import upload path; default `/{apiModule}/{businessName}/import` */
  importPath?: string
  /** Template download path used inside import dialog (not a toolbar builtin) */
  importTemplatePath?: string
  /** Show on readOnly pages (export usually true) */
  allowOnReadOnly?: boolean
}

/** view.json extensions — reference registered bricks; do not embed Vue */
export interface GenViewExtensions {
  /** Toolbar buttons (export, …) rendered by GenCrudPage */
  toolbarActions?: GenViewToolbarAction[]
  rowActions?: GenViewRowAction[]
  /** Dialog widget ids to mount (must be registered) */
  dialogs?: string[]
  /** Tree-select data sources keyed by treeSource field value */
  treeSources?: Record<string, GenViewTreeSourceConfig>
}

/** view.json detailDialog — 标准 GenDetailDialog 字段布局 */
export interface GenViewDetailField {
  field: string
  component?: 'text' | 'dict-tag' | 'datetime' | 'json' | 'async-task-error'
  dictType?: string
  span?: number
}

export interface GenViewDetailDialogConfig {
  dialogWidth?: number
  labelWidth?: number
  column?: number
  fields?: GenViewDetailField[]
}

export interface GenViewMasterDetailConfig {
  masterSpan?: number
  detailSpan?: number
  /** Detail rows filtered by this field (e.g. dictTypeId) */
  detailForeignKey: string
  master?: GenViewPanelSchema
  detail?: GenViewPanelSchema
}

export interface GenViewTreeListConfig {
  /** extensions.treeSources 的 key（侧栏树数据源） */
  treeSource: string
  /** 选中树节点后写入列表查询的字段（如 deptId） */
  filterField: string
  sidebarSpan?: number
  mainSpan?: number
  valueField?: string
  labelField?: string
  rootValue?: string
  /** 选中 root 节点时不带 filter（默认 true） */
  clearFilterOnRoot?: boolean
  /** 侧栏已提供筛选时，隐藏 query 中同名字段（默认 true） */
  omitFromQuery?: boolean
  defaultExpandAll?: boolean
}

export interface GenViewRegionOrgConfig {
  competentTable?: {
    fields?: GenViewTableField[]
    actions?: { edit?: boolean; delete?: boolean; width?: number }
  }
  competentForm?: {
    dialogWidth?: number
    labelWidth?: number
    fields?: GenViewFormField[]
  }
  internalForm?: {
    dialogWidth?: number
    labelWidth?: number
    fields?: GenViewFormField[]
  }
}

/** custom-form 页：按文件目录 code 拉槽位渲染上传（如站点品牌） */
export interface GenViewFileCatalogBind {
  /** infra_file_catalog.catalog_code */
  code: string
  /** 页内 tab 名（如 logo / qr） */
  tab: string
  /** i18n page key via gp() */
  tabLabelKey?: string
  /** 是否编辑 labels[profileCode] 说明文案 */
  enableLabels?: boolean
}

/**
 * 附件场景选型 / 联调页（view.json 驱动）。
 * 文案一律走同目录 zh-CN.json / en-US.json 的 page.*（gp）。
 */
export interface GenViewAttachmentScenario {
  id: string
  /** gp()：Tab 标题 */
  tabLabelKey: string
  /** gp()：适用场景说明 */
  whenKey: string
  /** gp()：可复制 import 片段 */
  snippetKey: string
  component:
    | 'AttachmentManager'
    | 'CatalogAttachmentManager'
    | 'CoverImagePicker'
    | 'OaFileUpload'
    | 'special'
  mode?: 'single' | 'multiple'
  variant?: 'image' | 'list'
  /** @/constants/file 中的常量名，如 FILE_BIZ_NOTICE_COVER */
  bizTypeConst?: string
  downloadScope?: 'infra' | 'notice'
  maxFiles?: number
  /** Catalog 场景：与 fileCatalogs[].tab 对齐，取该组目录编码 */
  catalogBindTab?: string
}

/** Locale-agnostic CRUD page layout — paired with zh-CN.json / en-US.json for labels. */
export interface GenViewSchema {
  meta: GenViewMeta
  page?: {
    layout?: 'standard-crud' | 'tree-crud' | 'master-detail' | 'tree-list' | 'region-org' | 'custom-form'
    /** Enable table multi-select column */
    selectable?: boolean
    /** Filter which rows can be selected (batch delete) */
    selectionFilter?: 'workflowCanDelete' | 'none'
  }
  query?: {
    maxVisible?: number
    collapsible?: boolean
    fields?: GenViewQueryField[]
  }
  table?: {
    fields?: GenViewTableField[]
    actions?: { edit?: boolean; delete?: boolean; width?: number }
  }
  readOnly?: boolean
  form?: {
    mode?: 'dialog' | 'drawer' | 'page'
    dialogWidth?: number
    labelWidth?: number
    closeOnClickModal?: boolean
    closeOnPressEscape?: boolean
    /** Merged into create form before field defaultValue */
    createDefaults?: Record<string, unknown>
    fields?: GenViewFormField[]
  }
  tree?: GenViewTreeConfig
  treeList?: GenViewTreeListConfig
  masterDetail?: GenViewMasterDetailConfig
  regionOrg?: GenViewRegionOrgConfig
  /** Named bricks: row actions / dialogs (see gen/blocks/registry) */
  extensions?: GenViewExtensions
  /**
   * 标准详情 Dialog 配置（GenDetailDialog）；复杂领域详情仍用 extensions.dialogs 注册插件。
   */
  detailDialog?: GenViewDetailDialogConfig
  /**
   * 配置驱动文件目录上传（custom-form）：按 code 拉槽位 → AttachmentManager / CatalogAttachmentManager。
   * GenCrudPage 忽略；业务页自行消费。停用槽位（status=1）不渲染。
   */
  fileCatalogs?: GenViewFileCatalogBind[]
  /**
   * 附件场景测试页：scenarios + fileCatalogs 由 view.json 驱动；文案在 gen i18n。
   */
  attachmentScenarios?: GenViewAttachmentScenario[]
  /** gp() keys：选型指引列表（按顺序） */
  attachmentPickGuideKeys?: string[]
  /** 特殊场景说明：{ nameKey, descKey } → gp() */
  attachmentSpecialCases?: { nameKey: string; descKey: string }[]
}

export interface GenViewConfigRow {
  id?: string
  tableId: string
  viewJson: string
  remark?: string
}
