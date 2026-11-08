import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  SiteMessageBatchDeletePayload,
  SiteMessagePageQuery,
  SiteMessageRecipientPageQuery,
  SiteMessageRecipientStatusVO,
  SiteMessageSendPayload,
  SiteMessageVO,
} from '@/types/system/siteMessage'

const BASE = '/system/siteMessage'

export function pageSiteMessage(params: SiteMessagePageQuery) {
  return request.get<ApiResponse<PageResult<SiteMessageVO>>>(`${BASE}/page`, { params })
}

export function getSiteMessage(id: string) {
  return request.get<ApiResponse<SiteMessageVO>>(`${BASE}/${id}`).then((res) => res.data.data)
}

export function pageSiteMessageRecipients(id: string, params: SiteMessageRecipientPageQuery) {
  return request.get<ApiResponse<PageResult<SiteMessageRecipientStatusVO>>>(
    `${BASE}/${id}/recipients/page`,
    {
      params,
    },
  )
}

export function sendSiteMessage(data: SiteMessageSendPayload) {
  return request.post<ApiResponse<string>>(`${BASE}/send`, data).then((res) => res.data.data)
}

export function deleteSiteMessage(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export function batchDeleteSiteMessage(data: SiteMessageBatchDeletePayload) {
  return request.delete<ApiResponse<void>>(`${BASE}/batch`, { data })
}

export const PERM_SiteMessage = {
  view: 'system:siteMessage:view',
  list: 'system:siteMessage:list',
  send: 'system:siteMessage:send',
  delete: 'system:siteMessage:delete',
}

export const GEN_RESOURCE_KEY = 'system.siteMessage' as const

export const genCrudResource = defineGenCrudResource(PERM_SiteMessage, {
  page: pageSiteMessage,
  delete: deleteSiteMessage,
  batchDelete: batchDeleteSiteMessage,
})
