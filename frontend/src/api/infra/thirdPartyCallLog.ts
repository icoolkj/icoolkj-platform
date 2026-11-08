/** AUTO-GENERATED - 第三方调用日志 API (read-only) */
import request from '@/utils/request'
import { defineReadOnlyGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  ThirdpartyCallLogPageQuery,
  ThirdpartyCallLogVO,
} from '@/types/infra/thirdPartyCallLog'

const BASE = '/infra/thirdPartyCallLog'

export function pageThirdPartyCallLog(params: ThirdpartyCallLogPageQuery) {
  return request.get<ApiResponse<PageResult<ThirdpartyCallLogVO>>>(`${BASE}/page`, { params })
}

export function getThirdPartyCallLog(id: string) {
  return request.get<ApiResponse<ThirdpartyCallLogVO>>(`${BASE}/${id}`)
}

export const PERM_ThirdPartyCallLog = {
  view: 'infra:thirdPartyCallLog:view',
  list: 'infra:thirdPartyCallLog:list',
}

/** @deprecated use PERM_ThirdPartyCallLog */
export const PERM_THIRD_PARTY_CALL_LOG = PERM_ThirdPartyCallLog

export const GEN_RESOURCE_KEY = 'infra.thirdPartyCallLog' as const

export const genCrudResource = defineReadOnlyGenCrudResource(PERM_ThirdPartyCallLog, pageThirdPartyCallLog, {
  get: getThirdPartyCallLog,
})
