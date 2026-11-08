import { computed, onMounted, reactive, ref, watch, type ComputedRef, type Ref } from 'vue'
import type { GenCrudApi } from '@/types/gen/crudApi'
import type { GenViewSchema } from '@/types/tool/genView'
import { evaluateSelectionFilter } from '@/composables/useGenRowActionRules'

export type UseGenCrudListOptions = {
  api: GenCrudApi
  view: ComputedRef<GenViewSchema>
  selectable?: boolean
  onSelectionChange?: (rows: Record<string, unknown>[]) => void
  /** Always merged into page query (e.g. tree-list sidebar filter) */
  fixedQuery?: Ref<Record<string, unknown> | undefined> | ComputedRef<Record<string, unknown> | undefined>
  omitQueryFields?: Ref<string[] | undefined> | ComputedRef<string[] | undefined>
  clearQueryOnFixedChange?: boolean
}

export function useGenCrudList(options: UseGenCrudListOptions) {
  const loading = ref(false)
  const total = ref(0)
  const dataList = ref<Record<string, unknown>[]>([])
  const selectedRows = ref<Record<string, unknown>[]>([])
  const queryParams = reactive<{ pageNo: number; pageSize: number; [key: string]: unknown }>({
    pageNo: 1,
    pageSize: 10,
  })

  const queryFields = computed(() => {
    const fields = options.view.value.query?.fields ?? []
    const omit = new Set(options.omitQueryFields?.value ?? [])
    return fields.filter((f) => !omit.has(f.field))
  })
  const tableFields = computed(() => options.view.value.table?.fields ?? [])

  const batchDeleteToolbarActions = computed(
    () =>
      options.view.value.extensions?.toolbarActions?.filter((a) => a.builtin === 'batchDelete') ??
      [],
  )

  const effectiveSelectable = computed(
    () => options.selectable === true || options.view.value.page?.selectable === true,
  )

  const selectionFilterPreset = computed(
    () =>
      options.view.value.page?.selectionFilter ??
      batchDeleteToolbarActions.value[0]?.selectionFilter,
  )

  function rowSelectable(row: Record<string, unknown>) {
    return evaluateSelectionFilter(selectionFilterPreset.value, row)
  }

  const selectedDeletableIds = computed(() =>
    selectedRows.value
      .filter((row) => rowSelectable(row))
      .map((row) => String(row.id))
      .filter(Boolean),
  )

  async function loadPage() {
    loading.value = true
    try {
      const params = { ...queryParams, ...(options.fixedQuery?.value ?? {}) }
      const res = (await options.api.page(params)) as {
        data: { data: { list: Record<string, unknown>[]; total: number } }
      }
      dataList.value = res.data.data.list
      total.value = res.data.data.total
    } finally {
      loading.value = false
    }
  }

  function handleQuery() {
    queryParams.pageNo = 1
    void loadPage()
  }

  function resetQuery() {
    for (const field of queryFields.value) {
      queryParams[field.field] = undefined
    }
    handleQuery()
  }

  function handleSelectionChange(rows: Record<string, unknown>[]) {
    selectedRows.value = rows
    options.onSelectionChange?.(rows)
  }

  if (options.fixedQuery) {
    watch(
      options.fixedQuery,
      () => {
        if (options.clearQueryOnFixedChange) {
          for (const field of options.view.value.query?.fields ?? []) {
            if (field.field in queryParams && field.field !== 'pageNo' && field.field !== 'pageSize') {
              delete queryParams[field.field]
            }
          }
        }
        queryParams.pageNo = 1
        void loadPage()
      },
      { deep: true },
    )
  }

  onMounted(() => {
    if (options.fixedQuery?.value) {
      Object.assign(queryParams, options.fixedQuery.value)
    }
    void loadPage()
  })

  return {
    loading,
    total,
    dataList,
    selectedRows,
    selectedDeletableIds,
    queryParams,
    queryFields,
    tableFields,
    effectiveSelectable,
    rowSelectable,
    selectionFilterPreset,
    loadPage,
    handleQuery,
    resetQuery,
    handleSelectionChange,
  }
}

export type GenCrudListReturn = ReturnType<typeof useGenCrudList>
