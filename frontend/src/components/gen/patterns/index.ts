/**
 * 形态积木层 — 框架映射的直接调用入口。
 *
 * @example
 * import { GenPatternStandardCrud, resolvePatternBlock } from '@/components/gen/patterns'
 */
export { default as GenPatternStandardCrud } from './GenPatternStandardCrud.vue'
export { default as GenPatternTreeCrud } from './GenPatternTreeCrud.vue'
export { default as GenPatternTreeList } from './GenPatternTreeList.vue'
export { default as GenPatternMasterDetail } from './GenPatternMasterDetail.vue'

export {
  GEN_PATTERN_BLOCKS,
  buildPatternBlockProps,
  listPatternBlocks,
  normalizeRoutedLayout,
  resolveLayoutFromView,
  resolvePatternBlock,
  resolvePatternBlockById,
  resolvePatternBlockMeta,
} from './registry'
export type { GenPatternBlockMeta } from './registry'
export { createGenPageExpose } from './expose'
export type {
  GenCrudApi,
  GenCrudPerm,
  GenMasterDetailApi,
  GenPageBundleProps,
  GenPageEmits,
  GenPageExpose,
  GenPagePatternProps,
  GenPatternMasterDetailProps,
  GenPatternStandardCrudProps,
  GenPatternTreeCrudProps,
  GenPatternTreeListProps,
  GenRuntimePageExtensionProps,
  GenRuntimePageProps,
} from './types'
