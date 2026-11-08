import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import { defineReadOnlyGenCrudResource } from '@/components/gen/registry/crud-resources/helpers'

export interface OnlineUserRow {
  sessionId: string
  userId: string
  username: string
  tenantId: string
  tenantCode: string
  createdAt?: string
}

export const PERM_Online = {
  view: 'monitor:online:view',
  list: 'monitor:online:list',
  forceLogout: 'monitor:online:forceLogout',
} as const

export async function listOnlineUsers() {
  const res = await request.get<ApiResponse<OnlineUserRow[]>>('/monitor/online/list')
  return res.data.data ?? []
}

export async function forceLogoutOnlineUser(sessionId: string) {
  await request.delete<ApiResponse<void>>(`/monitor/online/${encodeURIComponent(sessionId)}`)
}

export const GEN_RESOURCE_KEY = 'monitor.online' as const

/** 在线会话无后端分页：全量拉取后前端过滤 + 本地分页（保持原有交互）。 */
export const genCrudResource = defineReadOnlyGenCrudResource(PERM_Online, async (params) => {
  const rows = await listOnlineUsers()
  const u = String(params?.username ?? '').trim().toLowerCase()
  const tc = String(params?.tenantCode ?? '').trim().toLowerCase()
  const filtered = rows.filter((row) => {
    if (u && !(row.username || '').toLowerCase().includes(u)) return false
    if (tc && !(row.tenantCode || '').toLowerCase().includes(tc)) return false
    return true
  })
  const pageNo = Number(params?.pageNo ?? 1)
  const pageSize = Number(params?.pageSize ?? 10)
  const start = (pageNo - 1) * pageSize
  return {
    list: filtered.slice(start, start + pageSize),
    total: filtered.length,
    pageNo,
    pageSize,
  }
})
