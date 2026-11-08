/** AUTO-GENERATED — 第三方调用日志表 类型 */

export interface ThirdpartyCallLogVO {
  id?: string
  tenantId?: string
  providerName?: string
  bizType?: string
  requestUrl?: string
  requestMethod?: string
  requestData?: string
  responseData?: string
  responseStatus?: string
  errorMessage?: string
  costTime?: number
  calledAt?: string
}

export interface ThirdpartyCallLogPageQuery {
  pageNo?: number
  pageSize?: number
  tenantId?: string
  providerName?: string
  bizType?: string
  requestUrl?: string
  requestMethod?: string
  requestData?: string
  responseData?: string
  responseStatus?: string
  errorMessage?: string
  costTime?: number
  calledAt?: string
}

export interface ThirdpartyCallLogForm {
  id?: string
  tenantId?: string
  providerName?: string
  bizType?: string
  requestUrl?: string
  requestMethod?: string
  requestData?: string
  responseData?: string
  responseStatus?: string
  errorMessage?: string
  costTime?: number
  calledAt?: string
}
