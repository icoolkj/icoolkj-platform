import { onScopeDispose } from 'vue'
import request from '@/utils/request'
import { getToken } from '@/utils/request'
import {
  appendAccessTokenToApiPath,
  isInfraFileUrl,
  isPublicFileUrl,
  normalizeInfraUrl,
  toApiPath,
  type FileDownloadScope,
} from '@/utils/infraFileUrl'
import { FILE_DOWNLOAD_SCOPE_NOTICE } from '@/constants/file'
import {
  RichHtmlBlobRegistry,
  canonicalizeRichHtmlForSave,
  resolveRichHtmlImagesForDisplay,
  toRequestPath,
} from '@/utils/richHtmlImagesCore'

export interface RichHtmlSessionOptions {
  downloadScope?: FileDownloadScope
}

/** 组件级富文本图片会话：独立 blob 生命周期 + 可配置下载通道 */
export function useRichHtmlSession(options: RichHtmlSessionOptions = {}) {
  const registry = new RichHtmlBlobRegistry()

  /** Read on each call so host can switch notice ↔ infra without recreating session. */
  function currentScope(): FileDownloadScope {
    return options.downloadScope ?? FILE_DOWNLOAD_SCOPE_NOTICE
  }

  async function fetchBlobUrl(fileUrl: string): Promise<string | null> {
    const canonical = normalizeInfraUrl(fileUrl)
    if (isPublicFileUrl(canonical)) return canonical
    if (!isInfraFileUrl(canonical)) return null
    try {
      const response = await request.get(toRequestPath(canonical, currentScope()), {
        responseType: 'blob',
        silentError: true,
      })
      const rawBlob = response.data as Blob
      if (!rawBlob || rawBlob.size === 0) return null
      const headerType = (response.headers?.['content-type'] as string | undefined)
        ?.split(';')[0]
        ?.trim()
      const mimeType = rawBlob.type || headerType || 'application/octet-stream'
      const blob = rawBlob.type ? rawBlob : new Blob([rawBlob], { type: mimeType })
      const blobUrl = URL.createObjectURL(blob)
      registry.remember(blobUrl, canonical)
      return blobUrl
    } catch {
      return null
    }
  }

  function resolveDirectMediaUrl(fileUrl: string): string | null {
    const canonical = normalizeInfraUrl(fileUrl)
    if (isPublicFileUrl(canonical)) return canonical
    if (!isInfraFileUrl(canonical)) return null
    const apiPath = toApiPath(toRequestPath(canonical, currentScope()))
    return appendAccessTokenToApiPath(apiPath, getToken())
  }

  function canonicalize(html: string): string {
    return canonicalizeRichHtmlForSave(html, registry)
  }

  async function resolveForDisplay(html: string): Promise<string> {
    return resolveRichHtmlImagesForDisplay(html, fetchBlobUrl, resolveDirectMediaUrl)
  }

  function revokeAll() {
    registry.revokeAll()
  }

  onScopeDispose(revokeAll)

  return {
    registry,
    fetchBlobUrl,
    resolveDirectMediaUrl,
    canonicalize,
    resolveForDisplay,
    revokeAll,
  }
}
