/**
 * OA 发起目录卡片展示元数据：图标/色调来自字典 wf_biz_type
 *（icon = Element Plus 图标名，list_class/tagType = 色调）。
 */
import type { Component } from 'vue'
import type { DictOption } from '@/types/system/dict'
import { resolveIconComponent } from '@/utils/icon'

export type OaCatalogTone = 'success' | 'warning' | 'info' | 'primary' | 'danger'

const TONES = new Set<string>(['success', 'warning', 'info', 'primary', 'danger'])

export function resolveOaCatalogTone(tagType?: string | null): OaCatalogTone {
  const tone = (tagType || '').trim()
  if (TONES.has(tone)) {
    return tone as OaCatalogTone
  }
  return 'primary'
}

/** Resolve catalog icon; missing/invalid icon falls back to Document. */
export function resolveOaCatalogIcon(icon?: string | null): Component {
  return resolveIconComponent(icon) ?? resolveIconComponent('Document')!
}

export function findBizTypeOption(
  options: DictOption[],
  categoryCode?: string | null,
): DictOption | undefined {
  if (!categoryCode) {
    return undefined
  }
  return options.find((d) => String(d.value) === categoryCode)
}
