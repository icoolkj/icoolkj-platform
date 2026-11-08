import type { FormRules } from 'element-plus'
import type { InjectionKey, Ref, ComputedRef } from 'vue'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import type { GenCrudApi, GenCrudPerm } from '@/types/gen/crudApi'
import type { GenViewRowAction, GenViewSchema, GenViewTableField } from '@/types/tool/genView'
import type GenEntityForm from '@/components/gen/fields/GenEntityForm.vue'
import type { GenCrudExtensionsReturn } from './useGenCrudExtensions'
import type { GenCrudFormReturn } from './useGenCrudForm'
import type { GenCrudListReturn } from './useGenCrudList'

export type GenCrudContext = {
  genReady: Ref<boolean>
  viewReady: Ref<boolean>
  dictReady: Ref<boolean>
  treeSourcesReady: Ref<boolean>
  gp: (key: string, params?: Record<string, string | number>) => string
  gf: (field: string, key?: 'label' | 'placeholder' | 'placeholderReject') => string
  formRules: ComputedRef<FormRules>
  resolveFieldValidation: (field: string, rule: string) => string
  view: ComputedRef<GenViewSchema>
  readOnly: ComputedRef<boolean>
  customForm: ComputedRef<boolean>
  perm: GenCrudPerm
  api: GenCrudApi
  businessName: string
  apiModule: string
  getDict: (dictType: string) => import('@/types/system/dict').DictOption[]
  treeSources: ComputedRef<Record<string, Record<string, unknown>[]>>
  list: GenCrudListReturn
  form: GenCrudFormReturn & { bindEntityFormRef: (ref: InstanceType<typeof GenEntityForm> | undefined) => void }
  ext: GenCrudExtensionsReturn
  showSearch: Ref<boolean>
  showActionsColumn: ComputedRef<boolean>
  actionsColumnWidth: ComputedRef<number>
  hasRowActionsSlot: ComputedRef<boolean>
  statusChange?: (row: Record<string, unknown>, status: string) => Promise<void>
  formatAuditI18n: (
    category: 'modules' | 'businessTypes' | undefined,
    value?: string,
  ) => string
  clearSelection: () => void
  onStatusSwitch: (
    row: Record<string, unknown>,
    col: GenViewTableField,
    status: string,
  ) => Promise<void>
  tablePanelRef: Ref<{ clearSelection: () => void } | undefined>
}

export const GEN_CRUD_CTX: InjectionKey<GenCrudContext> = Symbol('GenCrudContext')

export type { GenViewRowAction, GenRowActionHandler }
