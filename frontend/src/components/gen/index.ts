/**
 * Gen 组件库对外入口。
 */
export { default as GenRuntimePage } from './runtime/GenRuntimePage.vue'
export { default as GenPage } from './pages/GenPage.vue'

export {
  GenPatternStandardCrud,
  GenPatternTreeCrud,
  GenPatternTreeList,
  GenPatternMasterDetail,
  GEN_PATTERN_BLOCKS,
  buildPatternBlockProps,
  listPatternBlocks,
  normalizeRoutedLayout,
  resolveLayoutFromView,
  resolvePatternBlock,
  resolvePatternBlockById,
  resolvePatternBlockMeta,
} from './patterns'
export type {
  GenPageBundleProps,
  GenPageEmits,
  GenPageExpose,
  GenPagePatternProps,
  GenPatternBlockMeta,
  GenPatternMasterDetailProps,
  GenPatternStandardCrudProps,
  GenPatternTreeCrudProps,
  GenPatternTreeListProps,
  GenRuntimePageExtensionProps,
  GenRuntimePageProps,
} from './patterns'
export { createGenPageExpose } from './patterns'

export { default as GenListLayout } from './layout/GenListLayout.vue'
export { default as GenFormShell } from './layout/GenFormShell.vue'

export { default as GenFormField } from './fields/GenFormField.vue'
export { default as GenEntityForm } from './fields/GenEntityForm.vue'

export { default as GenDataTable } from './table/GenDataTable.vue'

export { default as GenExcelToolbarActions } from './excel/GenExcelToolbarActions.vue'
export { default as GenExcelImportDialog } from './excel/GenExcelImportDialog.vue'
export { default as GenExcelExportDialog } from './excel/GenExcelExportDialog.vue'

export { default as GenDetailDialog } from './dialogs/GenDetailDialog.vue'

export * from './registry'
export * from './panels'
