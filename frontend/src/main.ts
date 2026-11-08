import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'highlight.js/styles/github.css'
import '@/styles/index.scss'
import App from './App.vue'
import router from './router'
import i18n, { loadRemoteI18n } from '@/i18n'
import { useLocaleStore } from '@/stores/locale'
import { useThemeStore } from '@/stores/theme'
import { useLayoutStore } from '@/stores/layout'
import { setupDirectives } from './directives/permission'
import { DEFAULT_LOCALE, LOCALE_STORAGE_KEY } from '@/i18n'
import { getConfigBootstrap } from '@/api/system/config'
import { useDemoStore } from '@/stores/demo'
import { useBrandStore } from '@/stores/brand'
import { applyBrandChrome } from '@/utils/brandChrome'
import { setupBaiduAnalytics } from '@/utils/baiduAnalytics'
import { applyElementPlusDefaults } from '@/utils/elementPlusDefaults'
import { registerGenPlatform } from '@/components/gen/registry/platform'

registerGenPlatform()

async function bootstrap() {
  try {
    document.documentElement.lang = localStorage.getItem(LOCALE_STORAGE_KEY) || DEFAULT_LOCALE
  } catch {
    document.documentElement.lang = DEFAULT_LOCALE
  }

  await loadRemoteI18n()

  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(router)
  setupBaiduAnalytics(router)
  app.use(i18n)
  app.use(ElementPlus)
  applyElementPlusDefaults()
  setupDirectives(app)

  try {
    const configBootstrap = await getConfigBootstrap()
    const data = configBootstrap.data.data
    useDemoStore().setDemoMode(data?.demoMode === true)
    useBrandStore().setBrand(data?.brand)
  } catch {
    useDemoStore().setDemoMode(false)
  }

  const localeStore = useLocaleStore()
  const themeStore = useThemeStore()
  const layoutStore = useLayoutStore()
  const brandStore = useBrandStore()
  localeStore.syncLocaleOptions()
  await localeStore.initLocale()
  themeStore.initTheme()
  layoutStore.initLayout()
  try {
    document.title = brandStore.productNameForLocale(localeStore.locale)
  } catch {
    /* keep shell defaultTitle */
  }
  void applyBrandChrome(localeStore.locale)

  app.mount('#app')
}

bootstrap()
