/** AUTO-GENERATED — 异步任务日志表 类型 */

export interface AsyncTaskLogVO {
  id?: string
  tenantId?: string
  taskId?: string
  logLevel?: string
  logContent?: string
}

export interface AsyncTaskLogPageQuery {
  pageNo?: number
  pageSize?: number
  tenantId?: string
  taskId?: string
  logLevel?: string
  logContent?: string
}

export interface AsyncTaskLogForm {
  id?: string
  tenantId?: string
  taskId?: string
  logLevel?: string
  logContent?: string
}
