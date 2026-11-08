export interface UserProfileRole {
  id: string
  roleName: string
  roleCode: string
}

export interface UserProfilePost {
  id: string
  postName: string
  postCode: string
}

export interface UserAvatarPolicy {
  maxSizeMb: number
  outputSize: number
  thumbSmSize: number
  thumbMdSize: number
}

export interface UserProfile {
  userId: string
  username: string
  nickname: string
  realName?: string
  email?: string
  phone?: string
  gender: string
  avatarFileId?: string
  avatarUrl?: string
  avatarUrlSm?: string
  avatarUrlMd?: string
  avatarUrlLg?: string
  tenantId: string
  tenantCode: string
  tenantName?: string
  deptId?: string
  deptName?: string
  roles: UserProfileRole[]
  posts: UserProfilePost[]
  lastLoginIp?: string
  lastLoginAt?: string
  pwdUpdatedAt?: string
  mustChangePassword?: string
}

export interface UserProfileUpdatePayload {
  nickname: string
  realName?: string
  email?: string
  phone?: string
  gender: string
}

export interface ChangePasswordPayload {
  oldPassword: string
  newPassword: string
}
