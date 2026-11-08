export interface GenFieldValidation {
  required?: string
  requiredOnReject?: string
  maxLength?: string
  minLength?: string
  max?: string
  min?: string
}

export interface GenFieldMessages {
  label?: string
  placeholder?: string
  /** Shown when reject is selected / intended */
  placeholderReject?: string
  validation?: GenFieldValidation
}

export interface GenFieldConstraints {
  required?: boolean
  maxLength?: number
  minLength?: number
  max?: number
  min?: number
}

export interface GenI18nMessages {
  page?: Record<string, string>
  /** Flat labels keyed by config key / code (may contain dots). */
  keys?: Record<string, string>
  audit?: {
    modules?: Record<string, string>
    businessTypes?: Record<string, string>
  }
  fields?: Record<string, GenFieldMessages>
  meta?: {
    businessName?: string
    apiModule?: string
    functionName?: string
    constraints?: Record<string, GenFieldConstraints>
  }
}

export interface GenI18nMessageRow {
  id?: string
  tableId: string
  locale: string
  messagesJson: string
  remark?: string
}
