import { useGenI18n } from '@/composables/useGenI18n'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import genI18nZhCN from '@/i18n/gen/oaTenantTemplate/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/oaTenantTemplate/en-US.json'

const GEN_I18N_FILES: Record<string, GenI18nMessages> = {
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}

/** 流程设计器（画布 + 节点编辑）共用的 gen i18n（oaTenantTemplate） */
export function useDesignerGenI18n() {
  return useGenI18n('oaTenantTemplate', 'oa', GEN_I18N_FILES)
}
