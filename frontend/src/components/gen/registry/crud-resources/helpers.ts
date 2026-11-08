import type { GenCrudPerm } from '@/types/gen/crudApi'
import { registerCrudResource } from './registry'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { StandardCrudFns } from '@/lib/gen-crud/helpers'

export {
  defineGenCrudResource,
  defineMasterDetailGenCrudResource,
  defineReadOnlyGenCrudResource,
  READONLY_CRUD_STUBS,
  standardCrudApi,
  wrapExportFileAsync,
  wrapImportFileAsync,
} from '@/lib/gen-crud'
export function registerStandardCrudResource(
  key: string,
  perm: GenCrudPerm,
  api: StandardCrudFns & {
    get: NonNullable<import('@/types/gen/crudApi').GenCrudApi['get']>
    create: NonNullable<import('@/types/gen/crudApi').GenCrudApi['create']>
    update: NonNullable<import('@/types/gen/crudApi').GenCrudApi['update']>
    delete: NonNullable<import('@/types/gen/crudApi').GenCrudApi['delete']>
  },
) {
  registerCrudResource(key, defineGenCrudResource(perm, api))
}

export type { GenCrudResource } from '@/types/gen/crudResource'
