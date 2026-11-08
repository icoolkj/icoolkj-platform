import { ref, watch, type MaybeRefOrGetter, toValue } from 'vue'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import type { GenViewSchema } from '@/types/tool/genView'

/**
 * 页面 bundle 全量懒加载：view.json 布局声明 + zh-CN / en-US 文案都在 pageKey
 * 打开时按需 import，避免全部业务页声明与文案打进首屏主包。
 * 壳层（useGenPageShellI18n）通过异步加载 + 响应式缓存获得文案。
 */
const viewJsonModules = import.meta.glob<GenViewSchema>('@/i18n/gen/*/view.json', {
  import: 'default',
})

const zhModules = import.meta.glob<GenI18nMessages>('@/i18n/gen/*/zh-CN.json', {
  import: 'default',
})

const enModules = import.meta.glob<GenI18nMessages>('@/i18n/gen/*/en-US.json', {
  import: 'default',
})

function pageKeyFromGlobPath(path: string): string | null {
  const match = path.match(/\/i18n\/gen\/([^/]+)\/(?:view|zh-CN|en-US)\.json$/)
  return match?.[1] ?? null
}

const viewLoaders = new Map<string, () => Promise<GenViewSchema>>()
for (const [path, loader] of Object.entries(viewJsonModules)) {
  const pageKey = pageKeyFromGlobPath(path)
  if (pageKey) viewLoaders.set(pageKey, loader)
}

const zhLoaders = new Map<string, () => Promise<GenI18nMessages>>()
for (const [path, loader] of Object.entries(zhModules)) {
  const pageKey = pageKeyFromGlobPath(path)
  if (pageKey) zhLoaders.set(pageKey, loader)
}

const enLoaders = new Map<string, () => Promise<GenI18nMessages>>()
for (const [path, loader] of Object.entries(enModules)) {
  const pageKey = pageKeyFromGlobPath(path)
  if (pageKey) enLoaders.set(pageKey, loader)
}

export type GenPageBundle = {
  pageKey: string
  view: GenViewSchema
  i18nFiles: Record<string, GenI18nMessages>
}

/** 已加载的 i18n 缓存（懒加载后填充；未加载过返回空对象）。 */
const i18nCache = new Map<string, Record<string, GenI18nMessages>>()

/** 同步读取某 pageKey 已加载的 Git i18n 文件；未加载时为空，配合 loadGenPageBundle 异步填充。 */
export function getGenPageI18nFiles(pageKey: string): Record<string, GenI18nMessages> {
  return i18nCache.get(pageKey) ?? {}
}

const bundleCache = new Map<string, Promise<GenPageBundle | undefined>>()

/** 懒加载页面 bundle（view.json 按需 import + i18n 同步取），结果带缓存。 */
export function loadGenPageBundle(pageKey: string): Promise<GenPageBundle | undefined> {
  let pending = bundleCache.get(pageKey)
  if (!pending) {
    pending = (async () => {
      const loader = viewLoaders.get(pageKey)
      if (!loader) return undefined
      const [view, zh, en] = await Promise.all([
        loader(),
        zhLoaders.get(pageKey)?.(),
        enLoaders.get(pageKey)?.(),
      ])
      const i18nFiles: Record<string, GenI18nMessages> = {}
      if (zh) i18nFiles['zh-CN'] = zh
      if (en) i18nFiles['en-US'] = en
      i18nCache.set(pageKey, i18nFiles)
      return {
        pageKey,
        view,
        i18nFiles,
      }
    })()
    bundleCache.set(pageKey, pending)
  }
  return pending
}

/** 全部 pageKey（由 glob 静态键推导，无需加载内容）。 */
export function listGenPageKeys(): string[] {
  return [...viewLoaders.keys()].sort()
}

export function useGenPageBundle(pageKey: MaybeRefOrGetter<string>) {
  const bundle = ref<GenPageBundle | null>(null)
  const ready = ref(false)
  const error = ref<string | null>(null)
  let seq = 0

  watch(
    () => toValue(pageKey),
    async (key) => {
      const current = ++seq
      bundle.value = null
      ready.value = false
      if (!key) {
        error.value = 'GenRuntimePage: pageKey is required'
        return
      }
      const loaded = await loadGenPageBundle(key)
      if (current !== seq) return // 页面切换竞态：丢弃过期结果
      if (!loaded) {
        error.value = `GenRuntimePage: no page bundle under frontend/src/i18n/gen/${key}/`
        return
      }
      bundle.value = loaded
      ready.value = true
      error.value = null
    },
    { immediate: true },
  )

  return { bundle, ready, error }
}
