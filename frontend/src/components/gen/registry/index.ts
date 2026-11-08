/**
 * Gen 注册表层（Imperative 层）统一出口。
 *
 * extension-blocks/  view.extensions 的 handler / dialog 积木
 * tree-sources/      view.extensions.treeSources 的 apiKey
 * crud-resources/    聚合 api/*.ts 的 GEN_RESOURCE_KEY → 运行时解析
 */
export {
  getDialogWidget,
  getRowActionHandler,
  listRegisteredBlockIds,
  registerDialogWidget,
  registerRowActionHandler,
} from './extension-blocks/registry'
export type { GenRowActionContext, GenRowActionHandler } from './extension-blocks/registry'
export { registerPlatformGenBlocks } from './extension-blocks/registerPlatform'
export {
  registerAllDomainGenBlocks,
  collectDomainTreeSourceApiKeys,
} from './plugins/registerAll'

export {
  getFieldWidget,
  hasFieldWidget,
  listRegisteredFieldWidgetIds,
  registerFieldWidget,
} from './field-widgets/registry'
export { registerCoreFieldWidgets } from './field-widgets/registerCore'
export type { GenFieldWidgetContext, GenFieldWidgetEmits } from './field-widgets/types'

export {
  getTreeSourceApi,
  listRegisteredTreeSourceApiKeys,
  registerTreeSourceApi,
} from './tree-sources/registry'
export type { GenTreeSourceApiContext, GenTreeSourceApiLoader } from './tree-sources/registry'
export { registerPlatformTreeSources, PLATFORM_TREE_SOURCE_API_KEYS } from './tree-sources/registerPlatform'

export {
  CRUD_RESOURCE_ENTRIES,
  GEN_CRUD_RESOURCE_ENTRIES,
  PLATFORM_CRUD_RESOURCE_KEYS,
  discoverCrudResourceKeys,
  registerPlatformCrudResources,
} from './crud-resources/registerAll'
export {
  defineGenCrudResource,
  defineMasterDetailGenCrudResource,
  getCrudResource,
  listRegisteredCrudResourceKeys,
  registerCrudResource,
  resolveGenCrudResource,
  resolveGenPermFromMeta,
  resolveResourceKey,
  standardCrudApi,
  wrapExportFileAsync,
  wrapImportFileAsync,
} from './crud-resources'
export type { GenCrudResource, GenCrudResourceEntry } from './crud-resources'
export {
  registerGenPlatform,
  resetGenPlatformRegistrationForTests,
} from './platform'

export {
  GEN_BUILDING_BLOCKS,
  GEN_PAGE_PATTERNS,
  GEN_PAGE_ROUTED_LAYOUTS,
  GEN_RESOURCE_DEF_BY_KIND,
  getImplementedLayouts,
  getPagePattern,
  getPagePatternsByLayout,
  isRoutedLayout,
  warnUnroutedLayout,
} from './page-patterns'
export type {
  GenBuildingBlockCategory,
  GenPageLayoutKey,
  GenPagePattern,
  GenPagePatternStatus,
  GenPageRoutedLayout,
  GenResourceDefKind,
} from './page-patterns'
