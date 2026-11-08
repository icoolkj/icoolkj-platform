/** AUTO-GENERATED — 数据库备份 API */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  DatabaseBackupConfigUpdateRequest,
  DatabaseBackupConfigVO,
  DatabaseBackupPageQuery,
  DatabaseBackupRunResultVO,
  DatabaseBackupVO,
} from '@/types/infra/databaseBackup'

const BASE = '/infra/databaseBackup'

export function pageDatabaseBackup(params: DatabaseBackupPageQuery) {
  return request.get<ApiResponse<PageResult<DatabaseBackupVO>>>(`${BASE}/page`, { params })
}

export function getDatabaseBackupConfig() {
  return request.get<ApiResponse<DatabaseBackupConfigVO>>(`${BASE}/config`)
}

export function updateDatabaseBackupConfig(data: DatabaseBackupConfigUpdateRequest) {
  return request.put<ApiResponse<void>>(`${BASE}/config`, data)
}

export function runDatabaseBackup() {
  return request.post<ApiResponse<DatabaseBackupRunResultVO>>(`${BASE}/run`)
}

export function deleteDatabaseBackup(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${encodeURIComponent(id)}`)
}

export const PERM_DatabaseBackup = {
  view: 'monitor:dbBackup:view',
  list: 'monitor:dbBackup:list',
  run: 'monitor:dbBackup:run',
  delete: 'monitor:dbBackup:delete',
  edit: 'monitor:dbBackup:edit',
}

export const GEN_RESOURCE_KEY = 'infra.databaseBackup' as const

export const genCrudResource = defineGenCrudResource(PERM_DatabaseBackup, {
  page: pageDatabaseBackup,
  delete: deleteDatabaseBackup,
})
