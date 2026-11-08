import type { Router } from 'vue-router'
import { DEV_TEST_ACCOUNTS_PATH } from './data'

/**
 * 注册联调测试账号页。
 * 后期去除：删除本目录，并去掉 main/router 中对本函数的调用。
 */
export function registerDevTestAccountRoutes(router: Router) {
  if (router.hasRoute('DevTestAccounts')) {
    return
  }
  router.addRoute({
    path: DEV_TEST_ACCOUNTS_PATH,
    name: 'DevTestAccounts',
    component: () => import('./TestAccountsPage.vue'),
    meta: { public: true },
  })
}

export { DEV_TEST_ACCOUNTS_PATH } from './data'
