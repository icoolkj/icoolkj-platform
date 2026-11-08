/**
 * CRUD 资源注册表内核（Imperative 层）。
 * view.json 通过 meta.resourceKey 引用；具体 API/perm 在各 modules/*.ts 登记。
 */
import type { GenCrudPerm } from '@/types/gen/crudApi'
import type { GenCrudResource } from '@/types/gen/crudResource'
import type { GenViewMeta } from '@/types/tool/genView'

export type { GenCrudResource }

const crudResources = new Map<string, GenCrudResource>()

export function registerCrudResource(key: string, resource: GenCrudResource) {
  crudResources.set(key, resource)
}

export function getCrudResource(key: string): GenCrudResource | undefined {
  return crudResources.get(key)
}

export function listRegisteredCrudResourceKeys(): string[] {
  return [...crudResources.keys()].sort()
}

/** 默认 `{apiModule}.{businessName}`；非标场景在 view.meta.resourceKey 覆盖 */
export function resolveResourceKey(meta: GenViewMeta): string {
  return meta.resourceKey ?? `${meta.apiModule}.${meta.businessName}`
}

export function resolveGenCrudResource(meta: GenViewMeta): GenCrudResource {
  const key = resolveResourceKey(meta)
  const resource = getCrudResource(key)
  if (!resource) {
    throw new Error(
      `[GenRuntime] unregistered resourceKey "${key}". Add GEN_RESOURCE_KEY + genCrudResource to api/{module}/${key.split('.').pop()}.ts`,
    )
  }
  return resource
}

/** 兜底：由 permissionPrefix 推导 perm（registry 优先） */
export function resolveGenPermFromMeta(meta: GenViewMeta): GenCrudPerm {
  const prefix = meta.permissionPrefix
  if (!prefix) return {}
  return {
    add: `${prefix}:add`,
    update: `${prefix}:update`,
    delete: `${prefix}:delete`,
    export: `${prefix}:export`,
    import: `${prefix}:import`,
  }
}
