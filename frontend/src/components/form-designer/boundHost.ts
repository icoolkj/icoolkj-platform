/**
 * Bound Host adapter (V1.2.1): single entry for runtime compose + save prepare.
 * Hosts must not invent their own field-pool filters before calling these APIs.
 */
import {
  composeBoundDocument,
  persistLayout,
  applyRuntimePolicy,
  validateBoundDocument,
  type ComposeBoundOptions,
  type FieldPermMap,
  type RuntimePolicyOptions,
} from '@/components/form-designer/oaBridge'
import type { FormDocument } from '@/types/form-designer/schema'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'

export interface BuildBoundRuntimeInput {
  /** Full contract pool — include file / H; policy hides H. */
  fields: OaFieldDefVO[]
  layoutJson?: string | null
  fieldPerms?: FieldPermMap
  readonly?: boolean
  composeOptions?: ComposeBoundOptions
  policy?: RuntimePolicyOptions
}

/**
 * Compose + applyRuntimePolicy for FormRuntime hosts.
 * Returns null when layout is absent/blank (caller should fall back to OaDynamicForm).
 */
export function buildBoundRuntimeDocument(
  input: BuildBoundRuntimeInput,
): FormDocument | null {
  if (!input.layoutJson?.trim()) {
    return null
  }
  try {
    const composed = composeBoundDocument({
      fields: input.fields,
      layoutJson: input.layoutJson,
      fieldPerms: input.fieldPerms,
      options: {
        appendMissingFields: true,
        keepEmptyCards: true,
        ...input.composeOptions,
      },
    })
    return applyRuntimePolicy(composed, {
      readonly: input.readonly,
      hideHidden: true,
      ...input.policy,
    })
  } catch {
    return null
  }
}

export interface PrepareBoundLayoutSaveInput {
  /** Live field rows (SoT). */
  fields: OaFieldDefVO[]
  /** Current designer document (in-memory). */
  document: FormDocument
}

export type PrepareBoundLayoutSaveResult =
  | { ok: true; layoutJson: string; document: FormDocument }
  | { ok: false; reason: string }

/**
 * Save Gate: live-compose → validate → persist reference shape.
 */
export function prepareBoundLayoutSave(
  input: PrepareBoundLayoutSaveInput,
): PrepareBoundLayoutSaveResult {
  const draftJson = persistLayout(input.document)
  const composed = composeBoundDocument({
    fields: input.fields,
    layoutJson: draftJson,
    options: { appendMissingFields: true, keepEmptyCards: true },
  })
  const gate = validateBoundDocument(composed, input.fields)
  if (!gate.ok) {
    return { ok: false, reason: gate.reason }
  }
  return {
    ok: true,
    layoutJson: persistLayout(composed),
    document: composed,
  }
}
