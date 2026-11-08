import { ref } from 'vue'
import { createI18n } from 'vue-i18n'
import { getI18nBootstrap, getI18nMessages } from '@/api/system/i18n'
import { unflattenMessages } from '@/utils/i18nLoader'
import { applyBrandDocumentTitle } from '@/utils/brandChrome'

import commonZhCN from '@/i18n/shell/common/zh-CN.json'
import commonEnUS from '@/i18n/shell/common/en-US.json'
import layoutZhCN from '@/i18n/shell/layout/zh-CN.json'
import layoutEnUS from '@/i18n/shell/layout/en-US.json'
import loginZhCN from '@/i18n/shell/login/zh-CN.json'
import loginEnUS from '@/i18n/shell/login/en-US.json'

import platformZhCN from '@/i18n/platform/zh-CN.json'
import platformEnUS from '@/i18n/platform/en-US.json'

import { LOCALE_STORAGE_KEY } from '@/constants/storage'
import { shellConfig } from '../../shell.config'

export type LocaleCode = string

export { LOCALE_STORAGE_KEY }

export const DEFAULT_LOCALE = shellConfig.defaultLang

export const FALLBACK_LOCALE = shellConfig.defaultLang

/** Filled from GET /system/i18n/bootstrap — locale catalog still from DB. */
export let SUPPORT_LOCALES: { label: string; value: LocaleCode }[] = []

export const i18nReady = ref(false)

export const i18nLoadError = ref<string | null>(null)

/** Platform shell copy (Git). Business pages use i18n/gen + useGenI18n. */
const SHELL_MESSAGES: Record<string, Record<string, unknown>> = {
  'zh-CN': {
    common: commonZhCN,
    layout: layoutZhCN,
    login: loginZhCN,
  },
  'en-US': {
    common: commonEnUS,
    layout: layoutEnUS,
    login: loginEnUS,
  },
}

/** Flat Git baseline for error / entity.name / validation / notify. */
const PLATFORM_FLAT: Record<string, Record<string, string>> = {
  'zh-CN': platformZhCN as Record<string, string>,
  'en-US': platformEnUS as Record<string, string>,
}

function resolveInitialLocale(): LocaleCode {
  const saved = localStorage.getItem(LOCALE_STORAGE_KEY)
  if (saved) {
    return saved
  }
  return DEFAULT_LOCALE
}

export const i18n = createI18n({
  legacy: false,
  locale: resolveInitialLocale(),
  fallbackLocale: FALLBACK_LOCALE,
  messages: {},
  missingWarn: import.meta.env.DEV,
  fallbackWarn: import.meta.env.DEV,
})

function shellMessagesFor(locale: string): Record<string, unknown> {
  return { ...(SHELL_MESSAGES[locale] ?? SHELL_MESSAGES[FALLBACK_LOCALE]) }
}

/** Shell + platform Git baselines (DB frontend scope cannot override these namespaces). */
function gitBaseMessagesFor(locale: string): Record<string, unknown> {
  const shell = shellMessagesFor(locale)
  const flat = PLATFORM_FLAT[locale] ?? PLATFORM_FLAT[FALLBACK_LOCALE] ?? {}
  const platform = unflattenMessages(flat)
  return {
    ...platform,
    ...shell,
  }
}

/**
 * Git shell + platform are the base; optional remaining frontend keys from DB are merged
 * but cannot override common / layout / login / error / entity / validation / notify / oa.
 */
async function fetchLocaleMessages(locale: string): Promise<Record<string, unknown>> {
  const base = gitBaseMessagesFor(locale)
  try {
    const res = await getI18nMessages(locale, 'frontend')
    const flat = res.data.data ?? {}
    if (!Object.keys(flat).length) {
      return base
    }
    const remote = unflattenMessages(flat)
    return {
      ...remote,
      ...base,
      common: base.common,
      layout: base.layout,
      login: base.login,
      error: base.error,
      entity: base.entity,
      validation: base.validation,
      notify: base.notify,
      oa: base.oa,
    }
  } catch {
    return base
  }
}

/** Load Git baselines + optional DB frontend extras; bootstrap locales from server. */
export async function loadRemoteI18n(): Promise<boolean> {
  i18nLoadError.value = null
  i18nReady.value = false

  try {
    const bootstrapRes = await getI18nBootstrap()
    const bootstrap = bootstrapRes.data.data

    if (bootstrap?.locales?.length) {
      SUPPORT_LOCALES = bootstrap.locales.map((item) => ({
        label: item.nativeName || item.localeName,
        value: item.localeCode,
      }))
    } else if (bootstrap?.supportedLocales?.length) {
      SUPPORT_LOCALES = bootstrap.supportedLocales.map((code) => ({
        label: code,
        value: code,
      }))
    }

    const locales = bootstrap?.supportedLocales?.length
      ? bootstrap.supportedLocales
      : SUPPORT_LOCALES.map((item) => item.value)

    const effectiveLocales = locales.length ? locales : [DEFAULT_LOCALE, 'en-US']

    await Promise.all(
      effectiveLocales.map(async (locale) => {
        i18n.global.setLocaleMessage(locale, await fetchLocaleMessages(locale))
      }),
    )

    const saved = localStorage.getItem(LOCALE_STORAGE_KEY)
    const defaultLocale = bootstrap?.defaultLocale || DEFAULT_LOCALE
    if (!saved && defaultLocale) {
      i18n.global.locale.value = defaultLocale
    }
    if (!effectiveLocales.includes(i18n.global.locale.value as string)) {
      i18n.global.locale.value = effectiveLocales[0]
    }

    document.documentElement.lang = i18n.global.locale.value as string
    applyDocumentTitle()
    i18nReady.value = true
    return true
  } catch (error) {
    // Bootstrap may fail offline — still ship Git baselines so login UI works.
    try {
      for (const locale of Object.keys(SHELL_MESSAGES)) {
        i18n.global.setLocaleMessage(locale, gitBaseMessagesFor(locale))
      }
      if (!SUPPORT_LOCALES.length) {
        SUPPORT_LOCALES = [
          { label: '简体中文', value: 'zh-CN' },
          { label: 'English', value: 'en-US' },
        ]
      }
      applyDocumentTitle()
      i18nReady.value = true
      return true
    } catch {
      i18nLoadError.value =
        error instanceof Error ? error.message : 'Failed to load i18n messages'
      return false
    }
  }
}

/** Reload messages after admin cache refresh or locale message edits. */
export async function reloadRemoteI18n(): Promise<boolean> {
  const ok = await loadRemoteI18n()
  if (ok) {
    applyDocumentTitle()
  }
  return ok
}

/** Sync browser tab title with OEM brand (shell layout.pageTitle 仅作兜底). */
export function applyDocumentTitle() {
  try {
    applyBrandDocumentTitle(String(i18n.global.locale.value || ''))
    return
  } catch {
    /* pinia / brand store 尚未就绪 */
  }
  const title = i18n.global.t('layout.pageTitle')
  if (title && title !== 'layout.pageTitle') {
    document.title = title
  }
}

export default i18n
