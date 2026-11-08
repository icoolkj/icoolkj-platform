import { describe, expect, it } from 'vitest'
import {
  RichHtmlBlobRegistry,
  canonicalizeRichHtmlForSave,
  isPlaceholderHtml,
} from '@/utils/richHtmlImagesCore'
import { normalizeInfraUrl } from '@/utils/infraFileUrl'

describe('richHtmlImagesCore', () => {
  it('detects placeholder html', () => {
    expect(isPlaceholderHtml('<p><br></p>')).toBe(true)
    expect(isPlaceholderHtml('<p>content</p>')).toBe(false)
  })

  it('normalizes infra urls without token', () => {
    const url = normalizeInfraUrl(
      '/api/infra/file/1234567890123456789/download?inline=1&access_token=secret',
    )
    expect(url).toBe('/infra/file/1234567890123456789/download?inline=1')
  })

  it('restores canonical src from data attribute', () => {
    const registry = new RichHtmlBlobRegistry()
    const html =
      '<p><img data-infra-canonical-src="/infra/file/1234567890123456789/download?inline=1" src="blob:mock" /></p>'
    const saved = canonicalizeRichHtmlForSave(html, registry)
    expect(saved).toContain('/infra/file/1234567890123456789/download?inline=1')
    expect(saved).not.toContain('blob:')
  })

  it('restores canonical src from blob registry', () => {
    const registry = new RichHtmlBlobRegistry()
    registry.remember('blob:mock-id', '/infra/file/1234567890123456789/download?inline=1')
    const html = '<p><img src="blob:mock-id" /></p>'
    const saved = canonicalizeRichHtmlForSave(html, registry)
    expect(saved).toContain('/infra/file/1234567890123456789/download?inline=1')
  })
})
