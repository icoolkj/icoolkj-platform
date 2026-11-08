/** 通知公告 */

import type { WorkflowInstanceDetailVO } from '@/types/workflow/instance'

export interface NoticeListVO {
  id?: string
  noticeTitle?: string
  noticeType?: string
  coverFileId?: string
  coverPreviewUrl?: string
  noticeSummary?: string
  sortNo?: number
  publishLocations?: string
  status?: string
  approveStatus?: string
  releaseStatus?: string
  processInstanceId?: string
  remark?: string
  createdAt?: string
  /** 一旦存在任意审批周期，禁删仅可关闭 */
  hasApprovalHistory?: boolean
  /** 版本快照 allowWithdraw；非 running 为 false */
  allowWithdraw?: boolean
}

export interface NoticeVO extends NoticeListVO {
  noticeContent?: string
  starterUserId?: string
  submittedAt?: string
  finishedAt?: string
  publishedAt?: string
}

export interface NoticePublishedVO {
  id?: string
  noticeTitle?: string
  noticeType?: string
  coverFileId?: string
  coverPreviewUrl?: string
  noticeSummary?: string
  noticeContent?: string
  sortNo?: number
  createdAt?: string
}

export interface NoticePageQuery {
  pageNo?: number
  pageSize?: number
  noticeTitle?: string
  noticeType?: string
  publishLocation?: string
  approveStatus?: string
  releaseStatus?: string
  status?: string
}

export interface NoticePublishedQuery {
  publishLocation?: string
  limit?: number
}

export interface NoticeForm {
  id?: string
  noticeTitle?: string
  noticeType?: string
  coverFileId?: string
  coverPreviewUrl?: string
  noticeContent?: string
  noticeSummary?: string
  sortNo?: number
  publishLocations?: string
  status?: string
  remark?: string
}

export interface NoticeBatchDeletePayload {
  ids: string[]
}

export interface NoticeApproveDetailVO {
  notice?: NoticeVO
  taskId?: string
  processInstanceId?: string
  approveStatus?: string
  instance?: WorkflowInstanceDetailVO
}
