import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

export interface AiProviderVO {
  id: string
  tenantId?: string
  providerCode: string
  providerName: string
  protocol?: string
  baseUrl?: string
  apiKeyConfigured?: boolean
  apiKeyMasked?: string
  timeoutSeconds?: number
  status?: string
  remark?: string
  platformOwned?: boolean
}

export interface AiModelVO {
  id: string
  tenantId?: string
  providerId: string
  providerName?: string
  modelCode: string
  modelName: string
  capabilities: string
  defaultParamsJson?: string
  status?: string
  sortNo?: number
  remark?: string
  platformOwned?: boolean
}

export interface AiCapabilityBindingVO {
  id?: string
  tenantId?: string
  capability: string
  modelId?: string
  modelName?: string
  modelCode?: string
  providerName?: string
  enabled?: number
  usingPlatformDefault?: boolean
}

export interface AiRuntimeStatusVO {
  capability: string
  enabled: boolean
  apiKeyConfigured: boolean
  baseUrl?: string
  protocol?: string
  timeoutSeconds?: number
  modelCode?: string
  modelName?: string
  providerName?: string
  usingPlatformDefault?: boolean
}

export type AiProviderForm = {
  id?: string
  providerCode: string
  providerName: string
  protocol?: string
  baseUrl?: string
  apiKey?: string
  timeoutSeconds?: number
  status?: string
  remark?: string
}

export type AiModelForm = {
  id?: string
  providerId: string
  modelCode: string
  modelName: string
  capabilities: string
  defaultParamsJson?: string
  status?: string
  sortNo?: number
  remark?: string
}

const BASE = '/tool/ai-catalog'

export function listAiProviders() {
  return request.get<ApiResponse<AiProviderVO[]>>(`${BASE}/providers`)
}

export function createAiProvider(data: AiProviderForm) {
  return request.post<ApiResponse<string>>(`${BASE}/providers`, data)
}

export function updateAiProvider(data: AiProviderForm) {
  return request.put<ApiResponse<string>>(`${BASE}/providers`, data)
}

export function deleteAiProvider(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/providers/${id}`)
}

export function adoptPlatformProvider(id: string, apiKey: string) {
  return request.post<ApiResponse<string>>(`${BASE}/providers/${id}/adopt`, { apiKey })
}

export function listAiModels() {
  return request.get<ApiResponse<AiModelVO[]>>(`${BASE}/models`)
}

export function listOwnedAiModels() {
  return request.get<ApiResponse<AiModelVO[]>>(`${BASE}/models/owned`)
}

export function createAiModel(data: AiModelForm) {
  return request.post<ApiResponse<string>>(`${BASE}/models`, data)
}

export function updateAiModel(data: AiModelForm) {
  return request.put<ApiResponse<string>>(`${BASE}/models`, data)
}

export function deleteAiModel(id: string) {
  return request.delete<ApiResponse<void>>(`${BASE}/models/${id}`)
}

export function listAiCapabilityBindings() {
  return request.get<ApiResponse<AiCapabilityBindingVO[]>>(`${BASE}/bindings`)
}

export function bindAiCapability(data: { capability: string; modelId?: string }) {
  return request.put<ApiResponse<void>>(`${BASE}/bindings`, data)
}

export function getAiRuntimeStatus(capability: string) {
  return request.get<ApiResponse<AiRuntimeStatusVO>>(`${BASE}/runtime/${capability}`)
}

export function testAiCapability(capability: string) {
  return request.post<ApiResponse<string>>(`${BASE}/runtime/${capability}/test`)
}

export const PERM_AiCatalog = {
  view: 'tool:aiCatalog:view',
  query: 'tool:aiCatalog:query',
  add: 'tool:aiCatalog:add',
  update: 'tool:aiCatalog:update',
  delete: 'tool:aiCatalog:delete',
}

export const PERM_AI = {
  query: 'tool:ai:query',
  generate: 'tool:ai:generate',
  table: 'tool:ai:table',
  codegen: 'tool:ai:codegen',
  image: 'tool:ai:image',
}
