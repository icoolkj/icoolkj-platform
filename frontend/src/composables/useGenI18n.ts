import { computed, ref, toValue, watch, type MaybeRefOrGetter } from 'vue'
import type { FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { GenFieldConstraints, GenFieldValidation, GenI18nMessages } from '@/types/tool/genI18n'

function interpolate(template: string, params?: Record<string, string | number>) {
  if (!params) return template
  return Object.entries(params).reduce(
    (text, [key, value]) => text.replace(new RegExp(`\\{${key}\\}`, 'g'), String(value)),
    template,
  )
}

/** Align vue-i18n / preference codes to bundled Git JSON keys. */
function normalizeGenLocale(locale: string): string {
  const raw = String(locale || '').trim()
  if (raw === 'zh-CN' || raw === 'en-US') return raw
  const lower = raw.toLowerCase().replace('_', '-')
  if (lower === 'zh' || lower.startsWith('zh-')) return 'zh-CN'
  if (lower === 'en' || lower.startsWith('en-')) return 'en-US'
  return raw
}

function fileMessagesForLocale(
  fileMessages: Record<string, GenI18nMessages> | undefined,
  locale: string,
): GenI18nMessages | null {
  if (!fileMessages) return null
  const code = normalizeGenLocale(locale)
  return fileMessages[code] ?? fileMessages['zh-CN'] ?? fileMessages['en-US'] ?? null
}

/**
 * Load low-code CRUD UI i18n from Git JSON bundled with the page (P0: Git-only runtime).
 * DB gen_i18n_message is for generator admin / future draft workflow — not read here.
 */
export function useGenI18n(
  _businessName: string,
  _apiModule: string,
  fileMessages?: MaybeRefOrGetter<Record<string, GenI18nMessages> | undefined>,
) {
  const { locale } = useI18n()
  const ready = ref(false)
  const messages = computed(() => fileMessagesForLocale(toValue(fileMessages), locale.value))

  watch(
    () => [locale.value, toValue(fileMessages)] as const,
    () => {
      ready.value = toValue(fileMessages) != null
    },
    { immediate: true, deep: true },
  )

  function gp(key: string, params?: Record<string, string | number>) {
    const value = messages.value?.page?.[key] ?? key
    return interpolate(value, params)
  }

  function gf(field: string, prop: 'label' | 'placeholder' | 'placeholderReject' = 'label') {
    const value = messages.value?.fields?.[field]?.[prop]
    if (value) return value
    return prop === 'label' ? field : ''
  }

  function gv(field: string, rule: keyof GenFieldValidation) {
    return messages.value?.fields?.[field]?.validation?.[rule] ?? ''
  }

  const formRules = computed<FormRules>(() => {
    const rules: FormRules = {}
    const constraints = messages.value?.meta?.constraints ?? {}
    for (const [field, constraint] of Object.entries(constraints)) {
      const fieldRules: FormRules[string] = []
      const c = constraint as GenFieldConstraints
      if (c.required) {
        fieldRules.push({
          required: true,
          message: gv(field, 'required') || gf(field, 'label'),
          trigger: ['blur', 'change'],
        })
      }
      if (c.maxLength != null && c.maxLength > 0) {
        fieldRules.push({
          max: c.maxLength,
          message:
            interpolate(gv(field, 'maxLength'), { max: c.maxLength }) ||
            `${gf(field, 'label')} max ${c.maxLength}`,
          trigger: 'blur',
        })
      }
      if (fieldRules.length) {
        rules[field] = fieldRules
      }
    }
    return rules
  })

  return {
    ready,
    messages,
    gp,
    gf,
    gv,
    formRules,
    reload: async () => {},
  }
}

/** No-op: runtime no longer caches DB overrides (Git-only). Kept for generator admin forms. */
export function clearGenI18nCache(_businessName?: string, _apiModule?: string) {}
