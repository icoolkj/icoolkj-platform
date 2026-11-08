/** AUTO-GENERATED — 登录日志表 类型 */

export interface LoginLogVO {
  id?: string
  username?: string
  loginType?: string
  loginStatus?: string
  message?: string
  ip?: string
  location?: string
  userAgent?: string
  browser?: string
  os?: string
  loginAt?: string
}

export interface LoginLogPageQuery {
  pageNo?: number
  pageSize?: number
  username?: string
  loginType?: string
  loginStatus?: string
  message?: string
  ip?: string
  location?: string
  userAgent?: string
  browser?: string
  os?: string
  loginAt?: string
}

export interface LoginLogForm {
  id?: string
  username?: string
  loginType?: string
  loginStatus?: string
  message?: string
  ip?: string
  location?: string
  userAgent?: string
  browser?: string
  os?: string
  loginAt?: string
}
