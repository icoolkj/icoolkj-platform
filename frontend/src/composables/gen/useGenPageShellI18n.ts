import { ref } from 'vue'
import { useGenI18n } from '@/composables/useGenI18n'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import { getGenPageI18nFiles, loadGenPageBundle } from './useGenPageBundle'

export type GenPageShellI18n = ReturnType<typeof useGenI18n> & {
  pageKey: string
  i18nFiles: ReturnType<typeof ref<Record<string, GenI18nMessages>>>
}

/**
 * 复合页壳层 i18n — 与 GenRuntimePage 共用同一 Git bundle，避免重复 import zh-CN/en-US。
 * 用于 Tab 标签、工具栏按钮等 Runtime 外部的 gp/gf。
 */
export function useGenPageShellI18n(pageKey: string): GenPageShellI18n {
  const i18nFiles = ref<Record<string, GenI18nMessages>>(getGenPageI18nFiles(pageKey))
  loadGenPageBundle(pageKey).then((bundle) => {
    if (bundle) i18nFiles.value = bundle.i18nFiles
    else if (import.meta.env.DEV) {
      console.warn(`[useGenPageShellI18n] no i18n files for pageKey "${pageKey}"`)
    }
  })
  const i18n = useGenI18n(pageKey, '', i18nFiles)
  return {
    ...i18n,
    pageKey,
    i18nFiles,
  }
}
