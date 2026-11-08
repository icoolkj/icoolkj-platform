export {
  getCrudResource,
  listRegisteredCrudResourceKeys,
  registerCrudResource,
  resolveGenCrudResource,
  resolveGenPermFromMeta,
  resolveResourceKey,
} from './registry'
export type { GenCrudResource } from '@/types/gen/crudResource'
export {
  defineGenCrudResource,
  defineMasterDetailGenCrudResource,
  defineReadOnlyGenCrudResource,
  registerStandardCrudResource,
  standardCrudApi,
  wrapExportFileAsync,
  wrapImportFileAsync,
} from './helpers'
export type { GenCrudResourceEntry } from './types'
export {
  CRUD_RESOURCE_ENTRIES,
  CRUD_RESOURCE_MODULES,
  GEN_CRUD_RESOURCE_ENTRIES,
  PLATFORM_CRUD_RESOURCE_KEYS,
  discoverCrudResourceKeys,
  registerPlatformCrudResources,
} from './registerAll'
