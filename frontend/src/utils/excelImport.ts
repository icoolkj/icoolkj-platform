import type { ExcelImportResult } from '@/types/common'

/** Align with backend ExcelImportSupport.MAX_UPLOAD_BYTES */
export const EXCEL_IMPORT_MAX_BYTES = 10 * 1024 * 1024

export type ExcelImportFileError = 'empty' | 'xlsx_only' | 'too_large'

/** Client-side gate before upload (extension + size). */
export function validateExcelImportFile(file: File | null | undefined): ExcelImportFileError | null {
  if (!file) return 'empty'
  if (!file.name.toLowerCase().endsWith('.xlsx')) return 'xlsx_only'
  if (file.size > EXCEL_IMPORT_MAX_BYTES) return 'too_large'
  return null
}

/** Normalize import API return shapes into ExcelImportResult. */
export function unwrapExcelImportResult(raw: unknown): ExcelImportResult | undefined {
  if (!raw || typeof raw !== 'object') return undefined
  const obj = raw as Record<string, unknown>
  if ('success' in obj && ('errors' in obj || 'errorTotal' in obj || 'total' in obj)) {
    return normalizeResult(obj)
  }
  if ('data' in obj && obj.data && typeof obj.data === 'object') {
    const data = obj.data as Record<string, unknown>
    if ('success' in data) {
      return normalizeResult(data)
    }
  }
  return undefined
}

function normalizeResult(obj: Record<string, unknown>): ExcelImportResult {
  const errors = Array.isArray(obj.errors) ? (obj.errors as ExcelImportResult['errors']) : []
  const errorTotal =
    typeof obj.errorTotal === 'number' ? obj.errorTotal : errors.length
  return {
    total: typeof obj.total === 'number' ? obj.total : 0,
    success: typeof obj.success === 'number' ? obj.success : 0,
    skipped: typeof obj.skipped === 'number' ? obj.skipped : 0,
    errorTotal,
    errors,
  }
}

export function excelImportHasErrors(result: ExcelImportResult): boolean {
  return (result.errorTotal ?? 0) > 0 || (result.errors?.length ?? 0) > 0
}
