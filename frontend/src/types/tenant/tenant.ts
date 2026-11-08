/** AUTO-GENERATED — 租户管理 类型 */

export interface TenantVO {
  id?: string
  tenantCode?: string
  tenantName?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  licenseNo?: string
  expireAt?: string
  accountLimit?: number
  status?: string
  tenantType?: string
  parentTenantId?: string
  rootRegionId?: string
  rootRegionName?: string
  remark?: string
}

/** 租户开通回执（明文密码仅创建响应出现一次） */
export interface TenantProvisionVO {
  tenantId?: string
  tenantCode?: string
  tenantName?: string
  adminUsername?: string
  initialPassword?: string
  rootDeptCode?: string
  rootDeptName?: string
  mustChangePassword?: boolean
}

export interface TenantPageQuery {
  pageNo?: number
  pageSize?: number
  tenantCode?: string
  tenantName?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  licenseNo?: string
  expireAt?: string
  accountLimit?: number
  status?: string
  tenantType?: string
  parentTenantId?: string
  rootRegionId?: string
  rootRegionName?: string
  remark?: string
}

export interface TenantForm {
  id?: string
  tenantCode?: string
  tenantName?: string
  adminUsername?: string
  rootDeptCode?: string
  rootDeptName?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  licenseNo?: string
  expireAt?: string
  accountLimit?: number
  status?: string
  tenantType?: string
  parentTenantId?: string
  rootRegionId?: string
  rootRegionName?: string
  remark?: string
}
