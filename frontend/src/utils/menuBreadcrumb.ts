import type { MenuRouter } from '@/types/system/menu'

export function resolveMenuBreadcrumb(menus: MenuRouter[], path: string) {
  function walk(
    items: MenuRouter[],
    ancestors: MenuRouter[],
  ): { parent?: MenuRouter; current?: MenuRouter } | null {
    for (const item of items) {
      if (
        item.menuType === 'C' &&
        item.path &&
        (path === item.path || path.startsWith(`${item.path}/`))
      ) {
        return {
          parent: ancestors.length ? ancestors[ancestors.length - 1] : undefined,
          current: item,
        }
      }
      if (item.children?.length) {
        const found = walk(item.children, [...ancestors, item])
        if (found) {
          return found
        }
      }
    }
    return null
  }
  return walk(menus, []) ?? {}
}
