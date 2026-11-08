import type { SiteView } from '@/i18n/types'
import homeView from '@/i18n/pages/home/view.json'
import productView from '@/i18n/pages/product/view.json'
import licenseView from '@/i18n/pages/license/view.json'
import deliveryView from '@/i18n/pages/delivery/view.json'
import aboutView from '@/i18n/pages/about/view.json'
import docsView from '@/i18n/pages/docs/view.json'

const registry = {
  home: homeView as SiteView,
  product: productView as SiteView,
  license: licenseView as SiteView,
  delivery: deliveryView as SiteView,
  about: aboutView as SiteView,
  docs: docsView as SiteView,
} as const

export type SitePageKey = keyof typeof registry

export function getSiteView(page: SitePageKey): SiteView {
  return registry[page]
}

export function useSiteView(page: SitePageKey) {
  const view = getSiteView(page)
  return {
    view,
    sections: view.sections,
    prefix: view.page.i18nPrefix,
    layout: view.page.layout,
    related: view.related ?? [],
  }
}
