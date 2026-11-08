import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import { deleteOaInstance, PERM_OaInstance } from '@/api/oa/oaInstance'
import type { GenCrudPerm } from '@/types/gen/crudApi'
import type { ApiResponse, PageResult } from '@/types/common'
import type { OaInitiateCatalogItemVO } from '@/types/oa/oaInitiate'
import type { OaInstanceListVO, OaMyInstancePageQuery } from '@/types/oa/oaOffice'

const BASE = '/oa/office'

export function listOfficeCatalog() {
  return request.get<ApiResponse<OaInitiateCatalogItemVO[]>>(`${BASE}/catalog`)
}

export function pageOfficeDrafts(params: OaMyInstancePageQuery) {
  return request.get<ApiResponse<PageResult<OaInstanceListVO>>>(`${BASE}/my/drafts`, { params })
}

export const PERM_OfficeApply = {
  view: 'oa:office:apply:view',
  list: 'oa:office:apply:list',
}

export const PERM_OfficeDraft = {
  view: 'oa:office:draft:view',
  list: 'oa:office:draft:list',
}

export const PERM_OfficeTodo = {
  view: 'oa:office:todo:view',
}

export const PERM_OfficeDone = {
  view: 'oa:office:done:view',
}

export const PERM_OfficeAi = {
  view: 'oa:office:ai:view',
  chat: 'oa:office:ai:chat',
}

const PERM_OfficeDraftCrud: GenCrudPerm = {
  query: PERM_OfficeDraft.list,
  update: PERM_OaInstance.update,
  submit: PERM_OaInstance.submit,
  delete: PERM_OaInstance.delete,
  close: PERM_OaInstance.close,
}

export const GEN_RESOURCE_KEY = 'oa.officeDraft' as const

export const genCrudResource = defineGenCrudResource(PERM_OfficeDraftCrud, {
  page: pageOfficeDrafts,
  delete: deleteOaInstance,
})
