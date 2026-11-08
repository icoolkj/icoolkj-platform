<!--
  P03 形态积木 — 树形表格 CRUD。
  框架映射：layout tree-crud → 本组件。
-->
<template>
  <div v-loading="!genReady || !viewReady || !dictReady || !treeSourcesReady || loading">
    <GenListLayout
      :title="gp('title')"
      :description="gp('description')"
      :show-search="showSearch"
      @refresh="loadPage"
      @toggle-search="showSearch = !showSearch"
    >
      <template #search>
        <el-form :model="queryParams" inline @submit.prevent>
          <el-form-item
            v-for="field in queryFields"
            :key="field.field"
            :label="gf(field.field, 'label')"
          >
            <GenFormField
              v-model="queryParams[field.field]"
              :config="field"
              mode="query"
              :placeholder="gf(field.field, 'placeholder')"
              :get-dict="getDict"
              :get-field-options="getFieldOptions"
              @enter="handleQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleQuery">
              {{ t('common.search') }}
            </el-button>
            <el-button :icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template v-if="$slots['table-top']" #table-top>
        <slot name="table-top" />
      </template>

      <template #toolbar>
        <el-button
          v-if="!readOnly"
          type="primary"
          :icon="Plus"
          v-hasPermi="[perm.add]"
          @click="openForm()"
        >
          {{ t('common.add') }}
        </el-button>
        <GenExcelToolbarActions
          :actions="excelToolbarActions"
          :read-only="readOnly"
          :perm="perm"
          :api="api"
          :api-module="apiModule"
          :business-name="businessName"
          :export-query="queryParams"
          :label-resolver="gp"
          :resolve-field-validation="resolveFieldValidation"
          @imported="loadPage"
        />
        <el-button v-if="treeActions.expandCollapse" @click="expandAll">
          {{ t('common.expandAll') }}
        </el-button>
        <el-button v-if="treeActions.expandCollapse" @click="collapseAll">
          {{ t('common.collapseAll') }}
        </el-button>
      </template>

      <template #toolbar-extra>
        <slot name="toolbar-extra" />
      </template>

      <el-table
        ref="tableRef"
        :data="treeList"
        :row-key="treeConfig.rowKey ?? 'id'"
        :tree-props="{ children: 'children' }"
        :default-expand-all="treeConfig.defaultExpandAll"
        stripe
        border
        highlight-current-row
      >
        <el-table-column
          v-for="col in tableFields"
          :key="col.field"
          :label="gf(col.field, 'label')"
          :prop="col.component === 'text' ? col.field : undefined"
          :min-width="col.minWidth"
          :width="col.width"
          :align="col.align"
          :show-overflow-tooltip="col.showOverflowTooltip"
        >
          <template v-if="col.component === 'dict-tag'" #default="{ row }">
            <DictTag :options="resolveColumnOptions(col)" :value="row[col.field]" />
          </template>
          <template v-else-if="col.component === 'icon'" #default="{ row }">
            <el-icon v-if="resolveIconComponent(row[col.field] as string)" :size="18">
              <component :is="resolveIconComponent(row[col.field] as string)" />
            </el-icon>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="!readOnly && showRowActions"
          :label="t('common.actions')"
          align="center"
          :width="treeActions.width ?? 220"
          fixed="right"
        >
          <template #default="{ row }">
            <slot
              name="row-actions"
              :row="row"
              :open-form="openForm"
              :open-add-child="openAddChild"
            >
              <el-button
                v-if="treeActions.addChild && canAddChild(row)"
                link
                type="primary"
                v-hasPermi="[perm.add]"
                @click="openAddChild(row)"
              >
                {{ gp('addChild') }}
              </el-button>
              <el-button
                v-if="treeActions.edit"
                link
                type="primary"
                v-hasPermi="[perm.update]"
                @click="openForm(row)"
              >
                {{ t('common.edit') }}
              </el-button>
              <el-button
                v-if="treeActions.delete"
                link
                type="danger"
                v-hasPermi="[perm.delete]"
                @click="handleDelete(row)"
              >
                {{ t('common.delete') }}
              </el-button>
            </slot>
          </template>
        </el-table-column>
      </el-table>
    </GenListLayout>

    <GenFormShell
      v-model="formVisible"
      :title="formTitle"
      :width="formConfig.dialogWidth ?? 620"
      :close-on-click-modal="formConfig.closeOnClickModal ?? false"
      :close-on-press-escape="formConfig.closeOnPressEscape ?? false"
    >
      <GenEntityForm
        ref="entityFormRef"
        :model="form"
        :fields="visibleFormFields"
        :rules="formRules"
        :label-width="`${formConfig.labelWidth ?? 110}px`"
        :gf="gf"
        :gp="gp"
        :get-dict="getDict"
        :get-field-options="getFieldOptions"
        :tree-sources="treeSourcesForForm"
        :is-field-disabled="isFieldDisabled"
      />
      <template #footer>
        <el-button @click="formVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </GenFormShell>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, useSlots, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type TableInstance } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import DictTag from '@/components/DictTag.vue'
