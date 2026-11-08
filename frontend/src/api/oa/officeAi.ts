/** 在线办公 · 用户侧 AI Skills */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'

const BASE = '/oa/office/ai'

export interface OfficeAiSkillVO {
  code: string
  name: string
  description: string
  requiredPermission: string
  allowed: boolean
}

export interface OfficeAiChatResultVO {
  sessionId: string
  reply: string
  usedSkills: string[]
}

export interface OfficeAiSessionVO {
  id: string
  title?: string | null
  status: string
  lastMessageAt?: string | null
  messageCount?: number
  createdAt?: string
}

export interface OfficeAiMessageVO {
  id: string
  sessionId: string
  role: 'user' | 'assistant' | string
  content: string
  usedSkills?: string[]
  createdAt?: string
}

export function listOfficeAiSkills() {
  return request.get<ApiResponse<OfficeAiSkillVO[]>>(`${BASE}/skills`)
}

export function createOfficeAiSession() {
  return request.post<ApiResponse<OfficeAiSessionVO>>(`${BASE}/sessions`)
}

export function pageOfficeAiSessions(params?: { pageNo?: number; pageSize?: number }) {
  return request.get<ApiResponse<PageResult<OfficeAiSessionVO>>>(`${BASE}/sessions`, { params })
}

export function listOfficeAiMessages(sessionId: string) {
  return request.get<ApiResponse<OfficeAiMessageVO[]>>(`${BASE}/sessions/${sessionId}/messages`)
}

export function deleteOfficeAiSession(sessionId: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/sessions/${sessionId}`)
}

export function chatOfficeAi(message: string, sessionId?: string | null) {
  return request.post<ApiResponse<OfficeAiChatResultVO>>(`${BASE}/chat`, {
    message,
    sessionId: sessionId || undefined,
  })
}

export const PERM_OfficeAi = {
  view: 'oa:office:ai:view',
  chat: 'oa:office:ai:chat',
}
