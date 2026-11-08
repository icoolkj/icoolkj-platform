/** Marketing docs categories on the public site (not internal engineering docs). */
export const DOC_CATEGORY_IDS = [
  'overview',
  'capabilities',
  'security',
  'solutions',
  'delivery',
  'resources',
] as const

export type DocCategoryId = (typeof DOC_CATEGORY_IDS)[number]

export function isDocCategoryId(value: string): value is DocCategoryId {
  return (DOC_CATEGORY_IDS as readonly string[]).includes(value)
}