import GenEntityForm from '@/components/gen/fields/GenEntityForm.vue'
import GenFormField from '@/components/gen/fields/GenFormField.vue'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import GenExcelToolbarActions from '@/components/gen/excel/GenExcelToolbarActions.vue'
import { useDict } from '@/composables/useDict'
import { useExcelImportExport } from '@/composables/useExcelImportExport'
import { useGenI18n } from '@/composables/useGenI18n'
import { collectDictTypes, useGenView } from '@/composables/useGenView'
import { useGenTreeSources } from '@/composables/useGenTreeSources'
import { useDemoStore } from '@/stores/demo'
import { TREE_ROOT_PARENT_ID } from '@/constants/flags'
import { buildGenTree, findGenTreeNode, sortGenTree, walkGenTree } from '@/utils/genTree'
import { resolveIconComponent } from '@/utils/icon'
import type { GenFieldValidation } from '@/types/tool/genI18n'
import type { GenViewFormField, GenViewTableField } from '@/types/tool/genView'
import type { DictOption } from '@/types/system/dict'
import { createGenPageExpose } from '@/components/gen/patterns/expose'
import type { GenPatternTreeCrudProps } from '@/components/gen/patterns/types'

const props = defineProps<GenPatternTreeCrudProps>()

const { t, locale } = useI18n()
const demoStore = useDemoStore()
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
const { treeSources: configuredTreeSources, ready: treeSourcesReady } = useGenTreeSources(
  view,
  gp,
  propTreeSources,
)
const dictTypes = computed(() => collectDictTypes(view.value))
const { getDict, ready: dictReady } = useDict(dictTypes)

function resolveColumnOptions(col: GenViewTableField): DictOption[] {
  const fromField = props.getFieldOptions?.(col.field)
  if (fromField?.length) return fromField
  if (col.dictType) return getDict(col.dictType)
  return []
}

const loading = ref(false)
const submitting = ref(false)
const showSearch = ref(true)
const formVisible = ref(false)
const flatList = ref<Record<string, unknown>[]>([])
const treeList = ref<Record<string, unknown>[]>([])
const form = ref<Record<string, unknown>>({})
const entityFormRef = ref<InstanceType<typeof GenEntityForm>>()
const tableRef = ref<TableInstance>()

const treeConfig = computed(() => view.value.tree ?? { parentField: 'parentId' })
const parentField = computed(() => treeConfig.value.parentField)
const rootValue = computed(() => treeConfig.value.rootValue ?? TREE_ROOT_PARENT_ID)

const queryParams = reactive<{ pageNo: number; pageSize: number; [key: string]: unknown }>({
  pageNo: 1,
  pageSize: treeConfig.value.pageSize ?? 10000,
})

