/** AUTO-GENERATED — 部门表 类型 */

export interface DeptVO {
  id?: string
  parentId?: string
  ancestors?: string
  deptName?: string
  deptCode?: string
  leaderName?: string
  phone?: string
  email?: string
  sortNo?: number
  status?: string
  remark?: string
}

export interface DeptPageQuery {
  pageNo?: number
  pageSize?: number
  tenantId?: string
  parentId?: string
  ancestors?: string
  deptName?: string
  deptCode?: string
  leaderName?: string
  phone?: string
  email?: string
  sortNo?: number
  status?: string
  remark?: string
}

export interface DeptForm {
  id?: string
  parentId?: string
  ancestors?: string
  deptName?: string
  deptCode?: string
  leaderName?: string
  phone?: string
  email?: string
  sortNo?: number
  status?: string
  remark?: string
}
