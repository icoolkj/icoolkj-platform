/** 文件类型图标判定（扩展名 / contentType；不拉真实预览） */

export type FileThumbKind = 'image' | 'pdf' | 'video' | 'other'

const IMAGE_EXTS = new Set(['.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp', '.svg'])
const VIDEO_EXTS = new Set(['.mp4', '.webm', '.ogg', '.ogv', '.mov'])

function normalizeExt(ext?: string | null): string {
  const raw = (ext ?? '').trim().toLowerCase()
  if (!raw) return ''
  return raw.startsWith('.') ? raw : `.${raw}`
}

export function resolveFileThumbKind(options: {
  fileExt?: string | null
  contentType?: string | null
}): FileThumbKind {
  const ext = normalizeExt(options.fileExt)
  const ct = (options.contentType ?? '').trim().toLowerCase()

  if (ext === '.pdf' || ct === 'application/pdf') {
    return 'pdf'
  }
  if (VIDEO_EXTS.has(ext) || ct.startsWith('video/')) {
    return 'video'
  }
  if (IMAGE_EXTS.has(ext) || ct.startsWith('image/')) {
    return 'image'
  }
  return 'other'
}
