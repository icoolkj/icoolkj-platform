import type { Router, RouteRecordRaw } from 'vue-router'
import type { MenuRouter } from '@/types/system/menu'
import { isStatusDisabled } from '@/constants/flags'
import { useMenuStore } from '@/stores/menu'

const viewModules = import.meta.glob('@/views/**/*.vue')

function menuComponentPath(menu: MenuRouter): string | undefined {
  const path = menu.component || menu.componentPath
  return path?.trim() || undefined
}

export function resolveViewComponent(componentPath?: string) {
  if (!componentPath?.trim()) {
    return undefined
  }
  let normalized = componentPath.trim().replace(/\\/g, '/').replace(/^\//, '')
  normalized = normalized.replace(/^@\/views\//, '').replace(/^views\//, '')
  if (normalized.endsWith('.vue')) {
    normalized = normalized.slice(0, -4)
  }
  if (normalized.endsWith('/index')) {
    normalized = normalized.slice(0, -'/index'.length)
  }
  const suffix = `${normalized}/index.vue`
  const entry = Object.entries(viewModules).find(([key]) => {
    const k = key.replace(/\\/g, '/')
    return k.endsWith(suffix) || k.endsWith(`/${suffix}`)
  })
  return entry?.[1]
}

function normalizeRoutePath(path: string) {
  return path.trim().replace(/^\//, '').replace(/\/+$/, '')
}

function flattenPageMenus(menus: MenuRouter[], bucket: MenuRouter[] = []) {
  for (const menu of menus) {
    if (menu.menuType === 'C' && menuComponentPath(menu)) {
      bucket.push(menu)
    }
    if (menu.children?.length) {
      flattenPageMenus(menu.children, bucket)
    }
  }
  return bucket
}

export async function setupDynamicRoutes(router: Router, options?: { force?: boolean }) {
  const menuStore = useMenuStore()
  const menus = await menuStore.loadAllRouters(options?.force ?? false)
  const pages = flattenPageMenus(menus)

  for (const menu of pages) {
    if (menu.routeName && router.hasRoute(menu.routeName)) {
      router.removeRoute(menu.routeName)
    }
  }

  const missed: string[] = []
  for (const menu of pages) {
    const componentPath = menuComponentPath(menu)
    const component = resolveViewComponent(componentPath)
    if (!component || !menu.routeName) {
      missed.push(
        `${menu.routeName ?? menu.menuName} -> ${menu.path} (${componentPath ?? 'no component'})`,
      )
      continue
    }
    const path = normalizeRoutePath(menu.path)
    if (!path) {
      missed.push(
        `${menu.routeName ?? menu.menuName} -> empty path (${componentPath ?? 'no component'})`,
      )
      continue
    }
    const route: RouteRecordRaw = {
      path,
      name: menu.routeName,
      component,
      meta: {
        title: menu.menuName,
        perm: menu.permissionCode,
        hidden: isStatusDisabled(menu.visible),
        productCode: menu.productCode,
      },
    }
    router.addRoute('Layout', route)
  }

  if (import.meta.env.DEV && missed.length) {
    console.warn('[dynamicRoutes] skipped routes:', missed)
  }
  if (import.meta.env.DEV) {
    const names = pages
      .filter((m) => m.routeName && resolveViewComponent(menuComponentPath(m)))
      .map((m) => `${m.routeName}:${normalizeRoutePath(m.path)}`)
    console.debug('[dynamicRoutes] registered:', names)
  }
  return menus
}

export function resetDynamicRoutes(router: Router, menus: MenuRouter[]) {
  for (const menu of flattenPageMenus(menus)) {
    if (menu.routeName && router.hasRoute(menu.routeName)) {
      router.removeRoute(menu.routeName)
    }
  }
}

function toAbsolutePath(path: string) {
  const trimmed = path.trim()
  return trimmed.startsWith('/') ? trimmed : `/${trimmed}`
}

/** Collects page menu paths in tree order (M/C menus with a path). */
export function collectMenuPaths(menus: MenuRouter[], bucket: string[] = []): string[] {
  for (const menu of menus) {
    if (menu.children?.length) {
      collectMenuPaths(menu.children, bucket)
    }
    if (menu.menuType === 'C' && menu.path?.trim()) {
      bucket.push(toAbsolutePath(menu.path))
    }
  }
  return bucket
}

/** First menu path that is already registered on the router instance. */
export function findFirstRegisteredPath(router: Router, menus: MenuRouter[]): string | undefined {
  for (const path of collectMenuPaths(menus)) {
    if (router.resolve(path).matched.length > 0) {
      return path
    }
  }
  return undefined
}
