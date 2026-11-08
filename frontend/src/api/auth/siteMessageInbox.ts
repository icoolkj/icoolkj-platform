import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type { SiteMessageInboxPageQuery, SiteMessageInboxVO } from '@/types/system/siteMessage'

const BASE = '/auth/siteMessage'

export function countUnreadSiteMessage() {
  return request.get<ApiResponse<number>>(`${BASE}/unread-count`)
}

export function pageInboxSiteMessage(params: SiteMessageInboxPageQuery) {
  return request.get<ApiResponse<PageResult<SiteMessageInboxVO>>>(`${BASE}/page`, { params })
}

export function getInboxSiteMessage(recipientId: string) {
  return request
    .get<ApiResponse<SiteMessageInboxVO>>(`${BASE}/${recipientId}`)
    .then((res) => res.data.data)
}

export function markInboxSiteMessageRead(recipientId: string) {
  return request.put<ApiResponse<void>>(`${BASE}/${recipientId}/read`)
}
