import { describe, expect, it } from 'vitest'
import { ApiError, isApiMessageKey } from '@/utils/apiError'

describe('apiError', () => {
  it('matches stable messageKey on ApiError', () => {
    const err = new ApiError('localized', { messageKey: 'error.async.disabled' })
    expect(isApiMessageKey(err, 'error.async.disabled')).toBe(true)
    expect(isApiMessageKey(err, 'error.other')).toBe(false)
  })

  it('matches raw key on plain Error', () => {
    expect(isApiMessageKey(new Error('error.async.disabled'), 'error.async.disabled')).toBe(true)
  })
})