const queryFields = computed(() => {
  const fields = view.value.query?.fields ?? []
  const omit = new Set(props.omitQueryFields ?? [])
  return fields.filter((f) => !omit.has(f.field))
})
const tableFields = computed(() => {
  const fields = view.value.table?.fields ?? []
  const omit = new Set(props.omitTableFields ?? [])
  return fields.filter((f) => !omit.has(f.field))
})
const readOnly = computed(
  () => view.value.readOnly === true || props.readOnly === true || demoStore.isDemo,
)
const excelToolbarActions = computed(() => {
  const fromView = view.value.extensions?.toolbarActions ?? []
  if (fromView.some((a) => a.builtin === 'export' || a.builtin === 'import')) {
    return fromView
  }
  return useExcelImportExport({
    businessName: props.businessName,
    apiModule: props.apiModule,
    api: props.api,
    perm: props.perm,
  }).actions
})
const treeActions = computed(() => ({
  edit:
    !readOnly.value &&
    (view.value.tree?.actions?.edit ?? view.value.table?.actions?.edit !== false),
  delete:
    !readOnly.value &&
    (view.value.tree?.actions?.delete ?? view.value.table?.actions?.delete !== false),
  addChild: !readOnly.value && view.value.tree?.actions?.addChild === true,
  expandCollapse: view.value.tree?.actions?.expandCollapse === true,
  width: view.value.tree?.actions?.width ?? view.value.table?.actions?.width,
  addChildExclude: view.value.tree?.actions?.addChildExclude,
}))
const slots = useSlots()
const showRowActions = computed(
  () =>
    treeActions.value.edit ||
    treeActions.value.delete ||
    treeActions.value.addChild ||
    Boolean(slots['row-actions']),
)

const formConfig = computed(() => view.value.form ?? {})
const formTitle = computed(() => (form.value.id ? gp('editTitle') : gp('addTitle')))

const selfTreeOptions = computed(() => {
  const labelField = treeConfig.value.labelField ?? 'name'
  const rootLabel = gp('treeRoot')
  return [
    {
      id: rootValue.value,
      [labelField]: rootLabel,
      children: treeList.value,
    },
  ]
})

const treeSourcesForForm = computed(() => ({
  self: selfTreeOptions.value,
  ...configuredTreeSources.value,
}))

const effectiveCreateDefaults = computed(
  () => props.createDefaults ?? view.value.form?.createDefaults ?? {},
)

const visibleFormFields = computed(() => {
  const fields = view.value.form?.fields ?? []
  if (form.value.id) {
    return fields.filter((f) => f.edit !== false && !f.insertOnly)
  }
  return fields.filter((f) => f.insert !== false)
})

function rebuildTree() {
  const source = props.treeRowFilter ? flatList.value.filter(props.treeRowFilter) : flatList.value
  treeList.value = buildGenTree(source, parentField.value, rootValue.value, {
    dropOrphans: props.dropOrphanRows ?? false,
  })
  sortGenTree(treeList.value, treeConfig.value.sortField ?? 'sortNo')
}

let loadPageSeq = 0

async function loadPage() {
  const seq = ++loadPageSeq
  loading.value = true
  try {
    queryParams.pageSize = treeConfig.value.pageSize ?? 10000
    const params = { ...queryParams, ...(props.fixedQuery ?? {}) }
    const res = (await props.api.page(params)) as {
      data: { data: { list: Record<string, unknown>[] } }
    }
    if (seq !== loadPageSeq) return
    flatList.value = res.data.data.list
    rebuildTree()
  } finally {
    if (seq === loadPageSeq) {
      loading.value = false
    }
  }
}

function handleQuery() {
  loadPage()
}

function resetQuery() {
  for (const field of queryFields.value) {
    queryParams[field.field] = undefined
  }
  if (props.fixedQuery) {
    Object.assign(queryParams, props.fixedQuery)
  }
  handleQuery()
}

