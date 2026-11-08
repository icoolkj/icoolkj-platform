export interface SiteMessagePageQuery {
  pageNo?: number
  pageSize?: number
  messageSubject?: string
  messageCategory?: string
  tenantId?: string
}

export interface SiteMessageVO {
  id?: string
  tenantId?: string
  messageSubject?: string
  messageCategory?: string
  messageContent?: string
  senderId?: string
  senderName?: string
  sentAt?: string
  recipientCount?: number
}

export interface SiteMessageSendPayload {
  messageSubject: string
  messageCategory: string
  messageContent: string
  targetTenantId?: string
  targetTenantIds?: string[]
  recipientType: 'user' | 'dept' | 'role' | 'all'
  userIds?: string[]
  deptIds?: string[]
  roleIds?: string[]
}

export interface SiteMessageInboxVO {
  id?: string
  messageId?: string
  messageSubject?: string
  messageCategory?: string
  messageContent?: string
  senderId?: string
  senderName?: string
  sentAt?: string
  readAt?: string
  readStatus?: string
}

export interface SiteMessageInboxPageQuery {
  pageNo?: number
  pageSize?: number
  messageCategory?: string
  readStatus?: string
}

export interface SiteMessageBatchDeletePayload {
  ids: string[]
}

export interface SiteMessageRecipientPageQuery {
  pageNo?: number
  pageSize?: number
  readStatus?: string
}

export interface SiteMessageRecipientStatusVO {
  id?: string
  userId?: string
  userNickname?: string
  username?: string
  readStatus?: string
  readAt?: string
}
