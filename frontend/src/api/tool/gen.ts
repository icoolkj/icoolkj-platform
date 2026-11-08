import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type { GenTable, GenTablePageQuery } from '@/types/tool/gen'

const BASE = '/tool/gen'

export function pageGenTable(params: GenTablePageQuery) {
  return request.get<ApiResponse<PageResult<GenTable>>>(`${BASE}/page`, { params })
}

export function listGenTable(params: Partial<GenTable> = {}) {
  return request.get<ApiResponse<GenTable[]>>(`${BASE}/list`, { params })
}

export function pageDbTable(params: GenTablePageQuery) {
  return request.get<ApiResponse<PageResult<GenTable>>>(`${BASE}/db/page`, { params })
}

export function getGenTable(id: string) {
  return request.get<ApiResponse<GenTable>>(`${BASE}/${id}`)
}

export function updateGenTable(data: GenTable) {
  return request.put<ApiResponse<void>>(`${BASE}/update`, data)
}

export function importGenTable(tables: string) {
  return request.post<ApiResponse<void>>(`${BASE}/import`, null, { params: { tables } })
}

export function deleteGenTable(ids: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/${ids}`)
}

export function previewGenTable(id: string) {
  return request.get<ApiResponse<Record<string, string>>>(`${BASE}/preview/${id}`)
}

export function genCodeToDisk(tableName: string) {
  return request.post<ApiResponse<void>>(`${BASE}/genCode/${tableName}`)
}

export function synchGenDb(tableName: string) {
  return request.post<ApiResponse<void>>(`${BASE}/synchDb/${tableName}`)
}

export const PERM_GEN = {
  view: 'tool:gen:view',
  list: 'tool:gen:list',
  query: 'tool:gen:query',
  import: 'tool:gen:import',
  edit: 'tool:gen:edit',
  remove: 'tool:gen:remove',
  preview: 'tool:gen:preview',
  code: 'tool:gen:code',
}
