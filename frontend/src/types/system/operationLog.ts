/** AUTO-GENERATED — 操作日志表 类型 */

export interface OperationLogVO {
  id?: string
  traceId?: string
  moduleName?: string
  businessType?: string
  method?: string
  requestMethod?: string
  operatorId?: string
  operatorName?: string
  operationUrl?: string
  operationIp?: string
  operationLocation?: string
  requestParam?: string
  responseResult?: string
  operationStatus?: string
  errorMessage?: string
  costTime?: number
  operationAt?: string
}

export interface OperationLogPageQuery {
  pageNo?: number
  pageSize?: number
  traceId?: string
  moduleName?: string
  businessType?: string
  method?: string
  requestMethod?: string
  operatorId?: string
  operatorName?: string
  operationUrl?: string
  operationIp?: string
  operationLocation?: string
  requestParam?: string
  responseResult?: string
  operationStatus?: string
  errorMessage?: string
  costTime?: number
  operationAt?: string
}

export interface OperationLogForm {
  id?: string
  traceId?: string
  moduleName?: string
  businessType?: string
  method?: string
  requestMethod?: string
  operatorId?: string
  operatorName?: string
  operationUrl?: string
  operationIp?: string
  operationLocation?: string
  requestParam?: string
  responseResult?: string
  operationStatus?: string
  errorMessage?: string
  costTime?: number
  operationAt?: string
}
