import { createI18n } from 'vue-i18n'
import type { Composer } from 'vue-i18n'
import {
  DEFAULT_LOCALE,
  LOCALE_STORAGE_KEY,
  type SiteLocale,
} from '@/config/site'

import shellZh from './shell/zh-CN.json'
import shellEn from './shell/en-US.json'
import docsZh from './docs/zh-CN.json'
import docsEn from './docs/en-US.json'
import homeZh from './pages/home/zh-CN.json'
import homeEn from './pages/home/en-US.json'
import productZh from './pages/product/zh-CN.json'
import productEn from './pages/product/en-US.json'
import licenseZh from './pages/license/zh-CN.json'
import licenseEn from './pages/license/en-US.json'
import deliveryZh from './pages/delivery/zh-CN.json'
import deliveryEn from './pages/delivery/en-US.json'
import aboutZh from './pages/about/zh-CN.json'
import aboutEn from './pages/about/en-US.json'

function mergeMessages(...parts: object[]) {
  return Object.assign({}, ...parts)
}

export const i18n = createI18n({
  legacy: false,
  locale: DEFAULT_LOCALE,
  fallbackLocale: DEFAULT_LOCALE,
  messages: {
    'zh-CN': mergeMessages(shellZh, docsZh, homeZh, productZh, licenseZh, deliveryZh, aboutZh),
    'en-US': mergeMessages(shellEn, docsEn, homeEn, productEn, licenseEn, deliveryEn, aboutEn),
  },
})

const composer = i18n.global as unknown as Composer

export function applyDocumentLocale(locale: SiteLocale) {
  document.documentElement.lang = locale === 'en-US' ? 'en' : 'zh-CN'
  document.title = String(composer.t('meta.title'))
  const desc = document.querySelector('meta[name="description"]')
  if (desc) {
    desc.setAttribute('content', String(composer.t('meta.description')))
  }
}

export function setSiteLocale(locale: SiteLocale) {
  composer.locale.value = locale
  localStorage.setItem(LOCALE_STORAGE_KEY, locale)
  applyDocumentLocale(locale)
}

export function readStoredLocale(): SiteLocale | null {
  const saved = localStorage.getItem(LOCALE_STORAGE_KEY)
  if (saved === 'zh-CN' || saved === 'en-US') {
    return saved
  }
  return null
}

export default i18n
