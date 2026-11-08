import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import genI18nZhCN from '@/i18n/gen/operationLog/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/operationLog/en-US.json'

const GEN_I18N_FILES: Record<string, GenI18nMessages> = {
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}

function fileMessagesForLocale(locale: string): GenI18nMessages | null {
  return GEN_I18N_FILES[locale] ?? GEN_I18N_FILES['zh-CN'] ?? null
}

/** Translate @OperationLog module / businessType values stored in sys_operation_log. */
export function useOperationLogI18n() {
  const { locale } = useI18n()
  const messages = computed(() => fileMessagesForLocale(locale.value))

  function translateModule(name?: string | null) {
    if (!name) return '—'
    return messages.value?.audit?.modules?.[name] ?? name
  }

  function translateBusinessType(type?: string | null) {
    if (!type) return '—'
    return messages.value?.audit?.businessTypes?.[type] ?? type
  }

  return { translateModule, translateBusinessType }
}
