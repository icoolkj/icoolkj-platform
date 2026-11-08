/** 文件表类型 */

export interface FileVO {
  id?: string
  tenantId?: string
  bizType?: string
  bizId?: string
  originalName?: string
  fileName?: string
  fileExt?: string
  contentType?: string
  fileSize?: number
  fileSizeDisplay?: string
  storageType?: string
  bucketName?: string
  objectKey?: string
  fileUrl?: string
  md5?: string
  /** 扫描状态：见 constants/file FILE_SCAN_* */
  scanStatus?: string
  scanEngine?: string
  scanAt?: string
  /** 应用层加密：YES/NO */
  encrypted?: string
  /** 密级：见 constants/file FILE_SENSITIVITY_* */
  sensitivityLevel?: string
  /** 归属用户（上传人） */
  userId?: string
  createdAt?: string
  remark?: string
  referenceCount?: number
  deletable?: boolean
  previewable?: boolean
  /** Whether current user may download (backend FileAccessGuard) */
  downloadable?: boolean
}

export interface FilePageQuery {
  pageNo?: number
  pageSize?: number
  originalName?: string
  storageType?: string
  bizType?: string
}

export interface FileStorageOptionsVO {
  defaultProvider?: string
  enabledProviders?: string[]
  maxFileSizeMb?: number
}

export interface FilePreviewUrlVO {
  url?: string
  external?: boolean
}

export interface FileRefVO {
  id?: string
  fileId?: string
  bizType?: string
  bizId?: string
  refField?: string
  createdBy?: string
  createdAt?: string
}

export interface FileRefCreateRequest {
  bizType: string
  bizId: string
  refField?: string
}

export interface FileBatchDeleteRequest {
  ids: string[]
}

export interface FileForm {
  id?: string
  tenantId?: string
  bizType?: string
  bizId?: string
  originalName?: string
  fileName?: string
  fileExt?: string
  contentType?: string
  fileSize?: number
  storageType?: string
  bucketName?: string
  objectKey?: string
  fileUrl?: string
  md5?: string
  /** 归属用户（上传人） */
  userId?: string
  createdAt?: string
  remark?: string
}
