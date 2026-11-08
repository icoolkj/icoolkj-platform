/** Converts flat dot-keys from API into nested vue-i18n message object. */
export function unflattenMessages(flat: Record<string, string>): Record<string, unknown> {
  const root: Record<string, unknown> = {}
  for (const [key, value] of Object.entries(flat)) {
    const parts = key.split('.')
    let current: Record<string, unknown> = root
    for (let i = 0; i < parts.length - 1; i++) {
      const part = parts[i]
      const next = current[part]
      if (!next || typeof next !== 'object' || Array.isArray(next)) {
        current[part] = {}
      }
      current = current[part] as Record<string, unknown>
    }
    current[parts[parts.length - 1]] = value
  }
  return root
}
