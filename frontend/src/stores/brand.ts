import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { BrandPack } from '@/types/system/config'
import { resolveShellCopyright, shellConfig, type ShellCopyrightInfo } from '../../shell.config'

/** 运行时品牌（bootstrap sys_brand_pack）；JS 加载前仍用 shell.config.json */
export const useBrandStore = defineStore('brand', () => {
  const pack = ref<BrandPack | null>(null)

  function setBrand(next?: BrandPack | null) {
    pack.value = next ?? null
  }

  const productName = computed(() => {
    const p = pack.value
    if (!p) return shellConfig.defaultTitle
    return p.productName || shellConfig.defaultTitle
  })

  function productNameForLocale(locale?: string | null): string {
    const p = pack.value
    if (!p) return shellConfig.defaultTitle
    const code = locale || shellConfig.defaultLang
    const i18nName = p.productNameI18n?.[code]
    if (i18nName?.trim()) return i18nName.trim()
    if (p.productName?.trim()) return p.productName.trim()
    return shellConfig.defaultTitle
  }

  function sidebarBrand(locale?: string | null): string {
    return productNameForLocale(locale)
  }

  function sidebarBrandMini(locale?: string | null): string {
    const name = productNameForLocale(locale)
    if (/[\u4e00-\u9fff]/.test(name)) {
      return name.slice(0, 2)
    }
    const token = name.trim().split(/\s+/)[0] || name
    return token.slice(0, 2).toUpperCase() || 'IC'
  }

  /** 侧栏 Logo：后端 chrome.sidebar_logo（目录启用槽位解析） */
  function sidebarLogoFileId(): string {
    return (pack.value?.chrome?.sidebar_logo || '').trim()
  }

  function faviconFileId(): string {
    return (pack.value?.chrome?.favicon || '').trim()
  }

  function copyrightForLocale(_locale?: string | null): ShellCopyrightInfo {
    const base = resolveShellCopyright()
    const p = pack.value
    if (!p) return base
    return {
      ...base,
      brand: p.copyrightBrand?.trim() || base.brand,
      icpNo: p.icpNo?.trim() || base.icpNo,
      icpUrl: p.icpUrl?.trim() || base.icpUrl,
      domain: p.domain?.trim() || base.domain,
      siteUrl: p.siteUrl?.trim() || base.siteUrl,
      email: p.email?.trim() || base.email,
    }
  }

  return {
    pack,
    setBrand,
    productName,
    productNameForLocale,
    sidebarBrand,
    sidebarBrandMini,
    sidebarLogoFileId,
    faviconFileId,
    copyrightForLocale,
  }
})