function canAddChild(row: Record<string, unknown>) {
  const exclude = treeActions.value.addChildExclude
  if (!exclude) return true
  const val = row[exclude.field]
  return !exclude.values.includes(String(val))
}

function expandAll() {
  walkGenTree(treeList.value as Parameters<typeof walkGenTree>[0], (row) => {
    if ((row as { children?: unknown[] }).children?.length) {
      tableRef.value?.toggleRowExpansion(row, true)
    }
  })
}

function collapseAll() {
  walkGenTree(treeList.value as Parameters<typeof walkGenTree>[0], (row) => {
    if ((row as { children?: unknown[] }).children?.length) {
      tableRef.value?.toggleRowExpansion(row, false)
    }
  })
}

function expandAncestors(row: Record<string, unknown>) {
  const map = new Map(flatList.value.map((item) => [String(item.id), item]))
  let pid = row[parentField.value]
  while (pid && String(pid) !== String(rootValue.value)) {
    const parent = findGenTreeNode(
      treeList.value as Parameters<typeof findGenTreeNode>[0],
      String(pid),
    )
    if (parent && (parent as { children?: unknown[] }).children?.length) {
      tableRef.value?.toggleRowExpansion(parent, true)
    }
    pid = map.get(String(pid))?.[parentField.value]
  }
}

function resetForm(defaults?: Record<string, unknown>) {
  form.value = { ...defaults }
  entityFormRef.value?.resetFields()
}

function openAddChild(parent: Record<string, unknown>) {
  const node = findGenTreeNode(
    treeList.value as Parameters<typeof findGenTreeNode>[0],
    String(parent.id),
  )
  if (node && (node as { children?: unknown[] }).children?.length) {
    tableRef.value?.toggleRowExpansion(node, true)
  }
  const defaults: Record<string, unknown> = {
    [parentField.value]: parent.id ?? rootValue.value,
    ...(props.getAddChildDefaults?.(parent) ?? {}),
  }
  resetForm(defaults)
  formVisible.value = true
}

function openForm(row?: Record<string, unknown>) {
  resetForm()
  if (row?.id) {
    expandAncestors(row)
    if (!props.api.get) return
    props.api.get(String(row.id)).then((raw) => {
      const res = raw as { data: { data: Record<string, unknown> } }
      form.value = { ...res.data.data }
      formVisible.value = true
    })
    return
  }
  form.value[parentField.value] = rootValue.value
  Object.assign(form.value, effectiveCreateDefaults.value)
  formVisible.value = true
}

async function submitForm() {
  if (!entityFormRef.value) return
  const valid = await entityFormRef.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    if (form.value.id) {
      if (!props.api.update) return
      await props.api.update(form.value)
    } else {
      if (!props.api.create) return
      await props.api.create(form.value)
    }
    ElMessage.success(t('common.success'))
    formVisible.value = false
    await loadPage()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: Record<string, unknown>) {
  if (!row.id) return
  if (!props.api.delete) return
  await ElMessageBox.confirm(gp('deleteConfirm', { count: 1 }), t('common.tip'))
  await props.api.delete(String(row.id))
  ElMessage.success(t('common.success'))
  await loadPage()
}

function isFieldDisabled(field: GenViewFormField) {
  return Boolean(field.disabledOnEdit && form.value.id)
}

defineExpose(createGenPageExpose({ openForm, openAddChild, loadPage, rebuildTree }))

onMounted(() => {
  if (props.fixedQuery) {
    Object.assign(queryParams, props.fixedQuery)
  }
  loadPage()
})
watch(
  () => props.fixedQuery,
  (next, prev) => {
    if (!next) return
    const changed =
      !prev || JSON.stringify(prev) !== JSON.stringify(next)
    if (changed && props.clearQueryOnFixedChange) {
      for (const field of queryFields.value) {
        queryParams[field.field] = undefined
      }
    }
    Object.assign(queryParams, next)
    if (changed) {
      loadPage()
    }
  },
  { deep: true },
)
watch(locale, loadPage)
</script>
