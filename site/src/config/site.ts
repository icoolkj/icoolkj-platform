/**
 * Site-wide non-translated config. Brand is always iCoolKJ Platform.
 * UI copy lives in src/i18n/locales/*.ts
 */
export const siteConfig = {
  brand: 'iCoolKJ Platform',
  consoleUrl: 'https://platform.example.com/',
  contact: {
    email: 'icoolkj@163.com',
    domain: 'icoolkj.com',
    siteUrl: 'https://icoolkj.com',
    icpNo: '请填写备案号',
    icpUrl: 'https://beian.miit.gov.cn/',
  },
  /** Align with Admin `--ic-primary`; day/night tokens in styles/index.css */
  colors: {
    primary: '#1677ff',
    primaryDark: '#0958d9',
    ink: '#1f2329',
    mist: '#f5f7fa',
  },
} as const

export type SiteLocale = 'zh-CN' | 'en-US'

export const SITE_LOCALES: {
  code: SiteLocale
  pathPrefix: string
  label: string
}[] = [
  { code: 'zh-CN', pathPrefix: '', label: '简体中文' },
  { code: 'en-US', pathPrefix: 'en', label: 'English' },
]

export const DEFAULT_LOCALE: SiteLocale = 'zh-CN'
export const LOCALE_STORAGE_KEY = 'icoolkj-site-locale'
