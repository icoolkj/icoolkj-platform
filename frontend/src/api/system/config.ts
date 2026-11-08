import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  ConfigBootstrap,
  ConfigForm,
  ConfigPageQuery,
  ConfigResolvedVO,
  ConfigVO,
} from '@/types/system/config'

const BASE = '/system/config'

export function getConfigBootstrap() {
  return request.get<ApiResponse<ConfigBootstrap>>(`${BASE}/bootstrap`)
}

/** Whitelisted tenant-resolved config (no system:config:list). */
export function getResolvedConfig(configKey: string) {
  return request.get<ApiResponse<ConfigResolvedVO>>(
    `${BASE}/resolved/${encodeURIComponent(configKey)}`,
  )
}

export function pageConfig(params: ConfigPageQuery) {
  return request.get<ApiResponse<PageResult<ConfigVO>>>(`${BASE}/page`, { params })
}

export function getConfig(id: string) {
  return request.get<ApiResponse<ConfigVO>>(`${BASE}/${id}`)
}

export function createConfig(data: ConfigForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateConfig(data: ConfigForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteConfig(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export function refreshConfigCache() {
  return request.post<ApiResponse<void>>(`${BASE}/refreshCache`)
}

export const PERM_Config = {
  view: 'system:config:view',
  list: 'system:config:list',
  add: 'system:config:add',
  update: 'system:config:update',
  delete: 'system:config:delete',
}

/** @deprecated use PERM_Config */
export const PERM_CONFIG = PERM_Config

export const GEN_RESOURCE_KEY = 'system.config' as const

export const genCrudResource = defineGenCrudResource(PERM_Config, {
  page: pageConfig,
  get: getConfig,
  create: createConfig,
  update: updateConfig,
  delete: deleteConfig,
})
