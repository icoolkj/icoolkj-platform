export interface GenTableMenuOptions {
  productCode?: string
  assignScope?: 'TENANT' | 'PLATFORM'
  parentMenuId?: string
}

export function parseGenTableOptions(options?: string | null): GenTableMenuOptions {
  if (!options?.trim()) return {}
  try {
    const parsed = JSON.parse(options) as GenTableMenuOptions
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

export function writeGenTableOptions(opts: GenTableMenuOptions): string | undefined {
  const clean: GenTableMenuOptions = {}
  if (opts.productCode?.trim()) clean.productCode = opts.productCode.trim()
  if (opts.assignScope) clean.assignScope = opts.assignScope
  if (opts.parentMenuId?.trim()) clean.parentMenuId = opts.parentMenuId.trim()
  return Object.keys(clean).length ? JSON.stringify(clean) : undefined
}

export function mergeGenTableOptions(
  options: string | undefined,
  patch: GenTableMenuOptions,
): string | undefined {
  return writeGenTableOptions({ ...parseGenTableOptions(options), ...patch })
}
