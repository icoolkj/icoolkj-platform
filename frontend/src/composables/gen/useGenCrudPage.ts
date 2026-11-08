import { computed, provide, ref, useSlots } from 'vue'
import type { GenPatternStandardCrudProps, GenPageEmits } from '@/components/gen/patterns/types'
import { useDict } from '@/composables/useDict'
import { useGenI18n } from '@/composables/useGenI18n'
import { useOperationLogI18n } from '@/composables/useOperationLogI18n'
import { collectDictTypes, useGenView } from '@/composables/useGenView'
import { useGenTreeSources } from '@/composables/useGenTreeSources'
import { useDemoStore } from '@/stores/demo'
import type { GenFieldValidation } from '@/types/tool/genI18n'
import type { GenViewTableField } from '@/types/tool/genView'
import type GenEntityForm from '@/components/gen/fields/GenEntityForm.vue'
import { GEN_CRUD_CTX, type GenCrudContext } from './genCrudContext'
import { useGenCrudExtensions } from './useGenCrudExtensions'
import { useGenCrudForm } from './useGenCrudForm'
import { useGenCrudList } from './useGenCrudList'

export type UseGenCrudPageProps = GenPatternStandardCrudProps

export type UseGenCrudPageEmit = {
  <K extends keyof GenPageEmits>(e: K, ...args: GenPageEmits[K]): void
}

export function useGenCrudPage(props: UseGenCrudPageProps, emit: UseGenCrudPageEmit) {
  const slots = useSlots()
  const demoStore = useDemoStore()
  const showSearch = ref(true)
  const tablePanelRef = ref<{ clearSelection: () => void }>()

  const {
    ready: genReady,
    gp,
    gf,
    gv,
    formRules,
  } = useGenI18n(props.businessName, props.apiModule, props.i18nFiles)

  function resolveFieldValidation(field: string, rule: string) {
    return gv(field, rule as keyof GenFieldValidation)
  }

  const { ready: viewReady, view } = useGenView(props.businessName, props.apiModule, props.fileView)
  const propTreeSources = computed(() => props.treeSources)
  const { treeSources, ready: treeSourcesReady } = useGenTreeSources(view, gp, propTreeSources)
  const dictTypes = computed(() => collectDictTypes(view.value))
  const { getDict, ready: dictReady } = useDict(dictTypes)
  const { translateModule, translateBusinessType } = useOperationLogI18n()

  const readOnly = computed(
    () => view.value.readOnly === true || props.readOnly === true || demoStore.isDemo,
  )
  const customForm = computed(() => props.customForm === true)
  const statusChangeFn = computed(() => props.statusChange)
  const hasRowActionsSlot = computed(() => Boolean(slots['row-actions']))

  const fixedQueryRef = computed(() => props.fixedQuery)
  const omitQueryFieldsRef = computed(() => props.omitQueryFields)

  const list = useGenCrudList({
    api: props.api,
    view,
    selectable: props.selectable,
    onSelectionChange: (rows) => emit('selection-change', rows),
    fixedQuery: fixedQueryRef,
    omitQueryFields: omitQueryFieldsRef,
    clearQueryOnFixedChange: props.clearQueryOnFixedChange,
  })

  const formBase = useGenCrudForm({
    api: props.api,
    view,
    gp,
    customForm,
    loadPage: list.loadPage,
    onOpenForm: (row) => emit('open-form', row),
  })

  function bindEntityFormRef(ref: InstanceType<typeof GenEntityForm> | undefined) {
    formBase.entityFormRef.value = ref
  }

  const form = { ...formBase, bindEntityFormRef }

  function clearSelection() {
    tablePanelRef.value?.clearSelection()
    list.selectedRows.value = []
  }

  const ext = useGenCrudExtensions({
    view,
    perm: props.perm,
    api: props.api,
    businessName: props.businessName,
    apiModule: props.apiModule,
    i18nFiles: props.i18nFiles,
    readOnly,
    gp,
    actionHandlers: props.actionHandlers,
    rowActionVisible: props.rowActionVisible,
    loadPage: list.loadPage,
    openForm: form.openForm,
    handleDelete: form.handleDelete,
    selectedRows: list.selectedRows,
    clearSelection,
    selectionFilterPreset: list.selectionFilterPreset,
  })

  const showActionsColumn = computed(
    () =>
      hasRowActionsSlot.value ||
      ext.declaredRowActions.value.length > 0 ||
      ext.tableActions.value.edit ||
      ext.tableActions.value.delete,
  )

  const actionsColumnWidth = computed(
    () => view.value.table?.actions?.width ?? (ext.declaredRowActions.value.length > 2 ? 280 : 160),
  )

  function formatAuditI18n(category: 'modules' | 'businessTypes' | undefined, value?: string) {
    if (!value) return '—'
    if (category === 'businessTypes') return translateBusinessType(value)
    return translateModule(value)
  }

  async function onStatusSwitch(
    row: Record<string, unknown>,
    col: GenViewTableField,
    status: string,
  ) {
    if (!statusChangeFn.value) return
    const previous = String(row[col.field] ?? '')
    row[col.field] = status
    try {
      await statusChangeFn.value(row, status)
    } catch {
      row[col.field] = previous
    }
  }

  const ctx: GenCrudContext = {
    genReady,
    viewReady,
    dictReady,
    treeSourcesReady,
    gp,
    gf,
    formRules,
    resolveFieldValidation,
    view,
    readOnly,
    customForm,
    perm: props.perm,
    api: props.api,
    businessName: props.businessName,
    apiModule: props.apiModule,
    getDict,
    treeSources,
    list,
    form,
    ext,
    showSearch,
    showActionsColumn,
    actionsColumnWidth,
    hasRowActionsSlot,
    statusChange: props.statusChange,
    formatAuditI18n,
    clearSelection,
    onStatusSwitch,
    tablePanelRef,
  }

  provide(GEN_CRUD_CTX, ctx)

  return ctx
}
