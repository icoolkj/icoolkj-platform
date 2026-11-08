<template>
  <GenDataTable
    ref="dataTableRef"
    :data="dataList"
    :columns="tableFields"
    :gf="gf"
    :get-dict="getDict"
    :selectable="effectiveSelectable"
    :row-selectable="rowSelectable"
    :show-index="true"
    :index-label="t('common.index')"
    :show-actions="showActionsColumn"
    :actions-label="t('common.actions')"
    :actions-width="actionsColumnWidth"
    :switch-disabled="readOnly || !statusChange"
    :format-audit="formatAuditI18n"
    @selection-change="handleSelectionChange"
    @status-switch="onStatusSwitch"
  >
    <template #row-actions="{ row }">
      <GenRowActions :row="row" />
      <slot name="row-actions" :row="row" />
    </template>
  </GenDataTable>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import GenDataTable from '@/components/gen/table/GenDataTable.vue'
import GenRowActions from '@/components/gen/panels/GenRowActions.vue'
import { useGenCrudContext } from '@/composables/gen/useGenCrudContext'

const { t } = useI18n()
const ctx = useGenCrudContext()

const dataList = ctx.list.dataList
const tableFields = ctx.list.tableFields
const effectiveSelectable = ctx.list.effectiveSelectable
const rowSelectable = ctx.list.rowSelectable
const handleSelectionChange = ctx.list.handleSelectionChange
const {
  gf,
  getDict,
  readOnly,
  statusChange,
  showActionsColumn,
  actionsColumnWidth,
  formatAuditI18n,
  onStatusSwitch,
  tablePanelRef,
} = ctx

const dataTableRef = ref<InstanceType<typeof GenDataTable>>()

function clearSelection() {
  dataTableRef.value?.clearSelection()
}

onMounted(() => {
  tablePanelRef.value = { clearSelection }
})
onBeforeUnmount(() => {
  tablePanelRef.value = undefined
})
</script>
