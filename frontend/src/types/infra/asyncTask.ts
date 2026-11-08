/** AUTO-GENERATED — 异步任务表 类型 */

export interface AsyncTaskVO {
  id?: string
  tenantId?: string
  taskType?: string
  taskName?: string
  bizType?: string
  bizId?: string
  taskStatus?: string
  requestParam?: string
  resultData?: string
  errorMessage?: string
  retryCount?: number
  maxRetryCount?: number
  requeueCount?: number
  startedAt?: string
  finishedAt?: string
}

export interface AsyncTaskPageQuery {
  pageNo?: number
  pageSize?: number
  tenantId?: string
  taskType?: string
  taskName?: string
  bizType?: string
  bizId?: string
  taskStatus?: string
  requestParam?: string
  resultData?: string
  errorMessage?: string
  retryCount?: number
  maxRetryCount?: number
  startedAt?: string
  finishedAt?: string
}

export interface AsyncTaskForm {
  id?: string
  tenantId?: string
  taskType?: string
  taskName?: string
  bizType?: string
  bizId?: string
  taskStatus?: string
  requestParam?: string
  resultData?: string
  errorMessage?: string
  retryCount?: number
  maxRetryCount?: number
  requeueCount?: number
  startedAt?: string
  finishedAt?: string
}
