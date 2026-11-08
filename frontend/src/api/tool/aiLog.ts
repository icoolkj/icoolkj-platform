import request from '@/utils/request'
import {
  defineReadOnlyGenCrudResource,
} from '@/lib/gen-crud'
import type { GenCrudPerm } from '@/types/gen/crudApi'
import type { GenCrudResource } from '@/components/gen/registry/crud-resources/registry'
import type { ApiResponse, PageResult } from '@/types/common'

const BASE = '/tool/ai-log'

export interface ToolAiInvokeLogVO {
  id: string
  traceId?: string
  providerName?: string
  model?: string
  bizType: string
  bizRefId?: string
  requestUrl?: string
  responseStatus: string
  errorMessage?: string
  costTime?: number
  userPromptPreview?: string
  responsePreview?: string
  operatorName?: string
  createdAt?: string
  systemPrompt?: string
  userMessage?: string
  responseContent?: string
}

export interface ToolAiInvokeLogPageQuery {
  pageNo?: number
  pageSize?: number
  bizType?: string
  responseStatus?: string
  operatorName?: string
}

export interface ToolAiDdlRecordVO {
  id: string
  traceId?: string
  invokeId?: string
  recordType: string
  apiModule?: string
  targetTableName?: string
  generatedSummary?: string
  userPromptPreview?: string
  status: string
  operatorName?: string
  createdAt?: string
  updatedAt?: string
  userPrompt?: string
  generatedDdl?: string
  finalDdl?: string
}

export interface ToolAiDdlRecordPageQuery {
  pageNo?: number
  pageSize?: number
  recordType?: string
  status?: string
  targetTableName?: string
}

export function pageAiInvokeLog(params: ToolAiInvokeLogPageQuery) {
  return request.get<ApiResponse<PageResult<ToolAiInvokeLogVO>>>(`${BASE}/invoke/page`, { params })
}

export function getAiInvokeLog(id: string) {
  return request.get<ApiResponse<ToolAiInvokeLogVO>>(`${BASE}/invoke/${id}`)
}

export function pageAiDdlLog(params: ToolAiDdlRecordPageQuery) {
  return request.get<ApiResponse<PageResult<ToolAiDdlRecordVO>>>(`${BASE}/ddl/page`, { params })
}

export function getAiDdlLog(id: string) {
  return request.get<ApiResponse<ToolAiDdlRecordVO>>(`${BASE}/ddl/${id}`)
}

export function markAiDdlCopied(id: string, data?: { finalDdl?: string }) {
  return request.post<ApiResponse<void>>(`${BASE}/ddl/${id}/copied`, data ?? {})
}

export const PERM = {
  query: 'tool:ai:query',
  generate: 'tool:ai:generate',
}

const PERM_AiLogReadOnly: GenCrudPerm = {
  add: PERM.query,
  update: PERM.query,
  delete: PERM.query,
}

export const GEN_CRUD_RESOURCES: Record<string, GenCrudResource> = {
  'tool.aiInvokeLog': defineReadOnlyGenCrudResource(PERM_AiLogReadOnly, pageAiInvokeLog, {
    get: getAiInvokeLog,
  }),
  'tool.aiDdlLog': defineReadOnlyGenCrudResource(PERM_AiLogReadOnly, pageAiDdlLog, {
    get: getAiDdlLog,
  }),
}
