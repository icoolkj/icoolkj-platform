import request from '@/utils/request'
import { defineReadOnlyGenCrudResource } from '@/lib/gen-crud'
import type { GenCrudPerm } from '@/types/gen/crudApi'
import type { ApiResponse, PageResult } from '@/types/common'

const BASE = '/tool/ai-chat'

export interface ToolAiChatMessage {
  id: string
  sessionId: string
  invokeId?: string
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt?: string
}

export interface ToolAiChatSession {
  id: string
  title?: string | null
  status?: string
  operatorName?: string
  createdAt?: string
  updatedAt?: string
  messages?: ToolAiChatMessage[]
}

export interface ToolAiChatSessionPageQuery {
  pageNo?: number
  pageSize?: number
  title?: string
  mineOnly?: boolean
}

export interface ToolAiChatSendResult {
  userMessage: ToolAiChatMessage
  assistantMessage: ToolAiChatMessage
}

export function pageAiChatSessions(params: ToolAiChatSessionPageQuery) {
  return request.get<ApiResponse<PageResult<ToolAiChatSession>>>(`${BASE}/sessions/page`, {
    params,
  })
}

export function createAiChatSession() {
  return request.post<ApiResponse<ToolAiChatSession>>(`${BASE}/sessions`)
}

export function getAiChatSession(id: string, mineOnly = true) {
  return request.get<ApiResponse<ToolAiChatSession>>(`${BASE}/sessions/${id}`, {
    params: { mineOnly },
  })
}

export function sendAiChatMessage(sessionId: string, content: string) {
  return request.post<ApiResponse<ToolAiChatSendResult>>(`${BASE}/sessions/${sessionId}/messages`, {
    content,
  })
}

export function archiveAiChatSession(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/sessions/${id}`)
}

export const PERM = {
  query: 'tool:ai:query',
  generate: 'tool:ai:generate',
}

const PERM_AiChatLogReadOnly: GenCrudPerm = {
  add: PERM.query,
  update: PERM.query,
  delete: PERM.query,
}

export const GEN_RESOURCE_KEY = 'tool.aiChatLog' as const

export const genCrudResource = defineReadOnlyGenCrudResource(
  PERM_AiChatLogReadOnly,
  pageAiChatSessions,
  { get: getAiChatSession },
)
