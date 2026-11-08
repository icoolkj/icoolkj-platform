/** 附件管理组件统一出口 */
export { default as AttachmentManager } from './AttachmentManager.vue'
export { default as CatalogAttachmentManager } from './CatalogAttachmentManager.vue'

export {
  ATTACHMENT_MODE_MULTIPLE,
  ATTACHMENT_MODE_SINGLE,
  ATTACHMENT_VARIANT_IMAGE,
  ATTACHMENT_VARIANT_LIST,
  FILE_BIZ_BANNER_IMAGE,
  FILE_BIZ_NOTICE_COVER,
  FILE_BIZ_NOTICE_RICHTEXT,
  FILE_BIZ_OA_INSTANCE,
  FILE_BIZ_USER_AVATAR,
  FILE_CATALOG_ATTACHMENT_DEMO,
  FILE_DOWNLOAD_SCOPE_INFRA,
  FILE_DOWNLOAD_SCOPE_NOTICE,
  FILE_REF_ATTACHMENT_DEMO,
  FILE_SCAN_CLEAN,
  FILE_SCAN_INFECTED,
  FILE_SCAN_PENDING,
  FILE_SCAN_SKIPPED,
  FILE_SENSITIVITY_CONFIDENTIAL,
  FILE_SENSITIVITY_INTERNAL,
  FILE_SENSITIVITY_PUBLIC,
  FILE_SENSITIVITY_SECRET,
  type AttachmentMode,
  type AttachmentVariant,
  type FileDownloadScope,
} from '@/constants/file'

export {
  normalizeAttachmentIds,
  resolveAttachmentConstraints,
  resolveAttachmentHint,
  uploadAttachmentFile,
  validateAttachmentFile,
} from '@/composables/useAttachmentUpload'

export {
  buildCatalogSlots,
  flattenCatalogFileIds,
  formatSlotCountReq,
  formatSlotSizeReq,
  formatSlotTypeReq,
  type CatalogSlot,
} from '@/utils/fileCatalogSlots'
