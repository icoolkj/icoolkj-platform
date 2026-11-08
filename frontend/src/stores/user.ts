import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  fetchMe,
  login as loginApi,
  logout as logoutApi,
  verifyMfa as verifyMfaApi,
  type LoginRequest,
  type LoginResponse,
} from '@/api/auth'
import type { LoginUser } from '@/types/common'
import { resetRouterState } from '@/router'
import { getToken } from '@/utils/request'
import { useLocaleStore } from '@/stores/locale'
import { useThemeStore } from '@/stores/theme'
import { useLayoutStore } from '@/stores/layout'
import { useTagsViewStore } from '@/stores/tagsView'
import { useMenuStore } from '@/stores/menu'
import { useProductStore } from '@/stores/product'
import { useBrandStore } from '@/stores/brand'
import { getConfigBootstrap } from '@/api/system/config'
import { applyBrandChrome } from '@/utils/brandChrome'
import { LAYOUT_STORAGE_KEY } from '@/utils/layout'

export const useUserStore = defineStore('user', () => {
  const user = ref<LoginUser | null>(null)
  const permissions = ref<string[]>([])

  async function login(payload: LoginRequest): Promise<LoginResponse> {
    resetRouterState()
    const result = await loginApi(payload)
    if (result.mfaRequired) {
      return result
    }
    await loadProfile()
    return result
  }

  async function completeMfa(mfaTicket: string, code: string): Promise<LoginResponse> {
    resetRouterState()
    const result = await verifyMfaApi(mfaTicket, code)
    await loadProfile()
    return result
  }

  async function loadProfile() {
    if (!getToken()) {
      user.value = null
      permissions.value = []
      return
    }
    const profile = await fetchMe()
    user.value = profile
    permissions.value = profile.permissions ?? []
    await applyUiPreferences(profile)
    await refreshRuntimeBrand()
  }

  /** 登录后按当前租户刷新侧栏/标题品牌（bootstrap 支持可选 JWT） */
  async function refreshRuntimeBrand() {
    try {
      const res = await getConfigBootstrap()
      useBrandStore().setBrand(res.data.data?.brand)
      await applyBrandChrome()
    } catch {
      /* ignore */
    }
  }

  async function applyUiPreferences(profile: LoginUser) {
    const localeStore = useLocaleStore()
    const themeStore = useThemeStore()
    await localeStore.applyAfterLogin(profile.locale)
    if (profile.theme) {
      themeStore.applyTheme(profile.theme)
    }
    const layoutStore = useLayoutStore()
    if (profile.layout) {
      layoutStore.applySettings(profile.layout)
    } else {
      layoutStore.initLayout()
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 改密等场景会话可能已失效，本地清理仍须执行
    } finally {
      user.value = null
      permissions.value = []
      useTagsViewStore().clearViews()
      useMenuStore().reset()
      useProductStore().reset()
      localStorage.removeItem(LAYOUT_STORAGE_KEY)
      resetRouterState()
      // 回到匿名：恢复平台品牌，避免租户名残留在登录页壳层 store
      try {
        const res = await getConfigBootstrap()
        useBrandStore().setBrand(res.data.data?.brand)
        await applyBrandChrome()
      } catch {
        useBrandStore().setBrand(null)
      }
    }
  }

  return { user, permissions, login, completeMfa, loadProfile, logout, applyUiPreferences, refreshRuntimeBrand }
})
