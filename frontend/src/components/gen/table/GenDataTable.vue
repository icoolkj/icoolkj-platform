<!--
  Gen 数据表格积木：按 view.table.fields 渲染列；行操作由父组件 #row-actions 或内置 edit/delete 提供。
-->
<template>
  <el-table
    ref="tableRef"
    :data="data"
    stripe
    border
    highlight-current-row
    v-bind="tableAttrs"
    @selection-change="onSelectionChange"
  >
    <el-table-column
      v-if="selectable"
      type="selection"
      width="48"
      align="center"
      :selectable="rowSelectable"
    />
    <el-table-column v-if="showIndex" :label="indexLabel" type="index" width="60" align="center" />
    <el-table-column
      v-for="col in columns"
      :key="col.field"
      :label="gf(col.field, 'label')"
      :prop="col.component === 'text' ? col.field : undefined"
      :min-width="col.minWidth"
      :width="col.width"
      :align="col.align"
      :show-overflow-tooltip="col.showOverflowTooltip"
    >
      <template v-if="col.component === 'dict-tag'" #default="{ row }">
        <DictTag :options="getDict(col.dictType!)" :value="row[col.field]" />
      </template>
      <template v-else-if="col.component === 'dict-tags'" #default="{ row }">
        <DictTag
          v-for="item in splitCsv(row[col.field])"
          :key="item"
          class="dict-tags-item"
          :options="getDict(col.dictType!)"
          :value="item"
        />
      </template>
      <template v-else-if="col.component === 'image'" #default="{ row }">
        <AuthImage
          v-if="row[col.imageField ?? col.field]"
          :src="String(row[col.imageField ?? col.field])"
          :download-scope="col.downloadScope ?? 'infra'"
          fit="cover"
          image-class="table-cover-thumb"
        />
        <span v-else class="text-muted">-</span>
      </template>
      <template v-else-if="col.component === 'tag'" #default="{ row }">
        <el-tag v-if="row[col.field]" size="small" type="info">{{ row[col.field] }}</el-tag>
        <span v-else class="text-muted">—</span>
      </template>
      <template v-else-if="col.component === 'file-thumb'" #default="{ row }">
        <FileThumb
          :file="{
            fileExt: String(row.fileExt ?? ''),
            contentType: String(row.contentType ?? ''),
          }"
        />
      </template>
      <template v-else-if="col.component === 'datetime'" #default="{ row }">
        {{ formatDateTime(row[col.field] as string) }}
      </template>
      <template v-else-if="col.component === 'audit-i18n'" #default="{ row }">
        {{ formatAudit?.(col.auditCategory, row[col.field] as string) ?? row[col.field] }}
      </template>
      <template v-else-if="col.component === 'switch'" #default="{ row }">
        <el-switch
          :model-value="String(row[col.field] ?? '')"
          :active-value="col.activeValue ?? STATUS_NORMAL"
          :inactive-value="col.inactiveValue ?? STATUS_DISABLE"
          :disabled="switchDisabled"
          @change="(value: string | number | boolean) => emit('status-switch', row, col, String(value))"
        />
      </template>
      <template v-else-if="col.component === 'icon'" #default="{ row }">
        <el-icon v-if="resolveIconComponent(row[col.field] as string)" :size="18">
          <component :is="resolveIconComponent(row[col.field] as string)" />
        </el-icon>
        <span v-else class="text-muted">-</span>
      </template>
    </el-table-column>
    <el-table-column
      v-if="showActions"
      :label="actionsLabel"
      align="center"
      :width="actionsWidth ?? 160"
      fixed="right"
    >
      <template #default="{ row }">
        <slot name="row-actions" :row="row" />
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { TableInstance } from 'element-plus'
import AuthImage from '@/components/AuthImage.vue'
import DictTag from '@/components/DictTag.vue'
import FileThumb from '@/components/FileThumb.vue'
import { STATUS_DISABLE, STATUS_NORMAL } from '@/constants/flags'
import { formatDateTime } from '@/utils/formatDateTime'
import { resolveIconComponent } from '@/utils/icon'
import type { DictOption } from '@/types/system/dict'
import type { GenViewTableField } from '@/types/tool/genView'

withDefaults(
  defineProps<{
    data: Record<string, unknown>[]
    columns: GenViewTableField[]
    gf: (field: string, key?: 'label' | 'placeholder' | 'placeholderReject') => string
    getDict: (dictType: string) => DictOption[]
    selectable?: boolean
    /** Per-row checkbox enable (Element Plus selection column) */
    rowSelectable?: (row: Record<string, unknown>) => boolean
    showIndex?: boolean
    indexLabel?: string
    showActions?: boolean
    actionsLabel?: string
    actionsWidth?: number
    switchDisabled?: boolean
    /** Extra attrs forwarded to el-table (row-key, tree-props, …) */
    tableAttrs?: Record<string, unknown>
    formatAudit?: (
      category: 'modules' | 'businessTypes' | undefined,
      value?: string,
    ) => string
  }>(),
  {
    selectable: false,
    showIndex: true,
    showActions: false,
    switchDisabled: true,
    indexLabel: '#',
    actionsLabel: 'Actions',
  },
)

const emit = defineEmits<{
  'selection-change': [rows: Record<string, unknown>[]]
  'status-switch': [row: Record<string, unknown>, col: GenViewTableField, status: string]
}>()

const tableRef = ref<TableInstance>()

function splitCsv(value: unknown): string[] {
  if (value == null || value === '') return []
  return String(value)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function onSelectionChange(rows: Record<string, unknown>[]) {
  emit('selection-change', rows)
}

function clearSelection() {
  tableRef.value?.clearSelection()
}

defineExpose({ tableRef, clearSelection })
</script>
