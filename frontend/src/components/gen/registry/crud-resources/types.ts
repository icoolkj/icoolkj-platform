/**
 * Gen CRUD 资源条目 — 由 api/*.ts 尾部 export，registerAll 聚合登记。
 */
import type { GenCrudResource } from './registry'

export type GenCrudResourceEntry = {
  readonly GEN_RESOURCE_KEY: string
  readonly genCrudResource: GenCrudResource
}

/** @deprecated use GEN_RESOURCE_KEY on GenCrudResourceEntry */
export type CrudResourceModule = {
  readonly RESOURCE_KEY: string
  register: () => void
}
