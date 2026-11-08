/**
 * Lightweight Markdown → HTML for office AI replies (tables / headings / lists / emphasis).
 * Escapes raw HTML first to avoid XSS from model output.
 */
export function renderOfficeAiMarkdown(source: string): string {
  if (!source) {
    return ''
  }
  const escaped = escapeHtml(source.replace(/\r\n/g, '\n'))
  const blocks = escaped.split(/\n{2,}/)
  const html: string[] = []

  for (const rawBlock of blocks) {
    const block = rawBlock.trim()
    if (!block) {
      continue
    }
    const tableHtml = tryRenderTable(block)
    if (tableHtml) {
      html.push(tableHtml)
      continue
    }
    const lines = block.split('\n')
    const nonEmpty = lines.map((l) => l.trim()).filter(Boolean)
    if (nonEmpty.length && nonEmpty.every((line) => /^[-*]\s+/.test(line))) {
      html.push(renderList(nonEmpty, 'ul'))
      continue
    }
    if (nonEmpty.length && nonEmpty.every((line) => /^\d+\.\s+/.test(line))) {
      html.push(renderList(nonEmpty, 'ol'))
      continue
    }
    if (lines.length === 1) {
      const heading = lines[0].match(/^(#{1,3})\s+(.+)$/)
      if (heading) {
        const level = heading[1].length
        html.push(`<h${level}>${inlineFormat(heading[2])}</h${level}>`)
        continue
      }
    }
    html.push(`<p>${lines.map((line) => inlineFormat(line)).join('<br>')}</p>`)
  }

  return html.join('')
}

const TABLE_SEP = /^\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?$/

function tryRenderTable(block: string): string | null {
  const lines = block
    .split('\n')
    .map((l) => l.trim())
    .filter(Boolean)
  if (lines.length < 2 || !lines[0].includes('|') || !TABLE_SEP.test(lines[1])) {
    return null
  }

  const parseRow = (line: string) =>
    line
      .replace(/^\|/, '')
      .replace(/\|$/, '')
      .split('|')
      .map((cell) => cell.trim())

  const header = parseRow(lines[0])
  const body = lines.slice(2).map(parseRow)
  if (!header.length) {
    return null
  }

  let out = '<div class="md-table-wrap"><table><thead><tr>'
  for (const cell of header) {
    out += `<th>${inlineFormat(cell)}</th>`
  }
  out += '</tr></thead><tbody>'
  for (const row of body) {
    out += '<tr>'
    for (let i = 0; i < header.length; i++) {
      out += `<td>${inlineFormat(row[i] ?? '')}</td>`
    }
    out += '</tr>'
  }
  out += '</tbody></table></div>'
  return out
}

function renderList(lines: string[], tag: 'ul' | 'ol'): string {
  const items = lines.map((line) => line.replace(/^([-*]|\d+\.)\s+/, ''))
  return `<${tag}>${items.map((item) => `<li>${inlineFormat(item)}</li>`).join('')}</${tag}>`
}

function inlineFormat(text: string): string {
  return text
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/__(.+?)__/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}
