/**
 * Gen 页面形态注册表 — 与 registry/page-patterns.ts、README 形态表对应。
 *
 * 用途：
 * - 文档 / codegen / 校验脚本引用单一来源
 * - IDE 跳转金样例与编排器
 * - GenPage 开发期可对照 layout 是否已实现
 */
import type { GenCrudApi } from '@/types/gen/crudApi'

/** 形态实现状态 */
export type GenPagePatternStatus = 'implemented' | 'convention' | 'planned'

/** view.page.layout 或等价推断键 */
export type GenPageLayoutKey =
  | 'standard-crud'
  | 'tree-crud'
  | 'master-detail'
  | 'tree-list'
  | 'master-tree'
  | 'custom-form'
  | 'custom'
  | 'workbench'
  | 'card-catalog'
  | 'custom-profile'
  | 'region-org'

/** API 资源定义方式（与 api/*.ts 尾部 export 对应） */
export type GenResourceDefKind =
  | 'defineGenCrudResource'
  | 'defineReadOnlyGenCrudResource'
  | 'defineMasterDetailGenCrudResource'
  | 'none'

export interface GenPagePattern {
  /** 形态编号，文档与 codegen 引用 */
  id: string
  /** 中文名（产品 / 架构文档） */
  name: string
  /** 英文简述 */
  nameEn: string
  status: GenPagePatternStatus
  /** view.json page.layout；custom 类形态可为 null */
  layout: GenPageLayoutKey | null
  /** 形态积木组件名（patterns/ 下，GenPage 映射调用） */
  patternBlock: string
  /** api/*.ts 资源定义方式 */
  resourceDef: GenResourceDefKind
  /** 金样例 pageKey（views 目录） */
  goldenSamples: string[]
  /** 典型 GenCrudApi 能力 */
  apiSurface: (keyof GenCrudApi | 'master' | 'detail')[]
  /** 一句话场景 */
  whenToUse: string
  /** 程序员扩展档位 L0–L5 */
  complexityRange: `${number}${number}` | `${number}`
  /** view.json 关键块 */
  viewBlocks: string[]
  /** GenRuntimePage 常用 props（非完整列表） */
  commonProps?: string[]
  /** GenListLayout / GenPage 常用 slot */
  commonSlots?: string[]
}

