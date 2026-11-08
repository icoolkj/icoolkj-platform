import { describe, expect, it } from 'vitest'
import { isAbortError } from '@/utils/download'
import { ApiError, isApiMessageKey } from '@/utils/apiError'
import { useExcelImportExport } from '@/composables/useExcelImportExport'

describe('download abort helpers', () => {
  it('detects AbortError', () => {
    expect(isAbortError(new DOMException('Aborted', 'AbortError'))).toBe(true)
    expect(isAbortError(new Error('other'))).toBe(false)
  })
})

describe('async-disabled detection for Excel fallback', () => {
  it('matches ApiError messageKey without relying on localized text', () => {
    const err = new ApiError('异步任务未启用', { messageKey: 'error.async.disabled' })
    expect(isApiMessageKey(err, 'error.async.disabled')).toBe(true)
    expect(isApiMessageKey(new Error('异步任务未启用'), 'error.async.disabled')).toBe(false)
  })
})

describe('useExcelImportExport discovery', () => {
  it('emits export when exportFileAsync + perm present', () => {
    const kit = useExcelImportExport({
      businessName: 'demo',
      apiModule: 'biz',
      api: {
        page: async () => ({}),
        exportFileAsync: async () => ({ taskId: '1' }),
      },
      perm: { export: 'biz:demo:export' },
      builtins: ['export', 'import'],
    })
    expect(kit.actions).toEqual([{ id: 'export', builtin: 'export' }])
  })

  it('requires import API surface for import button', () => {
    const kit = useExcelImportExport({
      businessName: 'demo',
      apiModule: 'biz',
      api: { page: async () => ({}) },
      perm: { import: 'biz:demo:import', export: 'biz:demo:export' },
    })
    // export appears from perm.export alone (sync path default); import needs API surface
    expect(kit.actions.some((a) => a.builtin === 'import')).toBe(false)
    expect(kit.actions.some((a) => a.builtin === 'export')).toBe(true)
  })
})
