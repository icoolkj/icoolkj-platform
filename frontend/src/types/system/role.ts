/** AUTO-GENERATED — 角色 类型 */

export interface RoleVO {
  id?: string
  roleName: string
  roleCode: string
  dataScope?: string
  sortNo?: number
  status?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

/** @deprecated use RoleVO */
export type SysRole = RoleVO

export interface RolePageQuery {
  pageNo?: number
  pageSize?: number
  tenantId?: string
  roleName?: string
  roleCode?: string
  status?: string
}

export interface RoleForm {
  id?: string
  roleName: string
  roleCode: string
  dataScope?: string
  sortNo?: number
  status?: string
  remark?: string
}
