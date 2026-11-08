import { inject, onMounted, onUnmounted, provide, type InjectionKey } from 'vue'

type RichEditorFlusher = () => void

interface RichEditorRegistry {
  register: (flusher: RichEditorFlusher) => () => void
  flushAll: () => void
}

const RICH_EDITOR_REGISTRY_KEY: InjectionKey<RichEditorRegistry> = Symbol('richEditorRegistry')

export function provideRichEditorRegistry() {
  const flushers = new Set<RichEditorFlusher>()
  const registry: RichEditorRegistry = {
    register(flusher) {
      flushers.add(flusher)
      return () => flushers.delete(flusher)
    },
    flushAll() {
      flushers.forEach((flush) => flush())
    },
  }
  provide(RICH_EDITOR_REGISTRY_KEY, registry)
  return registry
}

export function useRichEditorRegistry() {
  return inject(RICH_EDITOR_REGISTRY_KEY, null)
}

export function useRichEditorFlushRegister(flusher: RichEditorFlusher) {
  const registry = useRichEditorRegistry()
  let unregister: (() => void) | undefined
  onMounted(() => {
    if (registry) {
      unregister = registry.register(flusher)
    }
  })
  onUnmounted(() => {
    unregister?.()
  })
}
