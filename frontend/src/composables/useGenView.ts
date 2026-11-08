import { computed, ref, watch } from 'vue'
import type {
  GenViewFormField,
  GenViewQueryField,
  GenViewSchema,
  GenViewTableField,
} from '@/types/tool/genView'

type FieldRow = GenViewQueryField | GenViewTableField | GenViewFormField

function collectFieldsDictTypes(fields: FieldRow[] | undefined, types: Set<string>) {
  for (const field of fields ?? []) {
    if (field.dictType) types.add(field.dictType)
  }
}

export function collectDictTypes(view: GenViewSchema | null | undefined): string[] {
  const types = new Set<string>()
  if (!view) return []
  collectFieldsDictTypes(view.query?.fields, types)
  collectFieldsDictTypes(view.table?.fields, types)
  collectFieldsDictTypes(view.form?.fields, types)
  collectFieldsDictTypes(view.masterDetail?.master?.query?.fields, types)
  collectFieldsDictTypes(view.masterDetail?.master?.table?.fields, types)
  collectFieldsDictTypes(view.masterDetail?.master?.form?.fields, types)
  collectFieldsDictTypes(view.masterDetail?.detail?.table?.fields, types)
  collectFieldsDictTypes(view.masterDetail?.detail?.form?.fields, types)
  collectFieldsDictTypes(view.regionOrg?.competentTable?.fields, types)
  collectFieldsDictTypes(view.regionOrg?.competentForm?.fields, types)
  collectFieldsDictTypes(view.regionOrg?.internalForm?.fields, types)
  return [...types]
}

/**
 * CRUD layout from Git view.json bundled with the page (P0: Git-only runtime).
 * DB gen_view_config is for generator admin / future draft workflow — not read here.
 */
export function useGenView(_businessName: string, _apiModule: string, fileView: GenViewSchema) {
  const ready = ref(true)
  const view = computed(() => fileView)

  watch(
    () => fileView,
    () => {
      ready.value = true
    },
    { deep: true },
  )

  return { ready, view, reload: async () => {} }
}

/** No-op: runtime no longer caches DB overrides (Git-only). Kept for generator admin forms. */
export function clearGenViewCache(_businessName?: string, _apiModule?: string) {}
