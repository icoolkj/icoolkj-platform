export type ResolvedTenant = {
  /** 未命中时为 null，页面应展示兜底或 404 策略 */
  tenantCode: string | null
  /** 解析来源，便于调试 */
  source: 'subdomain' | 'query' | 'none'
}

/**
 * 从 Host / 查询参数解析租户编码（骨架）。
 * 后期对接：自定义域表、公开 bootstrap API。
 *
 * 约定示例：
 * - `{tenantCode}.icoolkj.com` → subdomain
 * - `?tenantCode=xxx` → query（本地联调）
 */
export function resolveTenantFromLocation(
  hostname = typeof window !== 'undefined' ? window.location.hostname : '',
  search = typeof window !== 'undefined' ? window.location.search : '',
): ResolvedTenant {
  const params = new URLSearchParams(search)
  const fromQuery = params.get('tenantCode')?.trim()
  if (fromQuery) {
    return { tenantCode: fromQuery, source: 'query' }
  }

  const host = hostname.toLowerCase()
  // localhost / IP：不解析子域
  if (host === 'localhost' || /^\d+\.\d+\.\d+\.\d+$/.test(host)) {
    return { tenantCode: null, source: 'none' }
  }

  const parts = host.split('.')
  // 至少 tenant.icoolkj.com → 3 段；跳过 www
  if (parts.length >= 3) {
    const sub = parts[0]
    if (sub && sub !== 'www' && sub !== 'platform' && sub !== 'app' && sub !== 'api') {
      return { tenantCode: sub, source: 'subdomain' }
    }
  }

  return { tenantCode: null, source: 'none' }
}
