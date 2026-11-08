import type { MenuRouter } from '@/types/system/menu'

export function flattenPageMenus(menus: MenuRouter[]): MenuRouter[] {
  const bucket: MenuRouter[] = []
  for (const menu of menus) {
    if (menu.menuType === 'C' && menu.path?.trim()) {
      bucket.push(menu)
    }
    if (menu.children?.length) {
      bucket.push(...flattenPageMenus(menu.children))
    }
  }
  return bucket
}

export function findMenuPath(menus: MenuRouter[], routeName: string): string | undefined {
  return flattenPageMenus(menus).find((menu) => menu.routeName === routeName)?.path
}
