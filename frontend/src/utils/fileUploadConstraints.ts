/** 从文件槽位 Profile 规则拼装 accept / 校验 / 提示（配置驱动，页面不写死类型与大小） */
import type { FileProfileVO } from '@/types/infra/fileProfile'

export type FileUploadConstraints = {
  accept: string
  allowedExts: string[]
  allowedContentTypes: string[]
  required: boolean
  minNum?: number
  maxNum?: number
  minSizeBytes?: number
  maxSizeBytes?: number
}

export function formatByteSize(bytes: number): string {
  if (!Number.isFinite(bytes) || bytes <= 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let n = bytes
  let i = 0
  while (n >= 1024 && i < units.length - 1) {
    n /= 1024
    i += 1
  }
  const digits = i === 0 ? 0 : n >= 10 ? 0 : 1
  return `${n.toFixed(digits)} ${units[i]}`
}

/** 浏览器 input accept：优先 content-type，否则 .ext */
export function acceptFromProfile(profile?: FileProfileVO | null): string {
  if (!profile) return ''
  const types = profile.allowedContentTypes?.filter(Boolean)
  if (types?.length) return types.join(',')
  const exts = profile.allowedExts?.filter(Boolean)
  if (exts?.length) {
    return exts.map((e) => (e.startsWith('.') ? e : `.${e}`)).join(',')
  }
  return ''
}

export function constraintsFromProfile(profile?: FileProfileVO | null): FileUploadConstraints {
  return {
    accept: acceptFromProfile(profile),
    allowedExts: (profile?.allowedExts || []).map((e) => e.replace(/^\./, '').toLowerCase()).filter(Boolean),
    allowedContentTypes: (profile?.allowedContentTypes || []).map((t) => t.toLowerCase()).filter(Boolean),
    required: !!profile?.required,
    minNum: profile?.minNum,
    maxNum: profile?.maxNum,
    minSizeBytes: profile?.minSizeBytes,
    maxSizeBytes: profile?.maxSizeBytes,
  }
}

type HintTranslator = (key: string, params?: Record<string, string | number>) => string

/** 用途说明（profile.remark）+ 规则约束文案（类型/大小/必传/数量） */
export function buildProfileUploadHint(
  profile: FileProfileVO | null | undefined,
  t: HintTranslator,
): string {
  const parts: string[] = []
  const remark = profile?.remark?.trim()
  if (remark) parts.push(remark)

  const c = constraintsFromProfile(profile)
  if (c.allowedExts.length) {
    parts.push(t('fileUploadAllowExts', { exts: c.allowedExts.map((e) => e.toUpperCase()).join('/') }))
  }
  if (c.maxSizeBytes && c.maxSizeBytes > 0) {
    parts.push(t('fileUploadMaxSize', { size: formatByteSize(c.maxSizeBytes) }))
  }
  if (c.required) {
    parts.push(t('fileUploadRequired'))
  }
  if (c.maxNum != null && c.maxNum > 0) {
    parts.push(t('fileUploadMaxNum', { n: c.maxNum }))
  }
  return parts.join(t('fileUploadHintJoin'))
}

export function fileMatchesConstraints(file: File, c: FileUploadConstraints): boolean {
  if (!c.allowedExts.length && !c.allowedContentTypes.length) {
    return true
  }
  const name = file.name.toLowerCase()
  const ext = name.includes('.') ? name.slice(name.lastIndexOf('.') + 1) : ''
  if (c.allowedExts.length && ext && c.allowedExts.includes(ext)) {
    return true
  }
  const mime = (file.type || '').toLowerCase()
  if (mime && c.allowedContentTypes.some((t) => mime === t || mime.startsWith(`${t};`))) {
    return true
  }
  // 空 MIME（如部分 .ico）时仅靠扩展名
  if (!mime && c.allowedExts.length && ext && c.allowedExts.includes(ext)) {
    return true
  }
  return false
}

export function fileSizeWithinConstraints(file: File, c: FileUploadConstraints): boolean {
  if (c.maxSizeBytes && c.maxSizeBytes > 0 && file.size > c.maxSizeBytes) {
    return false
  }
  if (c.minSizeBytes && c.minSizeBytes > 0 && file.size < c.minSizeBytes) {
    return false
  }
  return true
}
