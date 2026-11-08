import { createRouter, createWebHistory } from 'vue-router'
import { SITE_LOCALES, type SiteLocale } from '@/config/site'
import { setSiteLocale, readStoredLocale } from '@/i18n'
import { localeFromPath, localizedPath, stripLocalePrefix } from '@/i18n/paths'

const pageLoaders = {
  home: () => import('@/pages/home/index.vue'),
  product: () => import('@/pages/product/index.vue'),
  docs: () => import('@/pages/docs/index.vue'),
  docsCategory: () => import('@/pages/docs/category.vue'),
  license: () => import('@/pages/license/index.vue'),
  delivery: () => import('@/pages/delivery/index.vue'),
  about: () => import('@/pages/about/index.vue'),
} as const

type PageKey = keyof typeof pageLoaders

const pageKeys: { key: PageKey; segment: string }[] = [
  { key: 'home', segment: '' },
  { key: 'product', segment: 'product' },
  { key: 'docs', segment: 'docs' },
  { key: 'docsCategory', segment: 'docs/:category' },
  { key: 'license', segment: 'license' },
  { key: 'delivery', segment: 'delivery' },
  { key: 'about', segment: 'about' },
]

function buildRoutes() {
  return SITE_LOCALES.flatMap((loc) =>
    pageKeys.map(({ key, segment }) => {
      const path = loc.pathPrefix
        ? `/${loc.pathPrefix}${segment ? `/${segment}` : ''}`
        : segment
          ? `/${segment}`
          : '/'
      return {
        path,
        name: `${loc.code}-${key}`,
        component: pageLoaders[key],
        meta: { locale: loc.code satisfies SiteLocale },
      }
    }),
  )
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...buildRoutes(),
    {
      path: '/:pathMatch(.*)*',
      redirect: () => localizedPath('/', readStoredLocale() ?? 'zh-CN'),
    },
  ],
  scrollBehavior(to) {
    if (to.hash) {
      return { el: to.hash, top: 80 }
    }
    return { top: 0 }
  },
})

router.beforeEach((to, _from, next) => {
  const locale =
    (to.meta.locale as SiteLocale | undefined) ?? localeFromPath(to.path)
  setSiteLocale(locale)

  if (to.path === '/') {
    const stored = readStoredLocale()
    if (stored === 'en-US') {
      next({ path: localizedPath('/', 'en-US'), replace: true })
      return
    }
  }

  next()
})

export function switchLocaleKeepPage(target: SiteLocale) {
  const logical = stripLocalePrefix(router.currentRoute.value.path)
  return router.push(localizedPath(logical, target))
}

export default router
