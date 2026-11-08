/** Pretty-print JSON for diff display; invalid JSON is returned as-is. */
export function formatJsonText(raw: string): string {
  const trimmed = raw.trim()
  if (!trimmed) return ''
  try {
    return JSON.stringify(JSON.parse(trimmed), null, 2)
  } catch {
    return raw
  }
}

export function jsonTextEqual(a: string, b: string): boolean {
  return formatJsonText(a) === formatJsonText(b)
}

export interface JsonDiffRow {
  before: string
  after: string
  changed: boolean
}

export function buildJsonDiffRows(before: string, after: string): JsonDiffRow[] {
  const beforeLines = formatJsonText(before).split('\n')
  const afterLines = formatJsonText(after).split('\n')
  const max = Math.max(beforeLines.length, afterLines.length)
  const rows: JsonDiffRow[] = []
  for (let i = 0; i < max; i++) {
    const left = beforeLines[i] ?? ''
    const right = afterLines[i] ?? ''
    rows.push({ before: left, after: right, changed: left !== right })
  }
  return rows
}
