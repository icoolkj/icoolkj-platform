<!--
  P05 形态积木 — 左树右表（侧栏树筛选 + 右侧标准 CRUD）。
  框架映射：layout tree-list → 本组件。
  内部嵌套 GenPatternStandardCrud 作为右侧列表积木。
-->
<template>
  <div
    v-loading="!genReady || !viewReady || !treeSourcesReady"
    class="gen-tree-list-page"
  >
    <GenListLayout
      :title="gp('title')"
      :description="gp('description')"
      :show-search="false"
      :show-right-toolbar="false"
    >
      <el-row :gutter="12" class="gen-tree-list-page__row">
        <el-col :span="sidebarSpan" class="gen-tree-list-page__sidebar">
          <el-card shadow="never" class="tree-list-sidebar-card">
            <el-tree
              ref="treeRef"
              :data="sidebarTree"
              :props="treeProps"
              :node-key="valueField"
              highlight-current
              :default-expand-all="treeListConfig.defaultExpandAll !== false"
              @node-click="handleTreeNodeClick"
            />
          </el-card>
        </el-col>
        <el-col :span="mainSpan" class="gen-tree-list-page__main">
          <GenPatternStandardCrud
            ref="crudRef"
            hide-header
            :file-view="fileView"
            :business-name="businessName"
            :api-module="apiModule"
            :i18n-files="i18nFiles"
            :perm="perm"
            :api="api"
            :read-only="readOnly"
            :tree-sources="treeSources"
            :selectable="selectable"
            :custom-form="customForm"
            :status-change="statusChange"
            :action-handlers="actionHandlers"
            :row-action-visible="rowActionVisible"
            :fixed-query="activeFilter"
            :omit-query-fields="omitQueryFields"
            :clear-query-on-fixed-change="true"
            @selection-change="(rows) => emit('selection-change', rows)"
            @open-form="(row) => emit('open-form', row)"
          >
            <template v-for="(_, slotName) in slots" #[slotName]="slotData">
              <slot :name="slotName" v-bind="slotData ?? {}" />
            </template>
          </GenPatternStandardCrud>
        </el-col>
      </el-row>
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useSlots } from 'vue'
import type { ElTree } from 'element-plus'
import { createGenPageExpose } from '@/components/gen/patterns/expose'
import type { GenPageEmits, GenPatternTreeListProps } from '@/components/gen/patterns/types'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import GenPatternStandardCrud from '@/components/gen/patterns/GenPatternStandardCrud.vue'
import { useGenI18n } from '@/composables/useGenI18n'
import { useGenView } from '@/composables/useGenView'
import { useGenTreeSources } from '@/composables/useGenTreeSources'
import { TREE_ROOT_PARENT_ID } from '@/constants/flags'

const props = defineProps<GenPatternTreeListProps>()

const emit = defineEmits<GenPageEmits>()

const slots = useSlots()
const crudRef = ref<InstanceType<typeof GenPatternStandardCrud>>()
const treeRef = ref<InstanceType<typeof ElTree>>()
const selectedNodeKey = ref<string | undefined>()

const {
  ready: genReady,
  gp,
} = useGenI18n(props.businessName, props.apiModule, props.i18nFiles)
const { ready: viewReady, view } = useGenView(props.businessName, props.apiModule, props.fileView)
const propTreeSources = computed(() => props.treeSources)
const { treeSources: configuredTreeSources, ready: treeSourcesReady } = useGenTreeSources(
  view,
  gp,
  propTreeSources,
)

const treeListConfig = computed(() => view.value.treeList!)
const sidebarSpan = computed(() => treeListConfig.value.sidebarSpan ?? 5)
const mainSpan = computed(() => treeListConfig.value.mainSpan ?? 19)

const treeSourceKey = computed(() => treeListConfig.value.treeSource)
const treeSourceConfig = computed(
  () => view.value.extensions?.treeSources?.[treeSourceKey.value],
)
const filterField = computed(() => treeListConfig.value.filterField)
const valueField = computed(
  () => treeListConfig.value.valueField ?? treeSourceConfig.value?.valueField ?? 'id',
)
const labelField = computed(
  () => treeListConfig.value.labelField ?? treeSourceConfig.value?.labelField ?? 'label',
)

const sidebarTree = computed(() => configuredTreeSources.value[treeSourceKey.value] ?? [])

const treeProps = computed(() => ({
  label: labelField.value,
  children: 'children',
}))

const rootValue = computed(
  () => treeListConfig.value.rootValue ?? TREE_ROOT_PARENT_ID,
)

const activeFilter = computed(() => {
  if (selectedNodeKey.value === undefined) return {}
  if (
    treeListConfig.value.clearFilterOnRoot !== false &&
    selectedNodeKey.value === rootValue.value
  ) {
    return {}
  }
  return { [filterField.value]: selectedNodeKey.value }
})

const omitQueryFields = computed(() => {
  if (treeListConfig.value.omitFromQuery === false) return []
  return [filterField.value]
})

function handleTreeNodeClick(data: Record<string, unknown>) {
  const raw = data[valueField.value]
  selectedNodeKey.value = raw != null ? String(raw) : undefined
}

defineExpose(
  createGenPageExpose({
    openForm: (row?: Record<string, unknown>) => crudRef.value?.openForm(row),
    loadPage: () => crudRef.value?.loadPage(),
    clearSelection: () => crudRef.value?.clearSelection(),
    get form() {
      return crudRef.value?.form
    },
    get queryParams() {
      return crudRef.value?.queryParams
    },
    get selectedRows() {
      return crudRef.value?.selectedRows
    },
  }),
)
</script>

<style scoped>
.gen-tree-list-page__row {
  align-items: stretch;
}

.tree-list-sidebar-card :deep(.el-card__body) {
  padding: 8px 4px;
  max-height: calc(100vh - 220px);
  overflow: auto;
}

.gen-tree-list-page__main :deep(.page-container) {
  padding: 0;
}

.gen-tree-list-page__main :deep(.search-card),
.gen-tree-list-page__main :deep(.table-card) {
  border: none;
  box-shadow: none;
  background: transparent;
}
</style>
