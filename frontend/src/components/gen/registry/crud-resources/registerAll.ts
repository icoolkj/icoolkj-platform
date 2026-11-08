/**
 * 自动聚合 api 模块导出的 GEN_RESOURCE_KEY + genCrudResource（及 GEN_CRUD_RESOURCES 多键）。
 *
 * 标准约定（单文件）：
 *   api/{module}/{entity}.ts  → HTTP + PERM + genCrudResource（codegen 一体产出）
 *   i18n/gen/{pageKey}/       → view.json（Declarative）
 *   components/gen/           → Runtime 渲染
 */
import type { GenCrudResource } from './registry'
import type { GenCrudResourceEntry } from './types'
import { registerCrudResource } from './registry'

type ApiGenModule = {
  GEN_RESOURCE_KEY?: string
  genCrudResource?: GenCrudResource
  GEN_CRUD_RESOURCES?: Record<string, GenCrudResource>
}

/** 排除 *.test.ts：eager glob 会执行模块，api 下的测试文件 import vitest 会导致 admin 运行时崩溃 */
const apiModules = import.meta.glob<ApiGenModule>(
  ['@/api/**/*.ts', '!@/api/**/*.test.ts'],
  { eager: true },
)

function collectGenCrudResourceEntries(): GenCrudResourceEntry[] {
  const entries: GenCrudResourceEntry[] = []
  const seen = new Set<string>()

  for (const [path, mod] of Object.entries(apiModules)) {
    if (path.endsWith('.test.ts')) continue

    if (mod.GEN_RESOURCE_KEY && mod.genCrudResource) {
      if (!seen.has(mod.GEN_RESOURCE_KEY)) {
        seen.add(mod.GEN_RESOURCE_KEY)
        entries.push({
          GEN_RESOURCE_KEY: mod.GEN_RESOURCE_KEY,
          genCrudResource: mod.genCrudResource,
        })
      }
    }

    if (mod.GEN_CRUD_RESOURCES) {
      for (const [key, resource] of Object.entries(mod.GEN_CRUD_RESOURCES)) {
        if (!seen.has(key)) {
          seen.add(key)
          entries.push({ GEN_RESOURCE_KEY: key, genCrudResource: resource })
        }
      }
    }
  }

  return entries.sort((a, b) => a.GEN_RESOURCE_KEY.localeCompare(b.GEN_RESOURCE_KEY))
}

export const GEN_CRUD_RESOURCE_ENTRIES: readonly GenCrudResourceEntry[] = collectGenCrudResourceEntries()

/** @deprecated use GEN_CRUD_RESOURCE_ENTRIES */
export const CRUD_RESOURCE_ENTRIES = GEN_CRUD_RESOURCE_ENTRIES
/** @deprecated use GEN_CRUD_RESOURCE_ENTRIES */
export const CRUD_RESOURCE_MODULES = GEN_CRUD_RESOURCE_ENTRIES

export const PLATFORM_CRUD_RESOURCE_KEYS: readonly string[] = GEN_CRUD_RESOURCE_ENTRIES.map(
  (e) => e.GEN_RESOURCE_KEY,
)

let registered = false

export function registerPlatformCrudResources() {
  if (registered) return
  registered = true
  for (const { GEN_RESOURCE_KEY, genCrudResource } of GEN_CRUD_RESOURCE_ENTRIES) {
    registerCrudResource(GEN_RESOURCE_KEY, genCrudResource)
  }
}

export function discoverCrudResourceKeys(): string[] {
  return [...PLATFORM_CRUD_RESOURCE_KEYS].sort()
}

