/** Resolve encoded i18n payloads (key + optional US-unit-separated args) for async/Excel UI. */

export type ResolveI18nPayloadOptions = {
  fallbackKey?: string
  /**
   * Resolve Gen flat validation keys {@code gen.{business}.{field}.{rule}} via page Git i18n
   * (nested under fields[field].validation[rule]), not vue-i18n flat lookup.
   */
  resolveGenValidation?: (business: string, field: string, rule: string) => string | undefined
}

/** Parse {@code gen.{business}.{field}.{rule}} (rule may contain dots). */
export function parseGenValidationKey(
  key: string,
): { business: string; field: string; rule: string } | null {
  if (!key.startsWith('gen.')) return null
  const parts = key.split('.')
  if (parts.length < 4) return null
  const business = parts[1]
  const field = parts[2]
  const rule = parts.slice(3).join('.')
  if (!business || !field || !rule) return null
  return { business, field, rule }
}

export function resolveI18nPayloadMessage(
  t: (key: string, ...args: unknown[]) => string,
  raw: string | null | undefined,
  fallbackKeyOrOptions?: string | ResolveI18nPayloadOptions,
): string {
  const options: ResolveI18nPayloadOptions =
    typeof fallbackKeyOrOptions === 'string'
      ? { fallbackKey: fallbackKeyOrOptions }
      : (fallbackKeyOrOptions ?? {})

  if (!raw?.trim()) {
    return options.fallbackKey ? t(options.fallbackKey) : ''
  }
  const parts = raw.trim().split('\u001f')
  const key = parts[0] ?? ''
  const argKeys = parts.slice(1)
  if (
    key.startsWith('error.') ||
    key.startsWith('common.') ||
    key.startsWith('validation.') ||
    key.startsWith('gen.')
  ) {
    if (key.startsWith('gen.') && options.resolveGenValidation) {
      const parsed = parseGenValidationKey(key)
      if (parsed) {
        const genText = options.resolveGenValidation(parsed.business, parsed.field, parsed.rule)
        if (genText) return genText
      }
    }
    const listArgs = argKeys.map((a) => {
      if (
        a.startsWith('error.') ||
        a.startsWith('entity.') ||
        a.startsWith('fields.') ||
        a.startsWith('validation.') ||
        a.startsWith('gen.')
      ) {
        if (a.startsWith('gen.') && options.resolveGenValidation) {
          const nestedParsed = parseGenValidationKey(a)
          if (nestedParsed) {
            const nestedGen = options.resolveGenValidation(
              nestedParsed.business,
              nestedParsed.field,
              nestedParsed.rule,
            )
            if (nestedGen) return nestedGen
          }
        }
        const nested = t(a)
        return nested !== a ? nested : a
      }
      return a
    })
    const translated = listArgs.length > 0 ? t(key, listArgs) : t(key)
    return translated !== key ? translated : key
  }
  return raw.trim()
}

/** @deprecated use {@link resolveI18nPayloadMessage} */
export function resolveAsyncTaskErrorMessage(
  t: (key: string, ...args: unknown[]) => string,
  raw: string | null | undefined,
  fallbackKey = 'common.importAsyncFailed',
): string {
  return resolveI18nPayloadMessage(t, raw, fallbackKey)
}
