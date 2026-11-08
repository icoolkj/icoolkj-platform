/** AUTO-GENERATED — 数据库备份 类型 */

export interface DatabaseBackupVO {
  id?: string
  fileName?: string
  sizeBytes?: number
  sizeText?: string
  modifiedAt?: string
  compressed?: boolean
}

export interface DatabaseBackupPageQuery {
  pageNo?: number
  pageSize?: number
  fileName?: string
}

export interface DatabaseBackupConfigVO {
  enabled?: boolean
  cron?: string
  outputDir?: string
  retentionDays?: number
  mysqldumpCommand?: string
  compress?: boolean
  mysqldumpFromYaml?: boolean
}

export interface DatabaseBackupConfigUpdateRequest {
  enabled: boolean
  cron: string
  outputDir: string
  retentionDays: number
  compress: boolean
}

export interface DatabaseBackupRunResultVO {
  success?: boolean
  fileName?: string
  sizeBytes?: number
  message?: string
}
