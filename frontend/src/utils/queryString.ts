export function buildQueryString(
  params: Record<string, unknown>,
  excludedKeys: string[] = ['pageNo', 'pageSize'],
) {
  const excluded = new Set(excludedKeys)
  const query = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (excluded.has(key) || value === undefined || value === null || value === '') continue
    if (Array.isArray(value)) {
      value.forEach((item) => query.append(key, String(item)))
    } else {
      query.set(key, String(value))
    }
  }
  const text = query.toString()
  return text ? `?${text}` : ''
}
