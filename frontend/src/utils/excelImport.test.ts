import { describe, expect, it } from 'vitest'
import {
  EXCEL_IMPORT_MAX_BYTES,
  excelImportHasErrors,
  unwrapExcelImportResult,
  validateExcelImportFile,
} from '@/utils/excelImport'
import { toExcelExportQuery } from '@/composables/useGenExcelToolbar'

describe('excelImport utils', () => {
  it('validates extension and size', () => {
    expect(validateExcelImportFile(null)).toBe('empty')
    expect(validateExcelImportFile(new File(['x'], 'a.csv'))).toBe('xlsx_only')
    expect(validateExcelImportFile(new File(['x'], 'a.xlsx'))).toBeNull()
    const big = new File([new Uint8Array(EXCEL_IMPORT_MAX_BYTES + 1)], 'a.xlsx')
    expect(validateExcelImportFile(big)).toBe('too_large')
  })

  it('unwraps nested and flat import results', () => {
    expect(unwrapExcelImportResult(undefined)).toBeUndefined()
    const flat = unwrapExcelImportResult({
      total: 2,
      success: 0,
      skipped: 0,
      errorTotal: 2,
      errors: [{ rowNumber: 2, message: 'bad' }],
    })
    expect(flat?.errorTotal).toBe(2)
    expect(excelImportHasErrors(flat!)).toBe(true)

    const nested = unwrapExcelImportResult({
      data: { total: 1, success: 1, skipped: 0, errorTotal: 0, errors: [] },
    })
    expect(nested?.success).toBe(1)
    expect(excelImportHasErrors(nested!)).toBe(false)
  })

  it('treats malformed payload as missing result', () => {
    expect(unwrapExcelImportResult({ code: 0, message: 'ok' })).toBeUndefined()
  })
})

describe('toExcelExportQuery', () => {
  it('strips pagination params', () => {
    expect(toExcelExportQuery({ pageNo: 1, pageSize: 10000, name: 'a' })).toEqual({ name: 'a' })
  })
})
