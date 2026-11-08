import { onUnmounted, ref, watch, type Ref } from 'vue'
import { getToken } from '@/utils/request'
import {
  isPublicFileUrl,
  resolveNoticeContentDownloadPath,
  toApiPath,
  type FileDownloadScope,
} from '@/utils/infraFileUrl'

function resolveFetchPath(url: string, scope: FileDownloadScope): string {
  if (isPublicFileUrl(url)) return url
  const scoped = resolveNoticeContentDownloadPath(url, scope)
  return toApiPath(scoped)
}

/** 将 MinIO 外链或需鉴权的 API 路径转为可在 img 中使用的 URL */
export function useAuthImageUrl(
  source: Ref<string | undefined>,
  scope: FileDownloadScope = 'infra',
) {
  const displayUrl = ref('')
  let objectUrl = ''

  function revoke() {
    if (objectUrl) {
      URL.revokeObjectURL(objectUrl)
      objectUrl = ''
    }
  }

  watch(
    source,
    async (url) => {
      revoke()
      displayUrl.value = ''
      if (!url) {
        return
      }
      if (isPublicFileUrl(url)) {
        displayUrl.value = url
        return
      }
      try {
        const response = await fetch(resolveFetchPath(url, scope), {
          headers: { Authorization: `Bearer ${getToken() ?? ''}` },
        })
        if (!response.ok) {
          return
        }
        const blob = await response.blob()
        objectUrl = URL.createObjectURL(blob)
        displayUrl.value = objectUrl
      } catch {
        // ignore fetch errors; avatar falls back to initials
      }
    },
    { immediate: true },
  )

  onUnmounted(revoke)

  return displayUrl
}
