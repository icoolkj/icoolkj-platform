import request, { clearToken, setToken } from '@/utils/request'
import type { ApiResponse, LoginUser } from '@/types/common'

export interface LoginRequest {
  username: string
  password: string
  tenantCode: string
  captchaCode?: string
  captchaKey?: string
}

export interface LoginResponse {
  accessToken?: string | null
  refreshToken?: string | null
  tokenType?: string
  expireSeconds?: number
  refreshExpireSeconds?: number
  expiresIn?: number
  mfaRequired?: boolean
  mfaTicket?: string
  mustEnrollMfa?: boolean
}

export interface CaptchaChallenge {
  requireCaptcha: boolean
  captchaKey?: string | null
  imageBase64?: string | null
}

export interface MfaEnrollStart {
  otpAuthUri: string
  secretHint?: string
}

function applyTokens(payload: LoginResponse) {
  if (payload.accessToken) {
    setToken(payload.accessToken, payload.refreshToken)
  }
}

/** Password login; does not set token when MFA challenge is required. */
export async function login(data: LoginRequest): Promise<LoginResponse> {
  const { encryptPasswordForTransport } = await import('@/utils/passwordTransport')
  const payload = {
    ...data,
    password: await encryptPasswordForTransport(data.password),
  }
  const res = await request.post<ApiResponse<LoginResponse>>('/auth/login', payload)
  const body = res.data.data
  if (body.mfaRequired) {
    return body
  }
  applyTokens(body)
  return body
}

export async function verifyMfa(mfaTicket: string, code: string): Promise<LoginResponse> {
  const res = await request.post<ApiResponse<LoginResponse>>('/auth/mfa/verify', {
    mfaTicket,
    code,
  })
  const payload = res.data.data
  applyTokens(payload)
  return payload
}

export async function startMfaEnroll() {
  const res = await request.post<ApiResponse<MfaEnrollStart>>('/auth/mfa/enroll/start')
  return res.data.data
}

export async function confirmMfaEnroll(code: string) {
  await request.post<ApiResponse<void>>('/auth/mfa/enroll/confirm', { code })
}

export async function disableMfa(code: string) {
  await request.post<ApiResponse<void>>('/auth/mfa/disable', { code })
}

export async function fetchCaptcha(tenantCode: string) {
  const res = await request.get<ApiResponse<CaptchaChallenge>>('/auth/captcha', {
    params: { tenantCode },
  })
  return res.data.data
}

/** 登录页公开品牌；素材键均为 catalogCode → profileCode */
export interface LoginBrand {
  tenantCode?: string | null
  tenantName?: string | null
  tenantMatched?: boolean
  productName?: string | null
  productNameI18n?: Record<string, string> | null
  /** catalogCode → (profileCode → 公开 URL) */
  assets?: Record<string, Record<string, string>> | null
  /** catalogCode → (profileCode → 说明) */
  labels?: Record<string, Record<string, string>> | null
  /** catalogCode → 启用槽位 profileCode 有序表 */
  catalogs?: Record<string, string[]> | null
}

export async function fetchLoginBrand(tenantCode?: string) {
  const res = await request.get<ApiResponse<LoginBrand>>('/auth/login-brand', {
    params: tenantCode?.trim() ? { tenantCode: tenantCode.trim() } : {},
    silentError: true,
  })
  return res.data.data
}

export async function fetchMe() {
  const res = await request.get<ApiResponse<LoginUser>>('/auth/me')
  return res.data.data
}

export async function logout() {
  try {
    await request.post<ApiResponse<void>>('/auth/logout')
  } catch {
    // 会话可能已因改密等失效
  } finally {
    clearToken()
  }
}
