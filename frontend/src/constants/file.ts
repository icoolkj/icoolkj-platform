/**
 * 附件/文件域常量（对齐后端 FileScanStatusEnum / FileSensitivityEnum / 常见 bizType）。
 * 业务页优先用 AttachmentManager，勿散落写死码值。
 */
import { NO, YES } from '@/constants/flags'

export { YES, NO }

/** 扫描状态：待扫 */
export const FILE_SCAN_PENDING = '0'
/** 扫描状态：清洁 */
export const FILE_SCAN_CLEAN = '1'
/** 扫描状态：感染 */
export const FILE_SCAN_INFECTED = '2'
/** 扫描状态：跳过 */
export const FILE_SCAN_SKIPPED = '3'

/** 密级：公开 */
export const FILE_SENSITIVITY_PUBLIC = '0'
/** 密级：内部（默认） */
export const FILE_SENSITIVITY_INTERNAL = '1'
/** 密级：秘密 */
export const FILE_SENSITIVITY_SECRET = '2'
/** 密级：机密 */
export const FILE_SENSITIVITY_CONFIDENTIAL = '3'

/** 下载作用域：infra 鉴权下载 */
export const FILE_DOWNLOAD_SCOPE_INFRA = 'infra' as const
/** 下载作用域：公告内容文件 */
export const FILE_DOWNLOAD_SCOPE_NOTICE = 'notice' as const

export type FileDownloadScope = typeof FILE_DOWNLOAD_SCOPE_INFRA | typeof FILE_DOWNLOAD_SCOPE_NOTICE

/** 常见上传 bizType（与访问策略 / Profile 对齐；特殊场景可覆盖） */
export const FILE_BIZ_NOTICE_COVER = 'notice_cover'
export const FILE_BIZ_NOTICE_RICHTEXT = 'notice_richtext'
export const FILE_BIZ_BANNER_IMAGE = 'banner_image'
export const FILE_BIZ_OA_INSTANCE = 'oa_instance'
export const FILE_BIZ_USER_AVATAR = 'user-avatar'
/** 附件测试业务目录编码（view.json fileCatalogs / AttachmentDemoFileConstants） */
export const FILE_CATALOG_ATTACHMENT_DEMO = 'attachment_demo'
/** 附件测试业务 ref bizType */
export const FILE_REF_ATTACHMENT_DEMO = 'infra_attachment_demo'

/** 附件管理组件展示形态 */
export const ATTACHMENT_VARIANT_IMAGE = 'image' as const
export const ATTACHMENT_VARIANT_LIST = 'list' as const

export type AttachmentVariant =
  | typeof ATTACHMENT_VARIANT_IMAGE
  | typeof ATTACHMENT_VARIANT_LIST

/** 单文件 / 多文件 */
export const ATTACHMENT_MODE_SINGLE = 'single' as const
export const ATTACHMENT_MODE_MULTIPLE = 'multiple' as const

export type AttachmentMode = typeof ATTACHMENT_MODE_SINGLE | typeof ATTACHMENT_MODE_MULTIPLE

export function isFileEncrypted(value: string | null | undefined): boolean {
  return value === YES
}

export function isFileScanClean(value: string | null | undefined): boolean {
  return value === FILE_SCAN_CLEAN
}

export function isFileSensitivityAtLeast(
  value: string | null | undefined,
  min: string,
): boolean {
  const order = [
    FILE_SENSITIVITY_PUBLIC,
    FILE_SENSITIVITY_INTERNAL,
    FILE_SENSITIVITY_SECRET,
    FILE_SENSITIVITY_CONFIDENTIAL,
  ]
  const a = order.indexOf(value ?? '')
  const b = order.indexOf(min)
  if (a < 0 || b < 0) return false
  return a >= b
}
