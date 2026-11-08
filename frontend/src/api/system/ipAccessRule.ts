/** AUTO-GENERATED — IP访问规则 API */
import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  IpAccessRuleForm,
  IpAccessRulePageQuery,
  IpAccessRuleVO,
} from '@/types/system/ipAccessRule'

const BASE = '/system/ipAccessRule'

export function pageIpAccessRule(params: IpAccessRulePageQuery) {
  return request.get<ApiResponse<PageResult<IpAccessRuleVO>>>(`${BASE}/page`, { params })
}

export function getIpAccessRule(id: string) {
  return request.get<ApiResponse<IpAccessRuleVO>>(`${BASE}/${id}`)
}

export function createIpAccessRule(data: IpAccessRuleForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, data)
}

export function updateIpAccessRule(data: IpAccessRuleForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function deleteIpAccessRule(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_IpAccessRule = {
  view: 'system:ipAccessRule:view',
  list: 'system:ipAccessRule:list',
  add: 'system:ipAccessRule:add',
  update: 'system:ipAccessRule:update',
  delete: 'system:ipAccessRule:delete',
}

/** @deprecated use PERM_IpAccessRule */
export const PERM_IP_ACCESS_RULE = PERM_IpAccessRule

export const GEN_RESOURCE_KEY = 'system.ipAccessRule' as const

export const genCrudResource = defineGenCrudResource(PERM_IpAccessRule, {
  page: pageIpAccessRule,
  get: getIpAccessRule,
  create: createIpAccessRule,
  update: updateIpAccessRule,
  delete: deleteIpAccessRule,
})
