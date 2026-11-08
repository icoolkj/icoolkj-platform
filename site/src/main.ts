import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import i18n, { applyDocumentLocale, readStoredLocale, setSiteLocale } from './i18n'
import { DEFAULT_LOCALE } from './config/site'
import { applySiteTheme, resolveInitialTheme } from './utils/theme'
import './styles/index.css'

const initial = readStoredLocale() ?? DEFAULT_LOCALE
setSiteLocale(initial)
applyDocumentLocale(initial)
applySiteTheme(resolveInitialTheme())

createApp(App).use(router).use(i18n).mount('#app')
