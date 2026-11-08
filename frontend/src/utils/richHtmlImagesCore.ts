import {
  isInfraFileUrl,
  isPublicFileUrl,
  normalizeInfraUrl,
  resolveNoticeContentDownloadPath,
  type FileDownloadScope,
} from '@/utils/infraFileUrl'

export function isPlaceholderHtml(html: string): boolean {
  const normalized = html.replace(/\s/g, '').toLowerCase()
  return !normalized || normalized === '<p><br></p>' || normalized === '<p></p>'
}

/** 实例级 blob 映射，避免多编辑器/详情组件互相 revoke */
export class RichHtmlBlobRegistry {
  private readonly blobToCanonical = new Map<string, string>()
  private readonly blobUrlRegistry = new Set<string>()

  remember(blobUrl: string, canonical: string) {
    this.blobUrlRegistry.add(blobUrl)
    this.blobToCanonical.set(blobUrl, canonical)
  }

  resolveBlob(blobUrl: string): string {
    return this.blobToCanonical.get(blobUrl) ?? ''
  }

  revokeAll() {
    this.blobUrlRegistry.forEach((url) => {
      URL.revokeObjectURL(url)
      this.blobToCanonical.delete(url)
    })
    this.blobUrlRegistry.clear()
  }
}

export function toRequestPath(url: string, scope: FileDownloadScope): string {
  const normalized = normalizeInfraUrl(url)
  const resolved = resolveNoticeContentDownloadPath(normalized, scope)
  if (resolved.startsWith('/api/')) return resolved.slice(4)
  return resolved
}

function guessVideoMime(url: string): string {
  const lower = url.toLowerCase()
  if (lower.includes('.webm')) return 'video/webm'
  if (lower.includes('.ogg') || lower.includes('.ogv')) return 'video/ogg'
  if (lower.includes('.mov')) return 'video/quicktime'
  return 'video/mp4'
}

function isVideoNode(node: Element) {
  const tag = node.tagName
  return tag === 'VIDEO' || tag === 'SOURCE'
}

function applyVideoPlayUrl(node: Element, playUrl: string, canonical: string) {
  node.setAttribute('src', playUrl)
  if (node.tagName === 'SOURCE') {
    if (!node.getAttribute('type')) {
      node.setAttribute('type', guessVideoMime(canonical))
    }
    const video = node.parentElement
    if (video?.tagName === 'VIDEO') {
      video.setAttribute('src', playUrl)
      video.setAttribute('controls', 'true')
      video.setAttribute('preload', 'metadata')
    }
  } else if (node.tagName === 'VIDEO') {
    node.setAttribute('controls', 'true')
    node.setAttribute('preload', 'metadata')
  }
}

export function canonicalizeRichHtmlForSave(html: string, registry: RichHtmlBlobRegistry): string {
  if (!html) return ''
  const doc = new DOMParser().parseFromString(html, 'text/html')
  doc.querySelectorAll('img[src], video[src], source[src]').forEach((node) => {
    const canonicalAttr = node.getAttribute('data-infra-canonical-src')
    const src = node.getAttribute('src') ?? ''
    let resolved = normalizeInfraUrl(canonicalAttr ?? '')
    if (!resolved && src.startsWith('blob:')) {
      resolved = normalizeInfraUrl(registry.resolveBlob(src))
    }
    if (!resolved) {
      resolved = normalizeInfraUrl(src)
    }
    if (resolved && (isInfraFileUrl(resolved) || isPublicFileUrl(resolved))) {
      node.setAttribute('src', resolved)
    } else if (src.startsWith('blob:')) {
      node.removeAttribute('src')
    }
    node.removeAttribute('data-infra-canonical-src')
    const dataHref = node.getAttribute('data-href')
    if (dataHref?.startsWith('blob:')) {
      node.removeAttribute('data-href')
    }
  })
  doc.querySelectorAll('a[href]').forEach((anchor) => {
    const href = anchor.getAttribute('href')
    if (href) anchor.setAttribute('href', normalizeInfraUrl(href))
  })
  return doc.body.innerHTML
}

export async function resolveRichHtmlImagesForDisplay(
  html: string,
  fetchBlob: (fileUrl: string) => Promise<string | null>,
  resolveDirectMediaUrl?: (fileUrl: string) => string | null,
): Promise<string> {
  if (!html) return ''
  const doc = new DOMParser().parseFromString(html, 'text/html')
  const mediaNodes = Array.from(
    doc.querySelectorAll('img[src], video[src], video source[src], source[src]'),
  )

  await Promise.all(
    mediaNodes.map(async (node) => {
      const rawSrc = node.getAttribute('src') ?? ''
      if (!rawSrc || rawSrc.startsWith('blob:') || isPublicFileUrl(rawSrc)) {
        return
      }
      const canonical = normalizeInfraUrl(rawSrc)
      if (isVideoNode(node) && resolveDirectMediaUrl) {
        const playUrl = resolveDirectMediaUrl(canonical)
        if (playUrl) {
          applyVideoPlayUrl(node, playUrl, canonical)
        }
        return
      }
      const blobUrl = await fetchBlob(canonical)
      if (!blobUrl) {
        node.setAttribute('src', canonical)
        return
      }
      node.setAttribute('data-infra-canonical-src', canonical)
      node.setAttribute('src', blobUrl)
    }),
  )

  return doc.body.innerHTML
}
