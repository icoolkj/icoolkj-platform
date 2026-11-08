/**
 * 租户门户默认配置。真实品牌由运行时租户解析 / 后期公开 API 覆盖。
 */
export const portalConfig = {
  /** 仅作未解析到租户时的兜底，正式 UI 应以租户品牌为准 */
  fallbackBrand: 'iCoolKJ Platform',
  /** Admin 控制台基址；跳转登录时可附带 tenantCode */
  adminConsoleUrl: 'https://platform.example.com/',
  loginPath: '/login',
} as const

export function buildAdminLoginUrl(tenantCode?: string | null): string {
  const base = `${portalConfig.adminConsoleUrl.replace(/\/$/, '')}${portalConfig.loginPath}`
  if (!tenantCode) {
    return base
  }
  const url = new URL(base)
  url.searchParams.set('tenantCode', tenantCode)
  return url.toString()
}
