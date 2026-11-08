/**
 * 形态积木公共类型 — 供 GenPage / GenRuntimePage / registry 引用。
 */
import type { DictOption } from '@/types/system/dict'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import type { GenCrudApi, GenCrudPerm, GenMasterDetailApi } from '@/types/gen/crudApi'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import type { GenViewRowAction, GenViewSchema } from '@/types/tool/genView'

export type { GenCrudApi, GenCrudPerm, GenMasterDetailApi }

/** GenPage / 形态积木统一 props（layout 路由前） */
export interface GenPagePatternProps {
  fileView: GenViewSchema
  businessName: string
  apiModule: string
  i18nFiles: Record<string, GenI18nMessages>
  perm: GenCrudPerm
  /** P04 明细面板权限；缺省与 {@link perm} 相同 */
  detailPerm?: GenCrudPerm
  api: GenCrudApi | GenMasterDetailApi
  readOnly?: boolean
  hideHeader?: boolean
  treeSources?: Record<string, Record<string, unknown>[]>
  selectable?: boolean
  customForm?: boolean
  statusChange?: (row: Record<string, unknown>, status: string) => Promise<void>
  actionHandlers?: Record<string, GenRowActionHandler>
  rowActionVisible?: (
    action: GenViewRowAction,
    row: Record<string, unknown>,
  ) => boolean | undefined
  createDefaults?: Record<string, unknown>
  getAddChildDefaults?: (parent: Record<string, unknown>) => Record<string, unknown>
  fixedQuery?: Record<string, unknown>
  omitQueryFields?: string[]
  omitTableFields?: string[]
  treeRowFilter?: (row: Record<string, unknown>) => boolean
  dropOrphanRows?: boolean
  clearQueryOnFixedChange?: boolean
  /** GenPatternTreeCrud：动态字段选项（如 menu.productCode） */
  getFieldOptions?: (field: string) => DictOption[] | undefined
}

/** bundle 注入字段 — 由 GenRuntimePage / 手写页组装 */
export type GenPageBundleProps = Pick<
  GenPagePatternProps,
  'fileView' | 'businessName' | 'apiModule' | 'i18nFiles' | 'perm' | 'api'
>

/**
 * GenRuntimePage 页内扩展 props（不含 bundle 注入）。
 * 须为显式 interface — Vue SFC 编译器无法解析 Omit/Pick 等工具类型的 extends。
 */
export interface GenRuntimePageExtensionProps {
  /** P04 明细面板权限；缺省与 {@link perm} 相同 */
  detailPerm?: GenCrudPerm
  readOnly?: boolean
  hideHeader?: boolean
  treeSources?: Record<string, Record<string, unknown>[]>
  selectable?: boolean
  customForm?: boolean
  statusChange?: (row: Record<string, unknown>, status: string) => Promise<void>
  actionHandlers?: Record<string, GenRowActionHandler>
  rowActionVisible?: (
    action: GenViewRowAction,
    row: Record<string, unknown>,
  ) => boolean | undefined
  createDefaults?: Record<string, unknown>
  getAddChildDefaults?: (parent: Record<string, unknown>) => Record<string, unknown>
  fixedQuery?: Record<string, unknown>
  omitQueryFields?: string[]
  omitTableFields?: string[]
  treeRowFilter?: (row: Record<string, unknown>) => boolean
  dropOrphanRows?: boolean
  clearQueryOnFixedChange?: boolean
  /** GenPatternTreeCrud：动态字段选项（如 menu.productCode） */
  getFieldOptions?: (field: string) => DictOption[] | undefined
}

/** Gen 框架统一入口 props */
export interface GenRuntimePageProps extends GenRuntimePageExtensionProps {
  /** Git 目录名 = i18n/gen/{pageKey}/ */
  pageKey: string
  /** 覆盖注册表 api（分类 Tab 过滤、任务名 i18n 等） */
  apiOverride?: Partial<GenCrudApi> | Partial<GenMasterDetailApi>
  /** 运行时微调 view.json */
  viewOverride?: (view: GenViewSchema) => GenViewSchema
}

/** GenPage / GenRuntimePage 统一事件 */
export type GenPageEmits = {
  'selection-change': [rows: Record<string, unknown>[]]
  'open-form': [row?: Record<string, unknown>]
}

/** 形态积木 → GenPage → GenRuntimePage 统一 expose（方法恒存在，内部按形态 no-op） */
export interface GenPageExpose {
  openForm: (row?: Record<string, unknown>) => void
  openAddChild: (parent: Record<string, unknown>) => void
  loadPage: () => Promise<void> | void
  loadMasterPage: () => Promise<void> | void
  loadDetailPage: () => Promise<void> | void
  rebuildTree: () => void
  clearSelection: () => void
  form?: Record<string, unknown>
  queryParams?: { pageNo: number }
  selectedRows?: unknown[]
}

/** 标准 CRUD / custom-form 积木 props */
export type GenPatternStandardCrudProps = Pick<
  GenPagePatternProps,
  | 'fileView'
  | 'businessName'
  | 'apiModule'
  | 'i18nFiles'
  | 'perm'
  | 'readOnly'
  | 'hideHeader'
  | 'treeSources'
  | 'selectable'
  | 'customForm'
  | 'statusChange'
  | 'actionHandlers'
  | 'rowActionVisible'
  | 'fixedQuery'
  | 'omitQueryFields'
  | 'clearQueryOnFixedChange'
> & { api: GenCrudApi }

/** 树形表格 CRUD 积木 props */
export type GenPatternTreeCrudProps = GenPatternStandardCrudProps &
  Pick<
    GenPagePatternProps,
    | 'createDefaults'
    | 'getAddChildDefaults'
    | 'omitTableFields'
    | 'treeRowFilter'
    | 'dropOrphanRows'
    | 'getFieldOptions'
  >

/** 左树右表积木 props */
export type GenPatternTreeListProps = Pick<
  GenPagePatternProps,
  | 'fileView'
  | 'businessName'
  | 'apiModule'
  | 'i18nFiles'
  | 'perm'
  | 'readOnly'
  | 'treeSources'
  | 'selectable'
  | 'customForm'
  | 'statusChange'
  | 'actionHandlers'
  | 'rowActionVisible'
> & { api: GenCrudApi }

/** 主子双列表积木 props */
export type GenPatternMasterDetailProps = Pick<
  GenPagePatternProps,
  'fileView' | 'businessName' | 'apiModule' | 'i18nFiles' | 'perm' | 'detailPerm' | 'readOnly'
> & { api: GenMasterDetailApi }
