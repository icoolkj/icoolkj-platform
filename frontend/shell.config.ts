/**
 * Shell build / first-paint config loader.
 * <p>
 * Product SoT: {@code shell.config.json}（前后端镜像见 backend resources/shell/）。
 * 用户可见版权标签走 {@code i18n/shell/login}（login.copyright*）。
 * 运行时品牌优先 {@code GET /system/config/bootstrap} → {@code sys_brand_pack}。
 */
import raw from './shell.config.json'

export type ShellBootstrapCopy = {
  loading: string
  errorTitle: string
  errorHint: string
  retry: string
}

export type ShellCopyrightInfo = {
  brand: string
  icpNo: string
  icpUrl: string
  domain: string
  siteUrl: string
  email: string
}

export type ShellStorageKeys = {
  accessToken: string
  refreshToken: string
  locale: string
  layout: string
  theme: string
  loginRemember: string
  loginLocalePending: string
}

export type ShellDevtoolsConfig = {
  gapPx: number
  debugLagMs: number
  pollMs: number
}

export type ShellConfigFile = {
  defaultLang: string
  defaultTitle: string
  baiduHmId: string
  baiduHmDevEnabled: boolean
  blockDevtools: boolean
  storageKeys: ShellStorageKeys
  devtools: ShellDevtoolsConfig
  copyright: ShellCopyrightInfo
  bootstrap: Record<string, ShellBootstrapCopy>
}

export const shellConfig = raw as ShellConfigFile

export function shouldEnableBaiduHm(isProductionBuild: boolean): boolean {
  if (!shellConfig.baiduHmId?.trim()) return false
  if (isProductionBuild) return true
  return shellConfig.baiduHmDevEnabled === true
}

export function resolveShellBootstrap(locale?: string | null): ShellBootstrapCopy {
  const code = (locale || shellConfig.defaultLang).trim()
  return shellConfig.bootstrap[code] ?? shellConfig.bootstrap[shellConfig.defaultLang]
}

/** Site copyright values only (labels → i18n login.copyright*). */
export function resolveShellCopyright(): ShellCopyrightInfo {
  return { ...shellConfig.copyright }
}
