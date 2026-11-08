/**
 * 产品宇宙常量 — 前端单一来源。
 *
 * 与后端 `sys_product` 种子保持一致（docs/architecture/38-product-universe-console-shell.md）；
 * 运行时可用产品以 `GET /system/product/mine` 为准，此处仅提供默认码、展示顺序与类型兜底。
 * 新增业务产品（sort_no 1–49）时：后端种子 + 本文件 `BUILTIN_PRODUCT_ORDER` 前插。
 */

export const OA_PRODUCT_CODE = 'oa'
export const FILES_PRODUCT_CODE = 'files'
export const AI_PRODUCT_CODE = 'ai'
export const PLATFORM_PRODUCT_CODE = 'platform'

/** 内置产品展示顺序（业务产品由后端 sort_no 决定，插入此数组前） */
export const BUILTIN_PRODUCT_ORDER = [
  OA_PRODUCT_CODE,
  FILES_PRODUCT_CODE,
  AI_PRODUCT_CODE,
  PLATFORM_PRODUCT_CODE,
] as const

export type BuiltinProductCode = (typeof BUILTIN_PRODUCT_ORDER)[number]

/** 按内置顺序排序产品码；未知码排在最后并按字典序稳定 */
export function sortProductCodes(codes: Iterable<string>): string[] {
  const order = new Map<string, number>()
  BUILTIN_PRODUCT_ORDER.forEach((code, index) => order.set(code, index))
  return [...codes].sort((a, b) => {
    const ai = order.get(a) ?? Number.MAX_SAFE_INTEGER
    const bi = order.get(b) ?? Number.MAX_SAFE_INTEGER
    if (ai !== bi) return ai - bi
    return a.localeCompare(b)
  })
}

/** 默认产品码：优先 platform，否则按内置顺序取第一个可用码 */
export function resolveDefaultProductCode(availableCodes: Iterable<string>): string {
  const codes = new Set(availableCodes)
  if (codes.has(PLATFORM_PRODUCT_CODE)) return PLATFORM_PRODUCT_CODE
  return sortProductCodes(codes)[0] ?? ''
}
