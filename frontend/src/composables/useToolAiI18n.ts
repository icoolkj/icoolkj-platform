import { useGenI18n } from '@/composables/useGenI18n'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import genI18nZhCN from '@/i18n/gen/toolAi/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/toolAi/en-US.json'

const GEN_I18N_FILES: Record<string, GenI18nMessages> = {
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}

/** Git-local toolAi page copy (not sys_i18n_message). */
export function useToolAiI18n() {
  return useGenI18n('toolAi', 'tool', GEN_I18N_FILES)
}
