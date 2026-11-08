import request from '@/utils/request'
import { defineMasterDetailGenCrudResource } from '@/lib/gen-crud'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  DictItemForm,
  DictItemPageQuery,
  DictItemVO,
  DictOption,
  DictTypeForm,
  DictTypePageQuery,
  DictTypeVO,
} from '@/types/system/dict'

const TYPE_BASE = '/system/dictType'
const ITEM_BASE = '/system/dictItem'

export function pageDictType(params: DictTypePageQuery) {
  return request.get<ApiResponse<PageResult<DictTypeVO>>>(`${TYPE_BASE}/page`, { params })
}

export function getDictType(id: string) {
  return request.get<ApiResponse<DictTypeVO>>(`${TYPE_BASE}/${id}`)
}

export function createDictType(data: DictTypeForm) {
  return request.post<ApiResponse<string>>(`${TYPE_BASE}/create`, data)
}

export function updateDictType(data: DictTypeForm) {
  return request.put<ApiResponse<void>>(`${TYPE_BASE}/update`, data)
}

export function deleteDictType(id: string) {
  return request.delete<ApiResponse<void>>(`${TYPE_BASE}/${id}`)
}

export function pageDictItem(params: DictItemPageQuery) {
  return request.get<ApiResponse<PageResult<DictItemVO>>>(`${ITEM_BASE}/page`, { params })
}

export function getDictItem(id: string) {
  return request.get<ApiResponse<DictItemVO>>(`${ITEM_BASE}/${id}`)
}

export function createDictItem(data: DictItemForm) {
  return request.post<ApiResponse<string>>(`${ITEM_BASE}/create`, data)
}

export function updateDictItem(data: DictItemForm) {
  return request.put<ApiResponse<void>>(`${ITEM_BASE}/update`, data)
}

export function deleteDictItem(id: string) {
  return request.delete<ApiResponse<void>>(`${ITEM_BASE}/${id}`)
}

const DATA_BASE = '/system/dict/data'

/** Runtime: load dict options by type key (e.g. sys_common_status). */
export function getDictData(dictType: string) {
  return request.get<ApiResponse<DictOption[]>>(`${DATA_BASE}/${encodeURIComponent(dictType)}`)
}

/** Batch load dict options for generated CRUD pages. */
export function getDictDataBatch(types: string[]) {
  return request.get<ApiResponse<Record<string, DictOption[]>>>(DATA_BASE, {
    params: { types: types.join(',') },
  })
}

/**
 * Runtime: dict type list for codegen / form builders (no dict admin permission).
 * @param scope optional filter, e.g. `'oa'` for OA dynamic form field dict pool
 */
export function listDictTypeOptions(scope?: string) {
  return request.get<ApiResponse<DictTypeVO[]>>(`${DATA_BASE}/types`, {
    params: scope ? { scope } : undefined,
  })
}

export const PERM_Dict = {
  list: 'system:dict:list',
  add: 'system:dict:add',
  update: 'system:dict:update',
  delete: 'system:dict:delete',
}

/** @deprecated use PERM_Dict */
export const PERM_DICT = PERM_Dict

export const GEN_RESOURCE_KEY = 'system.dict' as const

export const genCrudResource = defineMasterDetailGenCrudResource(
  PERM_Dict,
  {
    page: pageDictType,
    get: getDictType,
    create: createDictType,
    update: updateDictType,
    delete: deleteDictType,
  },
  {
    page: pageDictItem,
    get: getDictItem,
    create: createDictItem,
    update: updateDictItem,
    delete: deleteDictItem,
  },
)
