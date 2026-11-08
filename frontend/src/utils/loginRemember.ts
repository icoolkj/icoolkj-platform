/** 仅当用户勾选「记住登录信息」时写入；默认登录页不落任何账号。 */

import { LOGIN_REMEMBER_KEY } from '@/constants/storage'

export { LOGIN_REMEMBER_KEY }

export interface LoginRememberPayload {
  tenantCode: string
  username: string
  password: string
}

export function loadLoginRemember(): LoginRememberPayload | null {
  try {
    const raw = localStorage.getItem(LOGIN_REMEMBER_KEY)
    if (!raw) {
      return null
    }
    const parsed = JSON.parse(raw) as Partial<LoginRememberPayload>
    const tenantCode = typeof parsed.tenantCode === 'string' ? parsed.tenantCode.trim() : ''
    const username = typeof parsed.username === 'string' ? parsed.username.trim() : ''
    const password = typeof parsed.password === 'string' ? parsed.password : ''
    if (!tenantCode && !username && !password) {
      return null
    }
    return { tenantCode, username, password }
  } catch {
    return null
  }
}

export function saveLoginRemember(payload: LoginRememberPayload) {
  localStorage.setItem(
    LOGIN_REMEMBER_KEY,
    JSON.stringify({
      tenantCode: payload.tenantCode.trim(),
      username: payload.username.trim(),
      password: payload.password,
    }),
  )
}

export function clearLoginRemember() {
  localStorage.removeItem(LOGIN_REMEMBER_KEY)
}
