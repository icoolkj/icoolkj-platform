import type { ComputedRef, Ref } from 'vue'
import type { GenCrudApi, GenCrudPerm } from '@/types/gen/crudApi'
import type { GenViewSchema } from '@/types/tool/genView'
import type { DictOption } from '@/types/system/dict'

export interface GenPanelContext {
  businessName: string
  apiModule: string
  perm: GenCrudPerm
  api: GenCrudApi
  readOnly: ComputedRef<boolean>
  gp: (key: string, params?: Record<string, string | number>) => string
  gf: (field: string, key?: 'label' | 'placeholder' | 'placeholderReject') => string
  t: (key: string, params?: Record<string, unknown>) => string
  getDict: (dictType: string) => DictOption[]
  treeSources: ComputedRef<Record<string, Record<string, unknown>[]>>
}

export interface GenCrudListState {
  loading: Ref<boolean>
  dataList: Ref<Record<string, unknown>[]>
  total: Ref<number>
  queryParams: Record<string, unknown>
  loadPage: () => Promise<void>
}

export interface GenCrudFormState {
  formVisible: Ref<boolean>
  form: Ref<Record<string, unknown>>
  openForm: (row?: Record<string, unknown>) => void
  closeForm: () => void
  submitForm: () => Promise<void>
}

export type { GenViewSchema }
