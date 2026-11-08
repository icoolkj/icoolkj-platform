/**
 * 形态积木注册表 — layout / patternId → Vue 组件，GenPage 唯一路由来源。
 *
 * 用法：
 *   const layout = resolveLayoutFromView(fileView)
 *   const Block = resolvePatternBlock(layout)
 *   <component :is="Block" v-bind="buildPatternBlockProps(layout, props)" />
 */
import type { Component } from 'vue'
import GenPatternStandardCrud from './GenPatternStandardCrud.vue'
import GenPatternTreeCrud from './GenPatternTreeCrud.vue'
import GenPatternTreeList from './GenPatternTreeList.vue'
import GenPatternMasterDetail from './GenPatternMasterDetail.vue'
import type { GenPageLayoutKey, GenPageRoutedLayout } from '@/components/gen/registry/page-patterns'
import type { GenCrudApi } from '@/types/gen/crudApi'
import type { GenMasterDetailApi } from '@/types/gen/crudApi'
import type { GenViewSchema } from '@/types/tool/genView'
import type { GenPagePatternProps } from './types'

export interface GenPatternBlockMeta {
  /** 形态编号 P01–P12 */
  patternId: string
  /** 主 layout 键 */
  layout: GenPageRoutedLayout
  /** 积木 Vue 组件 */
  component: Component
  /** 别名 layout，路由到同一积木 */
  aliasLayouts?: GenPageLayoutKey[]
  /** 中文简述 */
  label: string
}

/** 已实现形态积木 — 见 GEN_PAGE_PATTERNS / README 形态表 */
export const GEN_PATTERN_BLOCKS: Record<GenPageRoutedLayout, GenPatternBlockMeta> = {
  'standard-crud': {
    patternId: 'P01',
    layout: 'standard-crud',
    component: GenPatternStandardCrud,
    aliasLayouts: ['custom-form'],
    label: '标准分页 CRUD / 只读 / 自定义表单',
  },
  'tree-crud': {
    patternId: 'P03',
    layout: 'tree-crud',
    component: GenPatternTreeCrud,
    label: '树形表格 CRUD',
  },
  'tree-list': {
    patternId: 'P05',
    layout: 'tree-list',
    component: GenPatternTreeList,
    label: '左树右表',
  },
  'master-detail': {
    patternId: 'P04',
    layout: 'master-detail',
    component: GenPatternMasterDetail,
    label: '主子双列表',
  },
}

const layoutAliasMap = new Map<GenPageLayoutKey, GenPageRoutedLayout>()
for (const meta of Object.values(GEN_PATTERN_BLOCKS)) {
  layoutAliasMap.set(meta.layout, meta.layout)
  for (const alias of meta.aliasLayouts ?? []) {
    layoutAliasMap.set(alias, meta.layout)
  }
}

/** 从 view.json 推断已路由 layout（含块推断） */
export function resolveLayoutFromView(fileView: GenViewSchema): GenPageRoutedLayout {
  const fromView = fileView.page?.layout
  if (fromView === 'master-detail' || fileView.masterDetail) return 'master-detail'
  if (fromView === 'tree-list' || fileView.treeList) return 'tree-list'
  if (fromView === 'tree-crud' || fileView.tree) return 'tree-crud'
  if (fromView && layoutAliasMap.has(fromView)) {
    return layoutAliasMap.get(fromView)!
  }
  return 'standard-crud'
}

/** 解析 layout 字符串（含别名）→ 已路由 layout */
export function normalizeRoutedLayout(layout: string | undefined): GenPageRoutedLayout {
  if (!layout) return 'standard-crud'
  if (layout in GEN_PATTERN_BLOCKS) return layout as GenPageRoutedLayout
  const aliased = layoutAliasMap.get(layout as GenPageLayoutKey)
  return aliased ?? 'standard-crud'
}

export function resolvePatternBlockMeta(layout: GenPageRoutedLayout): GenPatternBlockMeta {
  return GEN_PATTERN_BLOCKS[layout]
}

export function resolvePatternBlock(layout: GenPageRoutedLayout): Component {
  return GEN_PATTERN_BLOCKS[layout].component
}

export function resolvePatternBlockById(patternId: string): GenPatternBlockMeta | undefined {
  return Object.values(GEN_PATTERN_BLOCKS).find((m) => m.patternId === patternId)
}

/** 按 layout 组装形态积木 props（GenPage 透传逻辑） */
export function buildPatternBlockProps(
  layout: GenPageRoutedLayout,
  props: GenPagePatternProps,
): Record<string, unknown> {
  const common = {
    fileView: props.fileView,
    businessName: props.businessName,
    apiModule: props.apiModule,
    i18nFiles: props.i18nFiles,
    perm: props.perm,
    detailPerm: props.detailPerm,
    readOnly: props.readOnly,
    hideHeader: props.hideHeader,
    treeSources: props.treeSources,
    selectable: props.selectable,
    customForm: props.customForm,
    statusChange: props.statusChange,
    actionHandlers: props.actionHandlers,
    rowActionVisible: props.rowActionVisible,
  }
  switch (layout) {
    case 'tree-list':
      return { ...common, api: props.api as GenCrudApi }
    case 'master-detail':
      return { ...common, api: props.api as GenMasterDetailApi }
    case 'tree-crud':
      return {
        ...common,
        api: props.api as GenCrudApi,
        createDefaults: props.createDefaults,
        getAddChildDefaults: props.getAddChildDefaults,
        fixedQuery: props.fixedQuery,
        omitQueryFields: props.omitQueryFields,
        omitTableFields: props.omitTableFields,
        treeRowFilter: props.treeRowFilter,
        dropOrphanRows: props.dropOrphanRows,
        clearQueryOnFixedChange: props.clearQueryOnFixedChange,
        getFieldOptions: props.getFieldOptions,
      }
    default:
      return {
        ...common,
        api: props.api as GenCrudApi,
        fixedQuery: props.fixedQuery,
        omitQueryFields: props.omitQueryFields,
        clearQueryOnFixedChange: props.clearQueryOnFixedChange,
      }
  }
}

export function listPatternBlocks(): GenPatternBlockMeta[] {
  return Object.values(GEN_PATTERN_BLOCKS)
}
