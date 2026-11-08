/**
 * sys_config / 租户策略共用分类（按 config_key 前缀）。
 * 与后端 ConfigPageQuery.configKeyPrefix / configKeyExcludePrefixes 约定一致。
 */

export type ConfigCategoryId =
  | 'all'
  | 'login'
  | 'password'
  | 'mfa'
  | 'ip'
  | 'i18n'
  | 'ai'
  | 'backup'
  | 'storage'
  | 'avatar'
  | 'workflow'
  | 'brand'
  | 'other'

export interface ConfigCategoryDef {
  id: ConfigCategoryId
  /** 空表示「全部」；null 表示「其他」（排除 knownPrefixes） */
  prefixes: string[] | null
}

/** 有序分类定义（Tab 展示顺序） */
export const CONFIG_CATEGORIES: ConfigCategoryDef[] = [
  { id: 'all', prefixes: [] },
  { id: 'login', prefixes: ['security.login.'] },
  { id: 'password', prefixes: ['security.password.'] },
  { id: 'mfa', prefixes: ['security.mfa.'] },
  { id: 'ip', prefixes: ['security.ip.'] },
  { id: 'i18n', prefixes: ['system.i18n.'] },
  { id: 'ai', prefixes: ['ai.'] },
  { id: 'backup', prefixes: ['infra.backup.'] },
  { id: 'storage', prefixes: ['infra.storage.'] },
  { id: 'avatar', prefixes: ['infra.avatar.'] },
  { id: 'workflow', prefixes: ['workflow.'] },
  { id: 'brand', prefixes: ['brand.'] },
  { id: 'other', prefixes: null },
]

/** 已知业务前缀（不含 all/other） */
export function knownConfigKeyPrefixes(): string[] {
  return CONFIG_CATEGORIES.filter((c) => c.prefixes && c.prefixes.length > 0).flatMap(
    (c) => c.prefixes as string[],
  )
}

export function resolveConfigCategory(configKey: string): ConfigCategoryId {
  const key = (configKey || '').trim()
  for (const cat of CONFIG_CATEGORIES) {
    if (!cat.prefixes || cat.prefixes.length === 0) continue
    if (cat.prefixes.some((p) => key.startsWith(p))) {
      return cat.id
    }
  }
  return 'other'
}

export function matchConfigCategory(configKey: string, categoryId: ConfigCategoryId): boolean {
  if (categoryId === 'all') return true
  return resolveConfigCategory(configKey) === categoryId
}

/** 供列表接口过滤：返回 prefix 或「排除前缀」列表 */
export function categoryQueryFilter(categoryId: ConfigCategoryId): {
  configKeyPrefix?: string
  configKeyExcludePrefixes?: string
} {
  if (categoryId === 'all') return {}
  if (categoryId === 'other') {
    return { configKeyExcludePrefixes: knownConfigKeyPrefixes().join(',') }
  }
  const cat = CONFIG_CATEGORIES.find((c) => c.id === categoryId)
  const prefix = cat?.prefixes?.[0]
  return prefix ? { configKeyPrefix: prefix } : {}
}

/** 仅展示有数据的分类（租户策略用） */
export function categoriesPresentIn(keys: string[]): ConfigCategoryDef[] {
  const present = new Set(keys.map(resolveConfigCategory))
  return CONFIG_CATEGORIES.filter((c) => {
    if (c.id === 'all') return true
    return present.has(c.id)
  })
}
