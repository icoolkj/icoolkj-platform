import {
  FILE_DOWNLOAD_SCOPE_INFRA,
  FILE_DOWNLOAD_SCOPE_NOTICE,
  type FileDownloadScope,
} from '@/constants/file'

export type { FileDownloadScope }
export { FILE_DOWNLOAD_SCOPE_INFRA, FILE_DOWNLOAD_SCOPE_NOTICE }

/** infra 文件下载路径（入库/持久化用，不含 token） */
export function canonicalInfraDownloadUrl(fileId: string, inline = true): string {
  return `/infra/file/${fileId}/download${inline ? '?inline=1' : ''}`
}

/** 公告正文/封面鉴权下载（仅需 system:notice:list） */
export function noticeContentFileDownloadUrl(fileId: string, inline = true): string {
  return `/system/notice/content-file/${fileId}/download${inline ? '?inline=1' : ''}`
}

const INFRA_FILE_ID_PATTERN = /\/infra\/file\/(\d{10,25})\/download/

export function extractInfraFileId(url: string): string | null {
  const normalized = normalizeInfraUrl(url)
  const match = normalized.match(INFRA_FILE_ID_PATTERN)
  return match?.[1] ?? null
}

export function isPublicFileUrl(url: string): boolean {
  return /^https?:\/\//i.test(url)
}

export function isInfraFileUrl(url: string): boolean {
  return INFRA_FILE_ID_PATTERN.test(url)
}

export function toApiPath(url: string): string {
  if (url.startsWith('/api/')) return url
  if (url.startsWith('/')) return `/api${url}`
  return url
}

/** 供 video/audio 等原生媒体标签播放：附带 access_token（仅展示用，不入库） */
export function appendAccessTokenToApiPath(apiPath: string, token?: string | null): string {
  if (!token) return apiPath
  const sep = apiPath.includes('?') ? '&' : '?'
  return `${apiPath}${sep}access_token=${encodeURIComponent(token)}`
}

/** 按 scope 将 infra 路径转为实际下载 API 路径（仍不含 token） */
export function resolveNoticeContentDownloadPath(url: string, scope: FileDownloadScope): string {
  if (!url || isPublicFileUrl(url)) return url
  if (scope !== 'notice' || !isInfraFileUrl(url)) return url
  const fileId = extractInfraFileId(url)
  if (!fileId) return url
  const inline = url.includes('inline=1')
  return noticeContentFileDownloadUrl(fileId, inline)
}

/** 入库前统一为不含 token、不含 /api 前缀的 infra 路径 */
export function normalizeInfraUrl(url: string): string {
  if (!url) return url
  let normalized = stripAccessTokenFromUrl(url.trim())
  if (normalized.startsWith('/api/infra/file/')) {
    normalized = normalized.slice(4)
  }
  if (normalized.startsWith('/api/system/notice/content-file/')) {
    normalized = normalized.slice(4)
  }
  const noticeMatch = normalized.match(/\/system\/notice\/content-file\/(\d{10,25})\/download/)
  if (noticeMatch) {
    return canonicalInfraDownloadUrl(noticeMatch[1], normalized.includes('inline=1'))
  }
  return normalized
}

export function stripAccessTokenFromUrl(url: string): string {
  if (!url || !url.includes('access_token=')) return url
  try {
    const parsed = new URL(url, window.location.origin)
    parsed.searchParams.delete('access_token')
    const search = parsed.searchParams.toString()
    const query = search ? `?${search}` : ''
    if (url.startsWith('/') || parsed.origin === window.location.origin) {
      return `${parsed.pathname}${query}${parsed.hash}`
    }
    return `${parsed.origin}${parsed.pathname}${query}${parsed.hash}`
  } catch {
    return url.replace(/([?&])access_token=[^&"'\\s<>]+/g, '$1').replace(/[?&]$/, '')
  }
}

/** 清理历史数据中 HTML 里的 access_token */
export function stripAccessTokenFromHtml(html: string): string {
  if (!html) return ''
  const doc = new DOMParser().parseFromString(html, 'text/html')
  doc.querySelectorAll('img[src], a[href]').forEach((el) => {
    const attr = el.hasAttribute('src') ? 'src' : 'href'
    const value = el.getAttribute(attr)
    if (value) el.setAttribute(attr, normalizeInfraUrl(value))
  })
  return doc.body.innerHTML
}
