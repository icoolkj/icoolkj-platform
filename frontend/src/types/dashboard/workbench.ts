export interface WorkbenchHeader {
  nickname?: string
  tenantId?: string
  tenantName?: string
  tenantCode?: string
  deptName?: string
  postLabel?: string
  platformOperator?: boolean
}

export interface WorkbenchStat {
  key: string
  value?: number
}

export interface WorkbenchLoginLogItem {
  username?: string
  ip?: string
  location?: string
  browser?: string
  os?: string
  loginAt?: string
}

export interface WorkbenchOperationLogItem {
  moduleName?: string
  businessType?: string
  operatorName?: string
  operationStatus?: string
  operationAt?: string
}

export interface WorkbenchVO {
  header?: WorkbenchHeader
  stats?: WorkbenchStat[]
  unreadSiteMessageCount?: number
  recentLoginLogs?: WorkbenchLoginLogItem[]
  recentOperationLogs?: WorkbenchOperationLogItem[]
}
