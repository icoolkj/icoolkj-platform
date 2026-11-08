/** API business error with stable i18n messageKey from backend. */
export class ApiError extends Error {
  readonly code?: number
  readonly messageKey?: string

  constructor(message: string, options?: { code?: number; messageKey?: string }) {
    super(message)
    this.name = 'ApiError'
    this.code = options?.code
    this.messageKey = options?.messageKey
  }
}

export function isApiMessageKey(error: unknown, messageKey: string): boolean {
  if (!messageKey) return false
  if (error instanceof ApiError && error.messageKey === messageKey) {
    return true
  }
  return error instanceof Error && error.message === messageKey
}
