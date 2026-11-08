import type { Router } from 'vue-router'
import { shouldEnableBaiduHm } from '../../shell.config'

declare global {
  interface Window {
    _hmt?: Array<unknown[]>
  }
}

function isBaiduAnalyticsEnabled(): boolean {
  return shouldEnableBaiduHm(import.meta.env.PROD)
}

export function trackBaiduPageview(pagePath?: string) {
  if (!isBaiduAnalyticsEnabled()) return
  const page =
    pagePath ?? `${window.location.pathname}${window.location.search}${window.location.hash}`
  window._hmt?.push(['_trackPageview', page])
}

/** SPA 路由切换时上报 PV（百度统计默认只统计首屏） */
export function setupBaiduAnalytics(router: Router) {
  if (!isBaiduAnalyticsEnabled()) return
  router.afterEach((to) => {
    trackBaiduPageview(to.fullPath)
  })
  trackBaiduPageview(router.currentRoute.value.fullPath)
}
