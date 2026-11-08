export interface AttachmentDemoVO {
  id: string
  title: string
  assets?: Record<string, string | string[]>
  status: string
  remark?: string
  createdAt?: string
}

export interface AttachmentDemoPageQuery {
  pageNo?: number
  pageSize?: number
  title?: string
  status?: string
}

export interface AttachmentDemoForm {
  id?: string
  title: string
  assets?: Record<string, string | string[]>
  status: string
  remark?: string
}

export interface AttachmentDemoStatusPayload {
  id: string
  status: string
}

export interface AttachmentDemoBatchDeletePayload {
  ids: string[]
}
