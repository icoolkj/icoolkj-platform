/**
 * Org ref field value: id + display name snapshot (survives rename / detail without extra fetch).
 */
export type OrgRefItem = { id: string; name: string }

export function normalizeOrgRefValue(raw: unknown): OrgRefItem[] {
  if (raw == null || raw === '') return []
  if (!Array.isArray(raw)) {
    if (typeof raw === 'string') {
      return raw
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean)
        .map((id) => ({ id, name: id }))
    }
    return []
  }
  const out: OrgRefItem[] = []
  for (const item of raw) {
    if (item == null) continue
    if (typeof item === 'string') {
      const id = item.trim()
      if (id) out.push({ id, name: id })
      continue
    }
    if (typeof item === 'object') {
      const rec = item as Record<string, unknown>
      const id = String(rec.id ?? '').trim()
      if (!id) continue
      const name = String(rec.name ?? rec.nickname ?? rec.deptName ?? id).trim() || id
      out.push({ id, name })
    }
  }
  return out
}

export function formatOrgRefDisplay(raw: unknown): string {
  const items = normalizeOrgRefValue(raw)
  if (!items.length) return ''
  return items.map((i) => i.name || i.id).join('、')
}

export function formatDateRangeDisplay(raw: unknown): string {
  if (raw == null || raw === '') return ''
  if (Array.isArray(raw) && raw.length >= 2) {
    const a = String(raw[0] ?? '').trim()
    const b = String(raw[1] ?? '').trim()
    if (!a && !b) return ''
    return `${a || '—'} ~ ${b || '—'}`
  }
  return String(raw)
}