/** 形态目录 — 按 id 排序即阅读顺序 */
export const GEN_PAGE_PATTERNS: readonly GenPagePattern[] = [
  {
    id: 'P01',
    name: '标准分页 CRUD',
    nameEn: 'Standard Paginated CRUD',
    status: 'implemented',
    layout: 'standard-crud',
    patternBlock: 'GenPatternStandardCrud',
    resourceDef: 'defineGenCrudResource',
    goldenSamples: ['post', 'role'],
    apiSurface: ['page', 'get', 'create', 'update', 'delete'],
    whenToUse: '扁平实体，搜索 + 分页表格 + 弹窗/抽屉表单增删改',
    complexityRange: '0',
    viewBlocks: ['query', 'table', 'form', 'toolbarActions'],
    commonProps: ['create-defaults'],
    commonSlots: ['table-top', 'toolbar-extra', 'row-actions'],
  },
  {
    id: 'P02',
    name: '只读审计列表',
    nameEn: 'Read-only Audit List',
    status: 'implemented',
    layout: 'standard-crud',
    patternBlock: 'GenPatternStandardCrud',
    resourceDef: 'defineReadOnlyGenCrudResource',
    goldenSamples: ['operationLog', 'apiAccessLog', 'aiInvokeLog'],
    apiSurface: ['page', 'exportFileAsync'],
    whenToUse: '日志 / 审计 / 监控类，仅查询与导出，无写操作',
    complexityRange: '0',
    viewBlocks: ['query', 'table', 'extensions.rowActions'],
    commonProps: ['read-only'],
    commonSlots: [],
  },
  {
    id: 'P03',
    name: '树形表格 CRUD',
    nameEn: 'Tree Table CRUD',
    status: 'implemented',
    layout: 'tree-crud',
    patternBlock: 'GenPatternTreeCrud',
    resourceDef: 'defineGenCrudResource',
    goldenSamples: ['dept', 'region'],
    apiSurface: ['page', 'get', 'create', 'update', 'delete'],
    whenToUse: '数据本身呈父子树，单表树表一体（展开/折叠、新增子节点）',
    complexityRange: '1',
    viewBlocks: ['tree', 'query', 'table', 'form'],
    commonProps: ['create-defaults', 'get-add-child-defaults', 'fixed-query', 'tree-row-filter'],
    commonSlots: ['table-top', 'toolbar-extra'],
  },
  {
    id: 'P04',
    name: '主子双列表',
    nameEn: 'Master-Detail List',
    status: 'implemented',
    layout: 'master-detail',
    patternBlock: 'GenPatternMasterDetail',
    resourceDef: 'defineMasterDetailGenCrudResource',
    goldenSamples: ['dict'],
    apiSurface: ['master', 'detail'],
    whenToUse: '主表选中行驱动从表分页列表（字典类型 ↔ 字典数据）',
    complexityRange: '0',
    viewBlocks: ['masterDetail.master', 'masterDetail.detail'],
    commonProps: [],
    commonSlots: [],
  },
  {
    id: 'P05',
    name: '左树右表',
    nameEn: 'Tree Sidebar + Paginated List',
    status: 'implemented',
    layout: 'tree-list',
    patternBlock: 'GenPatternTreeList',
    resourceDef: 'defineGenCrudResource',
    goldenSamples: ['user'],
    apiSurface: ['page', 'get', 'create', 'update', 'delete'],
    whenToUse: '左侧组织/分类树筛选右侧分页列表，filterField 注入 fixedQuery',
    complexityRange: '1',
    viewBlocks: ['treeList', 'extensions.treeSources', 'query', 'table', 'form'],
    commonProps: ['tree-sources'],
    commonSlots: ['table-top', 'toolbar-extra'],
  },
  {
    id: 'P06',
    name: '主子列表 + 从表树',
    nameEn: 'Master List + Detail Tree',
    status: 'planned',
    layout: 'master-tree',
    patternBlock: 'GenPatternMasterDetail',
    resourceDef: 'defineMasterDetailGenCrudResource',
    goldenSamples: [],
    apiSurface: ['master', 'detail'],
    whenToUse: '主表列表 + 从表树形 CRUD（P4 的 detail 区换 tree-crud）',
    complexityRange: '2',
    viewBlocks: ['masterDetail.master', 'masterDetail.detail', 'masterDetail.detailLayout'],
    commonProps: [],
    commonSlots: [],
  },
  {
    id: 'P07',
    name: '行操作扩展（详情/分配等）',
    nameEn: 'Row Action Extensions',
    status: 'implemented',
    layout: null,
    patternBlock: 'GenPattern* + GenRuntimePage',
    resourceDef: 'defineGenCrudResource',
    goldenSamples: ['notice', 'user', 'databaseBackup'],
    apiSurface: ['page', 'get'],
    whenToUse: '详情 Dialog、角色分配、自定义下载/删除等非标准行按钮',
    complexityRange: '4',
    viewBlocks: ['extensions.rowActions', 'extensions.dialogs'],
    commonProps: ['action-handlers', 'row-action-visible'],
    commonSlots: [],
  },
  {
    id: 'P08',
    name: '自定义表单页',
    nameEn: 'Custom Form Page',
    status: 'convention',
    layout: 'custom-form',
    patternBlock: 'GenPatternStandardCrud',
    resourceDef: 'defineGenCrudResource',
    goldenSamples: ['aiConfig'],
    apiSurface: ['get', 'update'],
    whenToUse: '单页配置表单，无列表；customForm + GenFormShell 或手写 GenEntityForm',
    complexityRange: '3',
    viewBlocks: ['form'],
    commonProps: ['custom-form'],
    commonSlots: [],
  },
  {
    id: 'P09',
    name: '复合壳页',
    nameEn: 'Composite Shell',
    status: 'convention',
    layout: 'custom',
    patternBlock: 'GenListLayout + GenRuntimePage',
    resourceDef: 'defineGenCrudResource',
    goldenSamples: ['databaseBackup', 'menu', 'attachmentDemo'],
    apiSurface: ['page'],
    whenToUse: 'Tab / 多面板 / 嵌套 Gen 页；外层 GenListLayout，内层 hide-header',
    complexityRange: '5',
    viewBlocks: [],
    commonProps: ['hide-header', 'action-handlers'],
    commonSlots: ['table-top', 'toolbar-extra', 'default'],
  },
  {
    id: 'P10',
    name: '自由布局壳',
    nameEn: 'Plain / Workbench Shell',
    status: 'convention',
    layout: 'workbench',
    patternBlock: 'GenListLayout',
    resourceDef: 'none',
    goldenSamples: [],
    apiSurface: [],
    whenToUse: '工作台、个人中心、全屏对话；plain=true，无 view.json CRUD',
    complexityRange: '5',
    viewBlocks: [],
    commonProps: ['plain', 'hide-header'],
    commonSlots: ['default', 'header-extra'],
  },
  {
    id: 'P11',
    name: '卡片目录',
    nameEn: 'Card Catalog',
    status: 'planned',
    layout: 'card-catalog',
    patternBlock: 'GenListLayout',
    resourceDef: 'none',
    goldenSamples: [],
    apiSurface: [],
    whenToUse: '图标卡片入口（应用中心类），schema 预留，运行时手写 GenListLayout',
    complexityRange: '5',
    viewBlocks: [],
    commonProps: ['plain'],
    commonSlots: ['default'],
  },
  {
    id: 'P12',
    name: '区划组织专用',
    nameEn: 'Region / Org Domain',
    status: 'planned',
    layout: 'region-org',
    patternBlock: 'GenPatternTreeCrud | GenPatternTreeList',
    resourceDef: 'defineGenCrudResource',
    goldenSamples: ['region', 'tenant'],
    apiSurface: ['page', 'get', 'create', 'update', 'delete'],
    whenToUse: '区划/租户领域；收敛为 tree-crud 或 tree-list + 领域 extension',
    complexityRange: '2',
    viewBlocks: ['tree', 'treeList', 'extensions'],
    commonProps: ['fixed-query', 'tree-row-filter'],
    commonSlots: [],
  },
] as const

