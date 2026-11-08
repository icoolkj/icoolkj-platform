import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'

export interface SysJobVO {
  id: string
  jobCode: string
  jobName: string
  jobGroup?: string
  cronExpression: string
  misfirePolicy?: string
  allowConcurrent?: string
  status: string
  remark?: string
  lastStatus?: string
  lastMessage?: string
  lastTriggerAt?: string
  lastStartedAt?: string
  lastFinishedAt?: string
  updatedAt?: string
}

export interface SysJobUpdateRequest {
  id: string
  cronExpression: string
  status: string
  allowConcurrent?: string
  misfirePolicy?: string
  remark?: string
}

export interface SysJobPageQuery {
  pageNo?: number
  pageSize?: number
  jobCode?: string
  jobName?: string
  status?: string
  jobGroup?: string
}

const BASE = '/monitor/job'

/** GenCrudPage-compatible page (axios envelope). */
export function pageJobs(params: SysJobPageQuery) {
  return request.get<ApiResponse<PageResult<SysJobVO>>>(`${BASE}/page`, { params })
}

export function getJob(id: string) {
  return request.get<ApiResponse<SysJobVO>>(`${BASE}/${encodeURIComponent(id)}`)
}

export function updateJob(data: SysJobUpdateRequest) {
  return request.put<ApiResponse<void>>(BASE, data)
}

export function changeJobStatus(id: string, status: string) {
  return request.put<ApiResponse<void>>(`${BASE}/${encodeURIComponent(id)}/status`, {}, {
    params: { status },
  })
}

export function runJob(jobCode: string) {
  return request.post<ApiResponse<void>>(`${BASE}/${encodeURIComponent(jobCode)}/run`)
}

/** @deprecated prefer pageJobs for GenCrud thin shells */
export function listJobs() {
  return request.get<ApiResponse<SysJobVO[]>>(`${BASE}/list`).then((res) => res.data.data)
}

export const PERM_Job = {
  view: 'monitor:job:view',
  list: 'monitor:job:list',
  update: 'monitor:job:update',
  run: 'monitor:job:run',
}

export const GEN_RESOURCE_KEY = 'monitor.managedJob' as const

const PERM_ManagedJob = {
  list: PERM_Job.list,
  update: PERM_Job.update,
  run: PERM_Job.run,
}

export const genCrudResource = defineGenCrudResource(PERM_ManagedJob, {
  page: pageJobs,
  get: getJob,
  update: updateJob,
})
