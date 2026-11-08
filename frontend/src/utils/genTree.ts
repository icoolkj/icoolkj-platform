import { TREE_ROOT_PARENT_ID } from '@/constants/flags'

export type BuildGenTreeOptions = {
  /** When true, rows whose parent is missing from data are omitted instead of promoted to root. */
  dropOrphans?: boolean
}

/** Build flat rows with parentId into a tree (mutable children arrays). */
export function buildGenTree<T extends Record<string, unknown>>(
  data: T[],
  parentField: string,
  rootValue: string | number | null = TREE_ROOT_PARENT_ID,
  options?: BuildGenTreeOptions,
): T[] {
  const dropOrphans = options?.dropOrphans ?? false
  const map = new Map<string, T & { children?: T[] }>()
  const roots: (T & { children?: T[] })[] = []

  for (const item of data) {
    const id = String(item.id ?? '')
    map.set(id, { ...item, children: [] })
  }

  map.forEach((node) => {
    const pid = node[parentField]
    const parentKey = pid == null || pid === '' ? String(rootValue) : String(pid)
    if (
      parentKey === String(rootValue) ||
      (parentKey === TREE_ROOT_PARENT_ID && rootValue === TREE_ROOT_PARENT_ID)
    ) {
      if (pid == null || pid === '' || String(pid) === String(rootValue)) {
        roots.push(node)
        return
      }
    }
    if (pid == null || pid === '' || String(pid) === String(rootValue)) {
      roots.push(node)
      return
    }
    const parent = map.get(String(pid))
    if (parent) {
      parent.children!.push(node as T)
    } else if (!dropOrphans) {
      roots.push(node)
    }
  })

  return roots as T[]
}

/** Sort tree siblings by numeric sort field (default sortNo), then id. */
export function sortGenTree<T extends Record<string, unknown>>(
  nodes: T[],
  sortField = 'sortNo',
): void {
  nodes.sort((a, b) => {
    const av = Number(a[sortField] ?? 0)
    const bv = Number(b[sortField] ?? 0)
    if (av !== bv) return av - bv
    return String(a.id ?? '').localeCompare(String(b.id ?? ''))
  })
  for (const node of nodes) {
    const children = node.children as T[] | undefined
    if (children?.length) {
      sortGenTree(children, sortField)
    }
  }
}

export function walkGenTree<T extends { children?: T[] }>(rows: T[], fn: (row: T) => void): void {
  for (const row of rows) {
    fn(row)
    if (row.children?.length) {
      walkGenTree(row.children, fn)
    }
  }
}

export function findGenTreeNode<T extends { id?: string; children?: T[] }>(
  rows: T[],
  id: string,
): T | undefined {
  for (const row of rows) {
    if (row.id === id) return row
    if (row.children?.length) {
      const found = findGenTreeNode(row.children, id)
      if (found) return found
    }
  }
  return undefined
}
