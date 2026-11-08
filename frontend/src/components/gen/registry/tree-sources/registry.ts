/**
 * Gen 树数据源注册表：view.json extensions.treeSources 通过 apiKey 引用已登记的列表 API。
 */
export type GenTreeSourceApiContext = {
  pageSize: number
}

export type GenTreeSourceApiLoader = (
  ctx: GenTreeSourceApiContext,
) => Promise<Record<string, unknown>[]>

const treeSourceApis = new Map<string, GenTreeSourceApiLoader>()

export function registerTreeSourceApi(key: string, loader: GenTreeSourceApiLoader) {
  treeSourceApis.set(key, loader)
}

export function getTreeSourceApi(key: string): GenTreeSourceApiLoader | undefined {
  return treeSourceApis.get(key)
}

export function listRegisteredTreeSourceApiKeys(): string[] {
  return [...treeSourceApis.keys()]
}
