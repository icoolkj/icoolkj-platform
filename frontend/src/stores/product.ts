import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { listMyProducts } from '@/api/system/product'
import type { ProductVO } from '@/types/system/product'
import { useMenuStore } from '@/stores/menu'
import { useTagsViewStore } from '@/stores/tagsView'
import { resolveDefaultProductCode } from '@/constants/product'

const STORAGE_KEY = 'icoolkj.ui.product'

export const useProductStore = defineStore('product', () => {
  const available = ref<ProductVO[]>([])
  const currentCode = ref<string>('')
  const loaded = ref(false)

  const current = computed(() => available.value.find((p) => p.code === currentCode.value) ?? null)

  function readStored(): string {
    try {
      return localStorage.getItem(STORAGE_KEY)?.trim() || ''
    } catch {
      return ''
    }
  }

  function writeStored(code: string) {
    try {
      if (code) localStorage.setItem(STORAGE_KEY, code)
      else localStorage.removeItem(STORAGE_KEY)
    } catch {
      /* ignore */
    }
  }

  async function loadMine(force = false) {
    if (loaded.value && !force) {
      return available.value
    }
    const res = await listMyProducts()
    available.value = res.data.data ?? []
    loaded.value = true

    const stored = readStored()
    const codes = new Set(available.value.map((p) => p.code))
    if (stored && codes.has(stored)) {
      currentCode.value = stored
    } else if (codes.size) {
      currentCode.value = resolveDefaultProductCode(codes)
      writeStored(currentCode.value)
    } else {
      currentCode.value = ''
    }
    return available.value
  }

  async function switchProduct(code: string) {
    if (!code || code === currentCode.value) {
      return current.value
    }
    const hit = available.value.find((p) => p.code === code)
    if (!hit) {
      return null
    }
    currentCode.value = code
    writeStored(code)
    useTagsViewStore().closeAll()
    await useMenuStore().loadSidebarRouters(code, true)
    return hit
  }

  async function ensureSidebar() {
    if (!currentCode.value) {
      await loadMine()
    }
    if (currentCode.value) {
      await useMenuStore().loadSidebarRouters(currentCode.value, true)
    }
  }

  function reset() {
    available.value = []
    currentCode.value = ''
    loaded.value = false
  }

  return {
    available,
    currentCode,
    current,
    loaded,
    loadMine,
    switchProduct,
    ensureSidebar,
    reset,
  }
})
