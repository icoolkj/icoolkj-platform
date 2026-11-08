export const AI_PROTOCOLS = [
  { value: 'openai_compatible', label: 'OpenAI 兼容' },
] as const

export const AI_CAPABILITIES = [
  { value: 'chat', label: '对话 (chat)', implemented: true },
  { value: 'image', label: '图像 (image)', implemented: true },
  { value: 'embedding', label: '向量 (embedding)', implemented: false },
] as const

export const AI_CAPABILITIES_IMPLEMENTED = AI_CAPABILITIES.filter((c) => c.implemented)

export const AI_PROTOCOL_DEFAULT = AI_PROTOCOLS[0].value
export const AI_CAPABILITY_DEFAULT = AI_CAPABILITIES_IMPLEMENTED[0].value

export function parseCapabilities(raw?: string | null): string[] {
  if (!raw?.trim()) return [AI_CAPABILITY_DEFAULT]
  return raw
    .split(',')
    .map((x) => x.trim().toLowerCase())
    .filter(Boolean)
}

export function joinCapabilities(values: string[]): string {
  return values.map((x) => x.trim().toLowerCase()).filter(Boolean).join(',')
}

export function isCapabilityImplemented(value: string): boolean {
  return AI_CAPABILITIES_IMPLEMENTED.some((c) => c.value === value)
}
