import { describe, expect, it } from 'vitest'
import { parseGenValidationKey, resolveI18nPayloadMessage } from '@/utils/asyncTaskError'
import { toExcelExportQuery } from '@/composables/useGenExcelToolbar'

describe('resolveI18nPayloadMessage', () => {
  const t = (key: string, args?: unknown) => {
    if (key === 'error.excel.import_dept_not_found' && Array.isArray(args)) {
      return `dept missing: ${args[0]}`
    }
    if (key === 'error.async.task_failed') return 'task failed'
    if (key === 'common.importAsyncFailed') return 'import failed'
    return key
  }

  it('translates key with US-separated args', () => {
    const raw = `error.excel.import_dept_not_found\u001f研发`
    expect(resolveI18nPayloadMessage(t, raw)).toBe('dept missing: 研发')
  })

  it('falls back for empty payload', () => {
    expect(resolveI18nPayloadMessage(t, '', 'common.importAsyncFailed')).toBe('import failed')
  })

  it('passes through literal text', () => {
    expect(resolveI18nPayloadMessage(t, '部门不存在')).toBe('部门不存在')
  })

  it('resolves gen validation keys via nested Git i18n', () => {
    const text = resolveI18nPayloadMessage(t, 'gen.user.username.required', {
      resolveGenValidation: (biz, field, rule) =>
        biz === 'user' && field === 'username' && rule === 'required' ? '用户名必填' : undefined,
    })
    expect(text).toBe('用户名必填')
  })
})

describe('parseGenValidationKey', () => {
  it('parses gen.business.field.rule', () => {
    expect(parseGenValidationKey('gen.user.username.required')).toEqual({
      business: 'user',
      field: 'username',
      rule: 'required',
    })
  })
})

describe('toExcelExportQuery', () => {
  it('strips pagination', () => {
    expect(toExcelExportQuery({ pageNo: 1, pageSize: 10, name: 'a' })).toEqual({ name: 'a' })
  })
})
