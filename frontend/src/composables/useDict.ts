import { computed, reactive, ref, toValue, watch, type MaybeRefOrGetter } from 'vue'
import { getDictDataBatch } from '@/api/system/dict'
import type { DictOption } from '@/types/system/dict'

const cache = new Map<string, DictOption[]>()

function resolveDictTypes(...inputs: (string | MaybeRefOrGetter<string[]>)[]): string[] {
  const types: string[] = []
  for (const input of inputs) {
    if (typeof input === 'string') {
      if (input) types.push(input)
      continue
    }
    types.push(...toValue(input))
  }
  return [...new Set(types.filter(Boolean))]
}

/**
 * Load platform dictionary options by dict type keys (e.g. sys_common_status).
 * Used by code-generated CRUD pages when gen_table_column.dict_type is set.
 */
export function useDict(...dictTypes: (string | MaybeRefOrGetter<string[]>)[]) {
  const typesSource = computed(() => resolveDictTypes(...dictTypes))
  const dict = reactive<Record<string, DictOption[]>>({})
  const ready = ref(false)

  function getDict(dictType: string): DictOption[] {
    return dict[dictType] ?? []
  }

  async function loadDict() {
    const types = typesSource.value
    if (types.length === 0) {
      ready.value = true
      return
    }
    ready.value = false
    try {
      const missing = types.filter((type) => !cache.has(type))
      if (missing.length > 0) {
        const res = await getDictDataBatch(missing)
        for (const [type, options] of Object.entries(res.data.data)) {
          const list = options ?? []
          // Do not negative-cache empty dicts (e.g. sys_bool before V7 migration).
          if (list.length > 0) {
            cache.set(type, list)
          }
        }
      }
      for (const type of types) {
        dict[type] = cache.get(type) ?? []
      }
    } catch {
      for (const type of types) {
        dict[type] = cache.get(type) ?? []
      }
    } finally {
      ready.value = true
    }
  }

  watch(typesSource, loadDict, { immediate: true })

  return { dict, getDict, ready, reload: loadDict }
}
