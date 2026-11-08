import i18n, { LOCALE_STORAGE_KEY } from '@/i18n'
import { shellConfig } from '../../shell.config'
import { getToken } from '@/utils/request'

function t(key: string, fallback: string): string {
  const text = i18n.global.t(key)
  return text === key ? fallback : text
}

function resolveDownloadFileName(response: Response, fallback: string): string {
  const headerName =
    response.headers.get('X-Download-Filename') || response.headers.get('X-Export-Filename')
  if (headerName?.trim()) {
    return headerName.trim()
  }
  const disposition = response.headers.get('Content-Disposition') || ''
  const utf8 = /filename\*\s*=\s*UTF-8''([^;]+)/i.exec(disposition)
  if (utf8?.[1]) {
    try {
      return decodeURIComponent(utf8[1].trim().replace(/"/g, ''))
    } catch {
      // keep fallback
    }
  }
  const plain = /filename\s*=\s*"?([^";]+)"?/i.exec(disposition)
  if (plain?.[1]) {
    return plain[1].trim()
  }
  return fallback
}

export type DownloadOptions = {
  signal?: AbortSignal
}

async function downloadBlob(
  url: string,
  fileName: string,
  parseJsonError: boolean,
  options?: DownloadOptions,
) {
  if (options?.signal?.aborted) {
    throw new DOMException('Aborted', 'AbortError')
  }
  const response = await fetch(`/api${url}`, {
    headers: {
      Authorization: `Bearer ${getToken() ?? ''}`,
      'Accept-Language': localStorage.getItem(LOCALE_STORAGE_KEY) || shellConfig.defaultLang,
    },
    signal: options?.signal,
  })
  if (options?.signal?.aborted) {
    throw new DOMException('Aborted', 'AbortError')
  }
  if (!response.ok) {
    if (parseJsonError) {
      const contentType = response.headers.get('content-type') ?? ''
      if (contentType.includes('application/json')) {
        const body = (await response.json()) as { message?: string }
        throw new Error(body.message || t('common.downloadFailed', 'Download failed'))
      }
    }
    throw new Error(t('common.downloadFailed', 'Download failed'))
  }
  const blob = await response.blob()
  if (options?.signal?.aborted) {
    throw new DOMException('Aborted', 'AbortError')
  }
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = resolveDownloadFileName(response, fileName)
  link.click()
  URL.revokeObjectURL(link.href)
}

export async function downloadZip(url: string, fileName: string, options?: DownloadOptions) {
  await downloadBlob(url, fileName, false, options)
}

export async function downloadByApi(path: string, fileName: string, options?: DownloadOptions) {
  await downloadBlob(path, fileName, true, options)
}

export function isAbortError(e: unknown): boolean {
  return (
    (e instanceof DOMException && e.name === 'AbortError') ||
    (e instanceof Error && e.name === 'AbortError')
  )
}