const byId = new Map(GEN_PAGE_PATTERNS.map((p) => [p.id, p]))
const byLayout = new Map<GenPageLayoutKey, GenPagePattern[]>()

for (const p of GEN_PAGE_PATTERNS) {
  if (!p.layout) continue
  const list = byLayout.get(p.layout) ?? []
  list.push(p)
  byLayout.set(p.layout, list)
}

export function getPagePattern(id: string): GenPagePattern | undefined {
  return byId.get(id)
}

export function getPagePatternsByLayout(layout: GenPageLayoutKey): GenPagePattern[] {
  return byLayout.get(layout) ?? []
}

export function getImplementedLayouts(): GenPageLayoutKey[] {
  return GEN_PAGE_PATTERNS.filter((p) => p.status === 'implemented' && p.layout)
    .map((p) => p.layout!)
    .filter((v, i, a) => a.indexOf(v) === i)
}

/** GenPage 当前已路由的 layout（与 GenPage.vue switch 同步） */
export const GEN_PAGE_ROUTED_LAYOUTS = [
  'standard-crud',
  'tree-crud',
  'tree-list',
  'master-detail',
] as const satisfies readonly GenPageLayoutKey[]

export type GenPageRoutedLayout = (typeof GEN_PAGE_ROUTED_LAYOUTS)[number]

export function isRoutedLayout(layout: string): layout is GenPageRoutedLayout {
  return (GEN_PAGE_ROUTED_LAYOUTS as readonly string[]).includes(layout)
}

/** 开发期：未知 layout 回退 standard-crud 时提示 */
const LAYOUT_ALIASES_TO_STANDARD_CRUD = ['custom-form'] as const

export function warnUnroutedLayout(layout: string | undefined, pageKey?: string): void {
  if (!layout || isRoutedLayout(layout) || layout === 'standard-crud') return
  if ((LAYOUT_ALIASES_TO_STANDARD_CRUD as readonly string[]).includes(layout)) return
  if (import.meta.env.DEV) {
    console.warn(
      `[GenPage] layout "${layout}"${pageKey ? ` (pageKey=${pageKey})` : ''} is not routed; falling back to GenPatternStandardCrud. See registry/page-patterns.ts.`,
    )
  }
}

/** 形态 → 推荐 api helper（文档/codegen） */
export const GEN_RESOURCE_DEF_BY_KIND: Record<
  GenResourceDefKind,
  { helper: string; exportKeys: string[] }
> = {
  defineGenCrudResource: {
    helper: 'defineGenCrudResource',
    exportKeys: ['GEN_RESOURCE_KEY', 'genCrudResource'],
  },
  defineReadOnlyGenCrudResource: {
    helper: 'defineReadOnlyGenCrudResource',
    exportKeys: ['GEN_RESOURCE_KEY', 'genCrudResource'],
  },
  defineMasterDetailGenCrudResource: {
    helper: 'defineMasterDetailGenCrudResource',
    exportKeys: ['GEN_RESOURCE_KEY', 'genCrudResource'],
  },
  none: { helper: '', exportKeys: [] },
}

/** 积木目录（与目录结构对应，供文档引用） */
export const GEN_BUILDING_BLOCKS = {
  runtime: ['GenRuntimePage'],
  routing: ['GenPage'],
  patterns: [
    'GenPatternStandardCrud',
    'GenPatternTreeCrud',
    'GenPatternTreeList',
    'GenPatternMasterDetail',
  ],
  layout: ['GenListLayout', 'GenFormShell'],
  panels: ['GenQueryPanel', 'GenTablePanel', 'GenToolbarPanel', 'GenFormPanel', 'GenRowActions'],
  fields: ['GenFormField', 'GenEntityForm'],
  table: ['GenDataTable'],
  excel: ['GenExcelToolbarActions', 'GenExcelImportDialog', 'GenExcelExportDialog'],
  dialogs: ['domains/**/dialogs/*', 'GenMenuAuthDialog', 'GenUserRoleDialog'],
  registry: [
    'patterns/registry',
    'crud-resources/registerAll',
    'extension-blocks/registry',
    'tree-sources/registry',
    'page-patterns',
  ],
} as const

export type GenBuildingBlockCategory = keyof typeof GEN_BUILDING_BLOCKS
