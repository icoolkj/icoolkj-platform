import request from '@/utils/request'
import { defineReadOnlyGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'

export interface SysJobLogVO {
  id: string
  jobId: string
  jobCode: string
  triggerType: string
  triggerUserId?: string
  status: string
  jobMessage?: string
  exceptionInfo?: string
  requestParam?: string
  resultSummary?: string
  startedAt?: string
  finishedAt?: string
  createdAt?: string
}

export interface SysJobLogPageQuery {
  pageNo?: number
  pageSize?: number
  jobCode?: string
  status?: string
  triggerType?: string
}

const BASE = '/monitor/jobLog'

/** GenCrudPage-compatible page (axios envelope). */
export function pageJobLogs(params: SysJobLogPageQuery) {
  return request.get<ApiResponse<PageResult<SysJobLogVO>>>(`${BASE}/page`, { params })
}

export function getJobLog(id: string) {
  return request.get<ApiResponse<SysJobLogVO>>(`${BASE}/${encodeURIComponent(id)}`)
}

export const PERM_JobLog = {
  view: 'monitor:jobLog:view',
  list: 'monitor:jobLog:list',
}

export const GEN_RESOURCE_KEY = 'monitor.jobLog' as const

export const genCrudResource = defineReadOnlyGenCrudResource(PERM_JobLog, pageJobLogs, {
  get: getJobLog,
})
