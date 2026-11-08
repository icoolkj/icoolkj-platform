/**
 * 文件目录槽位 → 上传控件模型。
 * CatalogAttachmentManager / brandPack 等共用；停用槽位不渲染。
 */
import type { FileCatalogItemVO, FileProfileVO } from '@/types/infra/fileProfile'
import { isStatusDisabled, isYes } from '@/constants/flags'
import { resolveI18nText } from '@/utils/i18nTextMap'
import {
  acceptFromProfile,
  buildProfileUploadHint,
  constraintsFromProfile,
  formatByteSize,
  type FileUploadConstraints,
} from '@/utils/fileUploadConstraints'
import {
  ATTACHMENT_MODE_MULTIPLE,
  ATTACHMENT_MODE_SINGLE,
  ATTACHMENT_VARIANT_IMAGE,
  ATTACHMENT_VARIANT_LIST,
  type AttachmentMode,
  type AttachmentVariant,
} from '@/constants/file'

export type CatalogSlot = {
  profileCode: string
  label: string
  /** Profile.remark 等说明 */
  remark: string
  hint: string
  accept: string
  sortNo: number
  required: boolean
  constraints: FileUploadConstraints
  profile?: FileProfileVO
  mode: AttachmentMode
  variant: AttachmentVariant
}

type HintTranslator = (key: string, params?: Record<string, string | number>) => string

function slotLabel(item: FileCatalogItemVO, locale: string): string {
  const p = item.resolvedProfile
  return resolveI18nText(p?.profileNameI18n, p?.profileName || item.profileName, locale)
}

function mergeConstraints(item: FileCatalogItemVO, profile?: FileProfileVO): FileUploadConstraints {
  const base = constraintsFromProfile(profile)
  const required =
    item.required != null && item.required !== ''
      ? isYes(item.required) || item.required === 'true'
      : base.required
  return {
    ...base,
    required,
    minNum: item.minNum ?? base.minNum,
    maxNum: item.maxNum ?? base.maxNum,
  }
}

function resolveMode(constraints: FileUploadConstraints): AttachmentMode {
  const max = constraints.maxNum
  return max != null && max > 1 ? ATTACHMENT_MODE_MULTIPLE : ATTACHMENT_MODE_SINGLE
}

/** 仅允许图片扩展名时用 image 预览，否则 list */
function resolveVariant(constraints: FileUploadConstraints): AttachmentVariant {
  const exts = constraints.allowedExts
  if (!exts.length) return ATTACHMENT_VARIANT_LIST
  const imageExts = new Set(['png', 'jpg', 'jpeg', 'gif', 'webp', 'ico', 'svg', 'bmp'])
  const allImage = exts.every((e) => imageExts.has(e.toLowerCase()))
  return allImage ? ATTACHMENT_VARIANT_IMAGE : ATTACHMENT_VARIANT_LIST
}

/** 仅启用槽位；label/accept/hint/约束来自目录解析后的 Profile + 目录项覆盖 */
export function buildCatalogSlots(
  items: FileCatalogItemVO[] | undefined,
  locale: string,
  tCommon: HintTranslator,
): CatalogSlot[] {
  return (items || [])
    .filter((it) => !isStatusDisabled(it.status) && !!it.profileCode)
    .map((it) => {
      const profile = it.resolvedProfile
      const constraints = mergeConstraints(it, profile)
      return {
        profileCode: it.profileCode,
        label: slotLabel(it, locale),
        remark: (profile?.remark || '').trim(),
        hint: buildProfileUploadHint(profile, tCommon),
        accept: acceptFromProfile(profile),
        sortNo: it.sortNo ?? 0,
        required: constraints.required,
        constraints,
        profile,
        mode: resolveMode(constraints),
        variant: resolveVariant(constraints),
      }
    })
    .sort((a, b) => a.sortNo - b.sortNo)
}

/** 类型要求文案（扩展名） */
export function formatSlotTypeReq(slot: CatalogSlot, empty = '—'): string {
  const exts = slot.constraints.allowedExts
  if (exts.length) return exts.map((e) => e.toUpperCase()).join(' / ')
  const types = slot.constraints.allowedContentTypes
  if (types.length) return types.join(' / ')
  return empty
}

/** 数量要求文案 */
export function formatSlotCountReq(
  slot: CatalogSlot,
  t: HintTranslator,
  empty = '—',
): string {
  const min = slot.constraints.minNum
  const max = slot.constraints.maxNum
  if (min == null && max == null) return empty
  if (min != null && max != null) {
    return t('attachmentReqCountRange', { min, max })
  }
  if (max != null) return t('fileUploadMaxNum', { n: max })
  return t('attachmentReqMinNum', { n: min! })
}

/** 大小要求文案 */
export function formatSlotSizeReq(
  slot: CatalogSlot,
  t: HintTranslator,
  empty = '—',
): string {
  const min = slot.constraints.minSizeBytes
  const max = slot.constraints.maxSizeBytes
  if (min == null && max == null) return empty
  if (min != null && max != null) {
    return t('attachmentReqSizeRange', {
      min: formatByteSize(min),
      max: formatByteSize(max),
    })
  }
  if (max != null) return t('fileUploadMaxSize', { size: formatByteSize(max) })
  return t('fileUploadMinSize', { size: formatByteSize(min!) })
}

/** 展平槽位值为 fileId 列表 */
export function flattenCatalogFileIds(
  value: Record<string, string | string[]> | null | undefined,
): string[] {
  if (!value) return []
  const ids: string[] = []
  for (const raw of Object.values(value)) {
    if (Array.isArray(raw)) {
      for (const id of raw) {
        const t = String(id || '').trim()
        if (t) ids.push(t)
      }
    } else {
      const t = String(raw || '').trim()
      if (t) ids.push(t)
    }
  }
  return ids
}

/** 单个槽位已传文件数 */
export function countSlotFiles(value: string | string[] | null | undefined): number {
  if (value == null || value === '') return 0
  if (Array.isArray(value)) {
    return value.map((v) => String(v || '').trim()).filter(Boolean).length
  }
  return String(value).trim() ? 1 : 0
}

/** 槽位最少应传数量：必传至少 1；否则看 minNum */
export function slotMinRequired(slot: CatalogSlot): number {
  const min = slot.constraints.minNum
  if (slot.required) {
    return Math.max(min ?? 0, 1)
  }
  return min != null && min > 0 ? min : 0
}

export type CatalogAssetsValidation =
  | { ok: true }
  | { ok: false; missing: CatalogSlot[] }

/** 校验目录槽位必传 / 最小数量 */
export function validateCatalogAssets(
  slots: CatalogSlot[],
  assets: Record<string, string | string[]> | null | undefined,
): CatalogAssetsValidation {
  const missing: CatalogSlot[] = []
  for (const slot of slots) {
    const need = slotMinRequired(slot)
    if (need <= 0) continue
    if (countSlotFiles(assets?.[slot.profileCode]) < need) {
      missing.push(slot)
    }
  }
  return missing.length ? { ok: false, missing } : { ok: true }
}
