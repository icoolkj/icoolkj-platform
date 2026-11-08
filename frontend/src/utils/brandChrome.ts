import { getToken } from '@/utils/request'
import { useBrandStore } from '@/stores/brand'
import { canonicalInfraDownloadUrl, toApiPath } from '@/utils/infraFileUrl'
import { shellConfig } from '../../shell.config'

const FAVICON_LINK_ATTR = 'data-ic-brand-favicon'
let faviconObjectUrl = ''

function ensureFaviconLink(): HTMLLinkElement {
  let link = document.querySelector<HTMLLinkElement>(`link[${FAVICON_LINK_ATTR}]`)
  if (!link) {
    link = document.createElement('link')
    link.rel = 'icon'
    link.setAttribute(FAVICON_LINK_ATTR, '1')
    document.head.appendChild(link)
  }
  return link
}

function revokeFaviconObjectUrl() {
  if (faviconObjectUrl) {
    URL.revokeObjectURL(faviconObjectUrl)
    faviconObjectUrl = ''
  }
}

/** 浏览器标签标题跟随 OEM 产品名 */
export function applyBrandDocumentTitle(locale?: string | null) {
  const brandStore = useBrandStore()
  const title = brandStore.productNameForLocale(locale) || shellConfig.defaultTitle
  if (title) {
    document.title = title
  }
}

/**
 * 设置 Favicon。
 * - publicUrl：匿名公开地址（/auth/login-brand/asset?catalogCode=&profileCode=）
 * - fileId：登录后走鉴权下载
 * - 皆空：移除自定义 icon
 */
export async function applyBrandFavicon(options?: {
  publicUrl?: string | null
  fileId?: string | null
}) {
  const link = ensureFaviconLink()
  revokeFaviconObjectUrl()

  const publicUrl = options?.publicUrl?.trim()
  if (publicUrl) {
    link.href = publicUrl.startsWith('/api/') || /^https?:\/\//i.test(publicUrl)
      ? publicUrl
      : toApiPath(publicUrl)
    return
  }

  const fileId = options?.fileId?.trim()
  const token = getToken()
  if (fileId && token) {
    try {
      const response = await fetch(toApiPath(canonicalInfraDownloadUrl(fileId)), {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (!response.ok) {
        link.remove()
        return
      }
      const blob = await response.blob()
      faviconObjectUrl = URL.createObjectURL(blob)
      link.href = faviconObjectUrl
      return
    } catch {
      link.remove()
      return
    }
  }

  // 未登录且无 publicUrl：不盲拉 favicon（未上传时必然 404，且与二维码无关）
  link.remove()
}

/** 标题 + Favicon（后台会话用 brandStore.chrome） */
export async function applyBrandChrome(locale?: string | null) {
  applyBrandDocumentTitle(locale)
  await applyBrandFavicon({ fileId: useBrandStore().faviconFileId() })
}
