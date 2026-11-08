import type { MenuRouter } from '@/types/system/menu'

/** Filter sidebar menu tree by display name (case-insensitive). */
export function filterMenuTree(menus: MenuRouter[], keyword: string): MenuRouter[] {
  const q = keyword.trim().toLowerCase()
  if (!q) return menus
  const out: MenuRouter[] = []
  for (const menu of menus) {
    const selfMatch = (menu.menuName ?? '').toLowerCase().includes(q)
    if (selfMatch) {
      out.push(menu)
      continue
    }
    if (menu.children?.length) {
      const kids = filterMenuTree(menu.children, keyword)
      if (kids.length) {
        out.push({ ...menu, children: kids })
      }
    }
  }
  return out
}

/** Collect submenu indexes (paths) for el-menu default-openeds while searching. */
export function collectOpenMenuIndexes(menus: MenuRouter[]): string[] {
  const indexes: string[] = []
  for (const menu of menus) {
    if (menu.menuType === 'M' && menu.children?.length) {
      indexes.push(menu.path || menu.id)
      indexes.push(...collectOpenMenuIndexes(menu.children))
    }
  }
  return indexes
}
