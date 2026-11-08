/** AUTO-GENERATED — 接口访问日志表 类型 */

export interface ApiAccessLogVO {
  id?: string
  tenantId?: string
  traceId?: string
  requestUri?: string
  requestMethod?: string
  clientIp?: string
  userId?: string
  username?: string
  requestParam?: string
  responseCode?: number
  responseMessage?: string
  costTime?: number
  accessedAt?: string
}

export interface ApiAccessLogPageQuery {
  pageNo?: number
  pageSize?: number
  tenantId?: string
  traceId?: string
  requestUri?: string
  requestMethod?: string
  clientIp?: string
  userId?: string
  username?: string
  requestParam?: string
  responseCode?: number
  responseMessage?: string
  costTime?: number
  accessedAt?: string
}

export interface ApiAccessLogForm {
  id?: string
  tenantId?: string
  traceId?: string
  requestUri?: string
  requestMethod?: string
  clientIp?: string
  userId?: string
  username?: string
  requestParam?: string
  responseCode?: number
  responseMessage?: string
  costTime?: number
  accessedAt?: string
}
