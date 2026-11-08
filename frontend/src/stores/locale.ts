import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import { LOGIN_LOCALE_PENDING_KEY } from '@/constants/storage'
import {
  i18n,
  LOCALE_STORAGE_KEY,
  SUPPORT_LOCALES,
  applyDocumentTitle,
  type LocaleCode,
} from '@/i18n'
import { getConfigBootstrap } from '@/api/system/config'
import { updatePreference } from '@/api/auth/preference'
import { getToken } from '@/utils/request'

/** Set on login page language switch; consumed once after login to override server preference. */
export { LOGIN_LOCALE_PENDING_KEY }

const elementLocales: Record<string, typeof zhCn> = {
  'zh-CN': zhCn,
  'en-US': en,
}

export const useLocaleStore = defineStore('locale', () => {
  const locale = ref<LocaleCode>(i18n.global.locale.value as LocaleCode)
  const localeOptionsRef = ref([...SUPPORT_LOCALES])

  const elementLocale = computed(() => elementLocales[locale.value] ?? zhCn)

  const localeOptions = computed(() => localeOptionsRef.value)

  function applyLocale(code: LocaleCode) {
    locale.value = code
    i18n.global.locale.value = code
    localStorage.setItem(LOCALE_STORAGE_KEY, code)
    document.documentElement.lang = code
    applyDocumentTitle()
  }

  async function setLocale(code: LocaleCode, persist = true) {
    applyLocale(code)
    if (!getToken()) {
      sessionStorage.setItem(LOGIN_LOCALE_PENDING_KEY, code)
    }
    if (persist && getToken()) {
      sessionStorage.removeItem(LOGIN_LOCALE_PENDING_KEY)
      await updatePreference({ locale: code })
    }
  }

  /** Prefer locale chosen on the login page; otherwise fall back to server preference. */
  async function applyAfterLogin(serverLocale?: string | null) {
    const pending = sessionStorage.getItem(LOGIN_LOCALE_PENDING_KEY)
    sessionStorage.removeItem(LOGIN_LOCALE_PENDING_KEY)
    if (pending) {
      await setLocale(pending as LocaleCode, true)
      return
    }
    if (serverLocale) {
      applyLocale(serverLocale as LocaleCode)
    }
  }

  function syncLocaleOptions() {
    localeOptionsRef.value = [...SUPPORT_LOCALES]
  }

  async function initLocale() {
    syncLocaleOptions()
    try {
      const res = await getConfigBootstrap()
      const data = res.data.data
      if (data.locales?.length) {
        localeOptionsRef.value = data.locales.map((item) => ({
          label: item.nativeName || item.localeName,
          value: item.localeCode,
        }))
      }
      const serverDefault = data.defaultLocale
      const supported = data.supportedLocales ?? []
      const saved = localStorage.getItem(LOCALE_STORAGE_KEY)
      if (!saved && serverDefault) {
        applyLocale(serverDefault)
      }
      if (supported.length && !supported.includes(locale.value)) {
        applyLocale(supported[0])
      }
    } catch {
      // keep local preference when bootstrap unavailable
    }
    document.documentElement.lang = locale.value
  }

  return {
    locale,
    elementLocale,
    localeOptions,
    setLocale,
    applyLocale,
    applyAfterLogin,
    initLocale,
    syncLocaleOptions,
  }
})
