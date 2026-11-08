import { describe, expect, it } from 'vitest'
import { parseAiImageAsyncResult } from '@/api/tool/aiImage'

describe('parseAiImageAsyncResult', () => {
  it('parses fileId contract and fills primary url', () => {
    const raw = JSON.stringify({
      fileId: 'f1',
      fileName: 'ai-image-f1.png',
      prompt: 'cat',
      items: [{ fileId: 'f1', url: '/infra/file/f1/download?inline=1' }],
    })
    const parsed = parseAiImageAsyncResult(raw)
    expect(parsed?.fileId).toBe('f1')
    expect(parsed?.url).toBe('/infra/file/f1/download?inline=1')
    expect(parsed?.items).toHaveLength(1)
  })

  it('returns null on invalid payload', () => {
    expect(parseAiImageAsyncResult('')).toBeNull()
    expect(parseAiImageAsyncResult('{}')).toBeNull()
    expect(parseAiImageAsyncResult('{')).toBeNull()
  })
})
