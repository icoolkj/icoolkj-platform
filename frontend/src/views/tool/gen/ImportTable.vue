<template>
  <el-dialog
    v-model="visible"
    :title="gp('importDialogTitle')"
    width="920px"
    append-to-body
    destroy-on-close
    class="import-dialog"
    @open="handleOpen"
  >
    <el-alert
      :title="gp('importDialogTip')"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <el-form :model="query" inline @submit.prevent>
      <el-form-item :label="gp('tableName')">
        <el-input
          v-model="query.tableName"
          :placeholder="gp('tableName')"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="gp('tableComment')">
        <el-input
          v-model="query.tableComment"
          :placeholder="gp('tableComment')"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">
          {{ t('common.search') }}
        </el-button>
        <el-button :icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <div class="dialog-table-wrap">
      <el-table
        v-loading="loading"
        :data="tableList"
        stripe
        border
        height="400"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column
          :label="t('common.index')"
          type="index"
          width="60"
          align="center"
          :index="indexMethod"
        />
        <el-table-column
          :label="gp('tableName')"
          prop="tableName"
          min-width="200"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span class="mono">{{ row.tableName }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="gp('tableComment')"
          prop="tableComment"
          min-width="220"
          show-overflow-tooltip
        />
      </el-table>
    </div>

    <div class="table-footer">
      <Pagination
        v-show="total > 0"
        v-model:page="query.pageNo"
        v-model:limit="query.pageSize"
        :total="total"
        @pagination="loadList"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <span class="selected-tip">
          {{ gp('selectedTables', { count: selected.length }) }}
        </span>
        <div>
          <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
          <el-button
            type="primary"
            :disabled="!selected.length"
            :loading="importing"
            @click="handleImport"
          >
            {{ gp('confirmImport') }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToolGenI18n } from '@/composables/useToolGenI18n'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { importGenTable, pageDbTable } from '@/api/tool/gen'
import type { GenTable } from '@/types/tool/gen'

const { t } = useI18n()
const { gp } = useToolGenI18n()

const visible = defineModel<boolean>({ default: false })
const emit = defineEmits<{ ok: [] }>()

const loading = ref(false)
const importing = ref(false)
const tableList = ref<GenTable[]>([])
const selected = ref<GenTable[]>([])
const total = ref(0)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  tableName: '',
  tableComment: '',
})

function indexMethod(index: number) {
  return (query.pageNo - 1) * query.pageSize + index + 1
}

function handleOpen() {
  selected.value = []
  query.pageNo = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const res = await pageDbTable(query)
    tableList.value = res.data.data.list
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  query.pageNo = 1
  loadList()
}

function resetQuery() {
  query.tableName = ''
  query.tableComment = ''
  handleQuery()
}

function handleSelectionChange(rows: GenTable[]) {
  selected.value = rows
}

async function handleImport() {
  importing.value = true
  try {
    const names = selected.value.map((item) => item.tableName).join(',')
    await importGenTable(names)
    ElMessage.success(gp('importSuccess', { count: selected.value.length }))
    visible.value = false
    emit('ok')
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}
.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.selected-tip {
  font-size: 13px;
  color: var(--ic-text-secondary);
}
</style>
