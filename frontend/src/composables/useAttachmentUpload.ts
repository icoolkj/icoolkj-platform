/** 附件上传共享逻辑（AttachmentManager / 特殊场景复用） */
import { ElMessage } from 'element-plus'
import { getFileStorageOptions, uploadInfraFile } from '@/api/infra/file'
import type { FileVO } from '@/types/infra/file'
import {
  buildProfileUploadHint,
  constraintsFromProfile,
  fileMatchesConstraints,
  fileSizeWithinConstraints,
  formatByteSize,
  type FileUploadConstraints,
} from '@/utils/fileUploadConstraints'
import type { FileProfileVO } from '@/types/infra/fileProfile'
import { FILE_SENSITIVITY_INTERNAL } from '@/constants/file'

export type AttachmentUploadOptions = {
  file: File
  bizType: string
  /** 业务主键；与 bizType 同时存在时后端写 file_ref */
  bizId?: string
  refField?: string
  replaceFileId?: string
  sensitivityLevel?: string
  constraints?: FileUploadConstraints | null
  profile?: FileProfileVO | null
  /** 无槽位规则时，是否要求图片（封面场景） */
  imageOnlyFallback?: boolean
  t: (key: string, params?: Record<string, string | number>) => string
}

/** 统一把 v-model 值规范为 fileId[]（支持 JSON 数组字符串） */
export function normalizeAttachmentIds(raw: unknown): string[] {
  if (raw == null || raw === '') return []
  if (Array.isArray(raw)) {
    return raw.map((v) => String(v).trim()).filter(Boolean)
  }
  if (typeof raw === 'string') {
    const text = raw.trim()
    if (!text) return []
    if (text.startsWith('[')) {
      try {
        const parsed = JSON.parse(text) as unknown
        if (Array.isArray(parsed)) {
          return parsed.map((v) => String(v).trim()).filter(Boolean)
        }
      } catch {
        /* fall through */
      }
    }
    return [text]
  }
  return []
}

export function resolveAttachmentConstraints(options: {
  constraints?: FileUploadConstraints | null
  profile?: FileProfileVO | null
}): FileUploadConstraints {
  if (options.constraints) return options.constraints
  return constraintsFromProfile(options.profile)
}

export function resolveAttachmentHint(options: {
  hint?: string
  profile?: FileProfileVO | null
  t: (key: string, params?: Record<string, string | number>) => string
}): string {
  if (options.hint?.trim()) return options.hint.trim()
  if (options.profile) {
    return buildProfileUploadHint(options.profile, (key, params) =>
      options.t(`common.${key}`, params),
    )
  }
  return ''
}

/** 客户端预校验；通过返回 null，失败返回已提示 */
export function validateAttachmentFile(
  file: File,
  options: Pick<
    AttachmentUploadOptions,
    'constraints' | 'profile' | 'imageOnlyFallback' | 't'
  >,
): boolean {
  const c = resolveAttachmentConstraints(options)
  const hasRules = c.allowedExts.length > 0 || c.allowedContentTypes.length > 0
  if (hasRules) {
    if (!fileMatchesConstraints(file, c)) {
      ElMessage.warning(options.t('common.fileUploadTypeMismatch'))
      return false
    }
  } else if (options.imageOnlyFallback) {
    const name = file.name.toLowerCase()
    const looksImage =
      file.type.startsWith('image/') || /\.(png|jpe?g|gif|webp|ico|svg)$/.test(name)
    if (!looksImage) {
      ElMessage.warning(options.t('common.imageFileRequired'))
      return false
    }
  }
  if (!fileSizeWithinConstraints(file, c)) {
    if (c.maxSizeBytes && file.size > c.maxSizeBytes) {
      ElMessage.warning(
        options.t('common.fileUploadSizeTooLarge', { size: formatByteSize(c.maxSizeBytes) }),
      )
    } else if (c.minSizeBytes && file.size < c.minSizeBytes) {
      ElMessage.warning(
        options.t('common.fileUploadSizeTooSmall', { size: formatByteSize(c.minSizeBytes) }),
      )
    }
    return false
  }
  return true
}

/** 统一 multipart 上传（bizType / replace / sensitivity） */
export async function uploadAttachmentFile(
  options: AttachmentUploadOptions,
): Promise<FileVO> {
  if (!validateAttachmentFile(options.file, options)) {
    throw new Error('VALIDATION')
  }
  const optionsRes = await getFileStorageOptions()
  const provider = optionsRes.data.data.defaultProvider ?? 'minio'
  const formData = new FormData()
  formData.append('file', options.file)
  formData.append('storageType', provider)
  formData.append('bizType', options.bizType)
  if (options.bizId?.trim()) {
    formData.append('bizId', options.bizId.trim())
  }
  if (options.refField?.trim()) {
    formData.append('refField', options.refField.trim())
  }
  if (options.replaceFileId?.trim()) {
    formData.append('replaceFileId', options.replaceFileId.trim())
  }
  const sensitivity = options.sensitivityLevel?.trim() || FILE_SENSITIVITY_INTERNAL
  formData.append('sensitivityLevel', sensitivity)
  const res = await uploadInfraFile(formData)
  return res.data.data
}
