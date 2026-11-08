import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import { useProductStore } from '@/stores/product'
import {
  findFirstRegisteredPath,
  setupDynamicRoutes,
  resetDynamicRoutes,
} from '@/router/dynamicRoutes'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { registerDevTestAccountRoutes } from '@/dev/test-accounts'
import { registerDevAttachmentShowcaseRoutes } from '@/dev/attachment-showcase'

const NO_ACCESS_PATH = '/no-access'
const FORCE_ENROLL_MFA_PATH = '/force-enroll-mfa'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { public: true },
    },
    {
      path: '/force-change-password',
      redirect: '/',
    },
    {
      path: '/force-enroll-mfa',
      name: 'ForceEnrollMfa',
      component: () => import('@/views/login/ForceEnrollMfa.vue'),
      meta: { forceEnrollMfa: true },
    },
    {
      path: '/',
      name: 'Layout',
      component: BasicLayout,
      redirect: '/tool/gen',
      children: [
        {
          path: 'no-access',
          name: 'NoAccess',
          component: () => import('@/views/error/NoAccess.vue'),
          meta: { titleKey: 'layout.noAccess' },
        },
        {
          path: 'tool/gen-edit/:id',
          name: 'ToolGenEdit',
          component: () => import('@/views/tool/gen/EditTable.vue'),
          meta: { titleKey: 'layout.editGenConfig', perm: 'tool:gen:edit', hidden: true },
        },
        {
          path: 'oa/tenant-template/configure/:id',
          name: 'OaTenantTemplateConfigure',
          component: () => import('@/views/oa/tenant-template/configure.vue'),
          meta: {
            titleKey: 'layout.oaTenantConfigure',
            perm: 'oa:tenantTemplate:query',
          },
        },
        {
          path: 'account/profile',
          name: 'AccountProfile',
          component: () => import('@/views/account/profile/index.vue'),
          meta: { genBusinessName: 'profile', genPageKey: 'title', hidden: true },
        },
      ],
    },
  ],
})

let dynamicRoutesReady = false
/** Prevents redirect loops when a target path cannot be registered. */
let unresolvedNavigationTarget: string | null = null

function resolveLandingPath(): string {
  const productStore = useProductStore()
  const home = productStore.current?.homePath?.trim()
  if (home) {
    const normalized = home.startsWith('/') ? home : `/${home}`
    if (router.hasRoute(router.resolve(normalized).name as string) || router.resolve(normalized).matched.length) {
      return normalized
    }
  }
  const menuStore = useMenuStore()
  const first = findFirstRegisteredPath(router, menuStore.allRouters.length ? menuStore.allRouters : menuStore.routers)
  if (first) {
    return first
  }
  return NO_ACCESS_PATH
}

export async function reloadAppRoutes() {
  const menuStore = useMenuStore()
  const productStore = useProductStore()
  const oldMenus = [...menuStore.allRouters]
  if (oldMenus.length) {
    resetDynamicRoutes(router, oldMenus)
  }
  menuStore.reset()
  productStore.reset()
  dynamicRoutesReady = false
  unresolvedNavigationTarget = null
  await setupDynamicRoutes(router, { force: true })
  await productStore.loadMine(true)
  await productStore.ensureSidebar()
  dynamicRoutesReady = true
}

router.beforeEach(async (to, _from, next) => {
  if (to.meta.public) {
    next()
    return
  }
  if (!getToken()) {
    next('/login')
    return
  }
  const userStore = useUserStore()
  if (!userStore.user) {
    await userStore.loadProfile()
  } else if (!dynamicRoutesReady && !to.meta.forceEnrollMfa) {
    // 刷新页面时 JWT 权限可能过期，/me 会按 DB 重新加载
    await userStore.loadProfile()
  }

  // 强制改密：进入主布局后由 ForceChangePasswordGate（弹框/抽屉）处理，不再跳独立页

  if (userStore.user?.mustEnrollMfa) {
    if (to.path === FORCE_ENROLL_MFA_PATH || to.meta.forceEnrollMfa) {
      next()
      return
    }
    next({ path: FORCE_ENROLL_MFA_PATH, replace: true })
    return
  }

  if (to.path === FORCE_ENROLL_MFA_PATH || to.meta.forceEnrollMfa) {
    next({ path: '/', replace: true })
    return
  }

  if (!dynamicRoutesReady) {
    await setupDynamicRoutes(router)
    const productStore = useProductStore()
    await productStore.loadMine(true)
    await productStore.ensureSidebar()
    dynamicRoutesReady = true
    next({ ...to, replace: true })
    return
  }

  if (to.matched.length === 0) {
    if (unresolvedNavigationTarget === to.fullPath) {
      unresolvedNavigationTarget = null
      next(to.fullPath === NO_ACCESS_PATH ? '/login' : NO_ACCESS_PATH)
      return
    }
    unresolvedNavigationTarget = to.fullPath
    const resolved = router.resolve(to.fullPath)
    if (resolved.matched.length > 0) {
      unresolvedNavigationTarget = null
      next({ path: to.fullPath, query: to.query, hash: to.hash, replace: true })
      return
    }
    if (import.meta.env.DEV) {
      console.warn('[router] no route after dynamic setup:', to.fullPath)
    }
    const landing = resolveLandingPath()
    next(landing === to.fullPath ? NO_ACCESS_PATH : landing)
    return
  }

  unresolvedNavigationTarget = null

  const perm = to.meta.perm as string | undefined
  if (perm && !userStore.permissions.includes(perm)) {
    const landing = resolveLandingPath()
    next(landing === to.fullPath ? NO_ACCESS_PATH : landing)
    return
  }
  next()
})

export function resetRouterState() {
  const menuStore = useMenuStore()
  const oldMenus = [...menuStore.routers]
  if (oldMenus.length) {
    resetDynamicRoutes(router, oldMenus)
  }
  menuStore.reset()
  dynamicRoutesReady = false
  unresolvedNavigationTarget = null
}

/** 联调测试账号页；后期删除 @/dev/test-accounts 并去掉本行即可 */
registerDevTestAccountRoutes(router)
/** 附件组件场景测试；后期删除 @/dev/attachment-showcase 并去掉本行即可 */
registerDevAttachmentShowcaseRoutes(router)

export default router
