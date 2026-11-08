import request from '@/utils/request'
import { defineGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type { MenuForm, MenuPageQuery, MenuRouter, MenuVO } from '@/types/system/menu'

const BASE = '/system/menu'

function toPayload(data: MenuForm): MenuForm {
  const nameI18n: Record<string, string> = { ...(data.nameI18n ?? {}) }
  if (data.menuName) {
    nameI18n['zh-CN'] = data.menuName
  }
  if (data.menuNameEn) {
    nameI18n['en-US'] = data.menuNameEn
  } else {
    delete nameI18n['en-US']
  }
  const { menuNameEn: _ignored, ...rest } = data
  return { ...rest, nameI18n }
}

function fromDetail(vo: MenuVO): MenuVO {
  return {
    ...vo,
    menuNameEn: vo.nameI18n?.['en-US'] ?? vo.menuNameEn,
  }
}

export function getMenuRouters(productCode?: string) {
  return request.get<ApiResponse<MenuRouter[]>>(`${BASE}/routers`, {
    params: productCode ? { productCode } : undefined,
  })
}

export function pageMenu(params: MenuPageQuery) {
  return request.get<ApiResponse<PageResult<MenuVO>>>(`${BASE}/page`, { params })
}

export function getMenu(id: string) {
  return request.get<ApiResponse<MenuVO>>(`${BASE}/${id}`).then((res) => {
    if (res.data?.data) {
      res.data.data = fromDetail(res.data.data)
    }
    return res
  })
}

export function createMenu(data: MenuForm) {
  return request.post<ApiResponse<string>>(`${BASE}/create`, toPayload(data))
}

export function updateMenu(data: MenuForm) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, toPayload(data))
}

export function deleteMenu(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${id}`)
}

export const PERM_Menu = {
  view: 'system:menu:view',
  list: 'system:menu:list',
  add: 'system:menu:add',
  update: 'system:menu:update',
  delete: 'system:menu:delete',
}

/** @deprecated use PERM_Menu */
export const PERM_MENU = PERM_Menu

export const GEN_RESOURCE_KEY = 'system.menu' as const

export const genCrudResource = defineGenCrudResource(PERM_Menu, {
  page: pageMenu,
  get: getMenu,
  create: createMenu,
  update: updateMenu,
  delete: deleteMenu,
})
