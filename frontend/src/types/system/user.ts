/** AUTO-GENERATED — 用户表 类型 */

export interface UserVO {
  id?: string
  tenantId?: string
  deptId?: string
  deptName?: string
  username?: string
  nickname?: string
  realName?: string
  userType?: string
  email?: string
  phone?: string
  gender?: string
  avatarUrl?: string
  avatarFileId?: string
  avatarUrlSm?: string
  status?: string
  isSuperAdmin?: string
  loginFailCount?: number
  lockedUntil?: string
  lastLoginIp?: string
  lastLoginAt?: string
  pwdUpdatedAt?: string
  mustChangePassword?: string
  remark?: string
}

export interface UserPageQuery {
  pageNo?: number
  pageSize?: number
  tenantId?: string
  deptId?: string
  deptSubtreeRootId?: string
  username?: string
  nickname?: string
  realName?: string
  userType?: string
  email?: string
  phone?: string
  gender?: string
  avatarUrl?: string
  avatarFileId?: string
  avatarUrlSm?: string
  status?: string
  isSuperAdmin?: string
  loginFailCount?: number
  lockedUntil?: string
  lastLoginIp?: string
  lastLoginAt?: string
  pwdUpdatedAt?: string
  mustChangePassword?: string
  remark?: string
}

export interface UserForm {
  id?: string
  deptId?: string
  username?: string
  nickname?: string
  realName?: string
  password?: string
  email?: string
  phone?: string
  gender?: string
  avatarUrl?: string
  avatarFileId?: string
  avatarUrlSm?: string
  status?: string
  remark?: string
}
