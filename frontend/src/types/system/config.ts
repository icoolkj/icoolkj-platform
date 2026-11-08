/** AUTO-GENERATED — 参数配置 类型 */

export interface ConfigVO {
  id?: string
  configName: string
  configKey: string
  configValue: string
  configType?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

/** @deprecated use ConfigVO */
export type SysConfig = ConfigVO

export interface ConfigPageQuery {
  pageNo?: number
  pageSize?: number
  configName?: string
  configKey?: string
  configType?: string
  /** 分类 Tab：键前缀，如 security.login. */
  configKeyPrefix?: string
  /** 分类「其他」：逗号分隔需排除的前缀 */
  configKeyExcludePrefixes?: string
}

/** OEM / 站点品牌包（与后端 BrandPackVO 对齐） */
export interface BrandPack {
  productName?: string
  productNameI18n?: Record<string, string>
  copyrightBrand?: string
  icpNo?: string
  icpUrl?: string
  domain?: string
  siteUrl?: string
  email?: string
  contactName?: string
  contactPhone?: string
  /** profile_code → fileId */
  assets?: Record<string, string>
  /** profile_code → 说明文案 */
  labels?: Record<string, string>
  /**
   * 运行时用途 → fileId（后端按启用目录槽位 chrome_roles 解析）
   * 如 sidebar_logo / login_logo / favicon / qr_wechat
   */
  chrome?: Record<string, string>
  /** 运行时用途 → caption */
  chromeLabels?: Record<string, string>
}


export interface ConfigBootstrap {
  defaultLocale: string
  supportedLocales: string[]
  locales?: import('./i18n').I18nLocale[]
  demoMode?: boolean
  brand?: BrandPack | null
}

export interface ConfigForm {
  id?: string
  configName: string
  configKey: string
  configValue: string
  configType?: string
  remark?: string
}

export interface ConfigResolvedVO {
  configKey: string
  configValue: string
}
