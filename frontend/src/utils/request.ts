import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'
import { ElMessage } from 'element-plus'
import i18n, { LOCALE_STORAGE_KEY } from '@/i18n'
import type { ApiResponse } from '@/types/common'
import { ApiError } from '@/utils/apiError'
import { ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY } from '@/constants/storage'
import { shellConfig } from '../../shell.config'

declare module 'axios' {
  export interface AxiosRequestConfig {
    /** Skip global ElMessage on business/HTTP errors for this request. */
    silentError?: boolean
  }
}

/** @deprecated Prefer AxiosRequestConfig.silentError after module augmentation. */
export type SilentRequestConfig = AxiosRequestConfig

function t(key: string): string {
  const text = i18n.global.t(key)
  return typeof text === 'string' ? text : key
}

function isSilent(config?: AxiosRequestConfig | InternalAxiosRequestConfig | null) {
  return Boolean(config?.silentError)
}

/** ???? / ????MFA ????????403?????????? Toast */
function isExpectedAuthGateError(error: {
  response?: { status?: number; data?: { message?: string } }
}): boolean {
  if (error.response?.status !== 403) {
    return false
  }
  const message = error.response?.data?.message || ''
  if (!message) {
    return false
  }
  const mustChange = t('error.auth.must_change_password')
  const mustEnroll = t('error.auth.must_enroll_mfa')
  return message === mustChange || message === mustEnroll
}


const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 60000,
})

let refreshPromise: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
  if (!refreshToken) {
    return null
  }
  try {
    const res = await axios.post<ApiResponse<{ accessToken?: string; refreshToken?: string }>>(
      '/api/auth/refresh',
      { refreshToken },
      {
        headers: {
          'Accept-Language': localStorage.getItem(LOCALE_STORAGE_KEY) || shellConfig.defaultLang,
        },
      },
    )
    const payload = res.data?.data
    if (res.data?.code !== 0 || !payload?.accessToken) {
      return null
    }
    setToken(payload.accessToken, payload.refreshToken)
    return payload.accessToken
  } catch {
    return null
  }
}

function singleFlightRefresh(): Promise<string | null> {
  if (!refreshPromise) {
    refreshPromise = refreshAccessToken().finally(() => {
      refreshPromise = null
    })
  }
  return refreshPromise
}

service.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem(ACCESS_TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  const locale = localStorage.getItem(LOCALE_STORAGE_KEY) || shellConfig.defaultLang
  config.headers['Accept-Language'] = locale
  return config
})

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<unknown>>) => {
    const responseType = response.config.responseType
    if (responseType === 'blob' || responseType === 'arraybuffer') {
      return response
    }
    const payload = response.data
    if (payload.code !== 0) {
      const message = payload.message || t('common.requestFailed')
      // Callers may fall back (e.g. Excel async ??sync); avoid double toast.
      const skipToast =
        isSilent(response.config) || payload.messageKey === 'error.async.disabled'
      if (!skipToast) {
        ElMessage.error(message)
      }
      return Promise.reject(
        new ApiError(message, {
          code: payload.code,
          messageKey: payload.messageKey,
        }),
      )
    }
    return response
  },
  async (error) => {
    const config = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
    const isLogoutRequest = config?.url?.includes('/auth/logout')
    const isRefreshRequest = config?.url?.includes('/auth/refresh')
    if (error.response?.status === 401 && !isLogoutRequest && !isRefreshRequest && config && !config._retry) {
      config._retry = true
      const newToken = await singleFlightRefresh()
      if (newToken) {
        config.headers.Authorization = `Bearer ${newToken}`
        return service.request(config)
      }
      clearToken()
      window.location.href = '/login'
      return Promise.reject(error)
    }
    if (!isLogoutRequest && !isSilent(config) && !isExpectedAuthGateError(error)) {
      const message = error.response?.data?.message || error.message || t('common.networkError')
      ElMessage.error(message)
    }
    if (error.response?.status === 401 && !isLogoutRequest) {
      clearToken()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export function setToken(token: string, refreshToken?: string | null) {
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  }
}

export function clearToken() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

export function getToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export default service
