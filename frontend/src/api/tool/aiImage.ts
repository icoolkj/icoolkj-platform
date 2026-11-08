import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

const BASE = '/tool/ai-image'

export interface AiImageItem {
  fileId: string
  url: string
  fileName?: string
}

export interface AiImageGenerateResult {
  invokeId?: string
  fileId: string
  fileName?: string
  url?: string
  prompt: string
  model?: string
  providerName?: string
  size?: string
  n?: number
  items?: AiImageItem[]
}

export interface AiImageGeneratePayload {
  prompt: string
  size?: string
  n?: number
  negativePrompt?: string
  responseFormat?: 'url' | 'b64_json'
  promptExtend?: boolean
  watermark?: boolean
}

export function generateAiImage(data: AiImageGeneratePayload) {
  return request.post<ApiResponse<AiImageGenerateResult>>(`${BASE}/generate`, data)
}

export function generateAiImageAsync(data: AiImageGeneratePayload) {
  return request.post<ApiResponse<{ taskId: string }>>(`${BASE}/generate-async`, data)
}

export function parseAiImageAsyncResult(resultData?: string | null): AiImageGenerateResult | null {
  if (!resultData?.trim()) return null
  try {
    const parsed = JSON.parse(resultData) as AiImageGenerateResult
    if (!parsed?.fileId && !parsed?.items?.length) return null
    if (!parsed.url && parsed.items?.[0]?.url) {
      parsed.url = parsed.items[0].url
    }
    if (!parsed.fileId && parsed.items?.[0]?.fileId) {
      parsed.fileId = parsed.items[0].fileId
    }
    return parsed
  } catch {
    return null
  }
}
