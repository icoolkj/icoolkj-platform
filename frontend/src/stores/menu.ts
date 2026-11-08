import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMenuRouters } from '@/api/system/menu'
import type { MenuRouter } from '@/types/system/menu'

export const useMenuStore = defineStore('menu', () => {
  /** 全量路由树（动态注册；不含 product 过滤） */
  const allRouters = ref<MenuRouter[]>([])
  /** 当前产品侧栏树 */
  const routers = ref<MenuRouter[]>([])
  const loaded = ref(false)
  const sidebarLoaded = ref(false)

  async function loadAllRouters(force = false) {
    if (loaded.value && !force) {
      return allRouters.value
    }
    const res = await getMenuRouters()
    allRouters.value = res.data.data ?? []
    loaded.value = true
    return allRouters.value
  }

  /** @deprecated 兼容旧调用：等同 loadAllRouters，并同步侧栏为全量 */
  async function loadRouters(force = false) {
    const all = await loadAllRouters(force)
    routers.value = all
    sidebarLoaded.value = true
    return all
  }

  async function loadSidebarRouters(productCode: string, force = false) {
    if (!force && sidebarLoaded.value && routers.value.length) {
      // still reload when product changes — caller should force
    }
    const res = await getMenuRouters(productCode)
    routers.value = res.data.data ?? []
    sidebarLoaded.value = true
    return routers.value
  }

  function reset() {
    allRouters.value = []
    routers.value = []
    loaded.value = false
    sidebarLoaded.value = false
  }

  return {
    allRouters,
    routers,
    loaded,
    sidebarLoaded,
    loadAllRouters,
    loadRouters,
    loadSidebarRouters,
    reset,
  }
})
