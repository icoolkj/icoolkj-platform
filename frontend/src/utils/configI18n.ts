import type { ComposerTranslation } from 'vue-i18n'
import type { SysConfig } from '@/types/system/config'

export function displayConfigName(t: ComposerTranslation, row: SysConfig) {
  const key = `systemConfig.configKeys.${row.configKey}`
  const translated = t(key)
  return translated === key ? row.configName : translated
}
