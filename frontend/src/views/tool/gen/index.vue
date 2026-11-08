<!-- 代码生成 — gen-page-shell + GenListLayout（导入/预览等工具交互） -->
<template>
  <div class="gen-page-shell">
    <GenListLayout
      :title="gp('title')"
      :description="gp('description')"
      :show-search="showSearch"
      @refresh="loadPage"
      @toggle-search="showSearch = !showSearch"
    >
      <template #search>
        <el-form :model="queryParams" inline @submit.prevent>
          <el-form-item :label="gp('tableName')">
            <el-input
              v-model="queryParams.tableName"
              :placeholder="t('common.pleaseInput', { field: gp('tableName') })"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item :label="gp('tableComment')">
            <el-input
              v-model="queryParams.tableComment"
              :placeholder="t('common.pleaseInput', { field: gp('tableComment') })"
              clearable
              style="width: 200px"
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
      </template>

      <template #toolbar>
        <el-button
          type="primary"
          :icon="Download"
          :disabled="multiple"
          @click="handleBatchGen"
          v-hasPermi="[PERM.code]"
        >
          {{ gp('batchGen') }}
        </el-button>
        <el-button
          type="primary"
          plain
          :icon="Upload"
          @click="importVisible = true"
          v-hasPermi="[PERM.import]"
        >
          {{ gp('importTable') }}
        </el-button>
        <el-button
          type="success"
          plain
          :icon="Edit"
          :disabled="single"
          @click="handleEdit()"
          v-hasPermi="[PERM.edit]"
        >
          {{ gp('editConfig') }}
        </el-button>
        <el-button
          type="danger"
          plain
          :icon="Delete"
          :disabled="multiple"
          @click="handleDelete()"
          v-hasPermi="[PERM.remove]"
        >
          {{ t('common.delete') }}
        </el-button>
      </template>

      <el-table
        v-loading="loading"
        :data="tableList"
        stripe
        border
        highlight-current-row
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" fixed="left" />
        <el-table-column :label="t('common.index')" type="index" width="60" align="center" />
        <el-table-column
          :label="gp('tableName')"
          prop="tableName"
          min-width="150"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span class="mono">{{ row.tableName }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="gp('tableComment')"
          prop="tableComment"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column
          :label="gp('className')"
          prop="className"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          :label="gp('backendModule')"
          prop="backendModule"
          width="100"
          align="center"
        />
        <el-table-column
          :label="t('common.createdAt')"
          prop="createdAt"
          width="170"
          align="center"
        />
        <el-table-column
          :label="t('common.updatedAt')"
          prop="updatedAt"
          width="170"
          align="center"
        />
        <el-table-column :label="t('common.actions')" align="center" width="280" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="handlePreview(scope.row)"
              v-hasPermi="[PERM.preview]"
            >
              {{ t('common.preview') }}
            </el-button>
            <el-button link type="primary" @click="handleEdit(scope.row)" v-hasPermi="[PERM.edit]">
              {{ t('common.edit') }}
            </el-button>
            <el-button link type="primary" @click="handleSync(scope.row)" v-hasPermi="[PERM.edit]">
              {{ t('common.sync') }}
            </el-button>
            <el-button link type="primary" @click="handleGen(scope.row)" v-hasPermi="[PERM.code]">
              {{ t('common.generate') }}
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row)"
              v-hasPermi="[PERM.remove]"
            >
              {{ t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <Pagination
          v-show="total > 0"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          :total="total"
          @pagination="loadPage"
        />
      </template>
    </GenListLayout>

    <ImportTable v-model="importVisible" @ok="loadPage" />

    <el-dialog
      v-model="previewVisible"
      :title="gp('previewTitle')"
      width="82%"
      top="4vh"
      destroy-on-close
      class="preview-dialog"
    >
      <el-tabs v-model="previewTab" type="border-card">
        <el-tab-pane
          v-for="(code, path) in previewMap"
          :key="path"
          :label="shortPath(path)"
          :name="path"
        >
          <div class="preview-toolbar">
            <span class="preview-path">{{ path }}</span>
            <el-button type="primary" plain @click="copyCode(code)">
              {{ t('common.copyCode') }}
            </el-button>
          </div>
          <pre class="code-block"><code class="hljs" v-html="highlight(path, code)" /></pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToolGenI18n } from '@/composables/useToolGenI18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Download, Edit, Refresh, Search, Upload } from '@element-plus/icons-vue'
import hljs from 'highlight.js/lib/core'
import java from 'highlight.js/lib/languages/java'
import typescript from 'highlight.js/lib/languages/typescript'
import xml from 'highlight.js/lib/languages/xml'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import Pagination from '@/components/Pagination.vue'
import { isGenCustomPath } from '@/constants/flags'
import ImportTable from './ImportTable.vue'
import {
  PERM_GEN as PERM,
  deleteGenTable,
  genCodeToDisk,
  pageGenTable,
  previewGenTable,
  synchGenDb,
} from '@/api/tool/gen'
import { downloadZip } from '@/utils/download'
import type { GenTable } from '@/types/tool/gen'

const { t } = useI18n()
const { gp } = useToolGenI18n()

hljs.registerLanguage('java', java)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('xml', xml)

const router = useRouter()
const loading = ref(false)
const showSearch = ref(true)
const importVisible = ref(false)
const previewVisible = ref(false)
const previewTab = ref('')
const previewMap = ref<Record<string, string>>({})
const tableList = ref<GenTable[]>([])
const total = ref(0)
const ids = ref<string[]>([])
const names = ref<string[]>([])
const single = ref(true)
const multiple = ref(true)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  tableName: '',
  tableComment: '',
})

async function loadPage() {
  loading.value = true
  try {
    const res = await pageGenTable(queryParams)
    tableList.value = res.data.data.list
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNo = 1
  loadPage()
}

function resetQuery() {
  queryParams.tableName = ''
  queryParams.tableComment = ''
  handleQuery()
}

function handleSelectionChange(selection: GenTable[]) {
  ids.value = selection.map((item) => item.id!).filter(Boolean)
  names.value = selection.map((item) => item.tableName)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleEdit(row?: GenTable) {
  const id = row?.id ?? ids.value[0]
  if (!id) return
  router.push(`/tool/gen-edit/${id}`)
}

async function handleDelete(row?: GenTable) {
  const targetIds = row?.id ? [row.id] : ids.value
  if (!targetIds.length) return
  await ElMessageBox.confirm(
    gp('deleteConfirm', { count: targetIds.length }),
    t('common.tip'),
    { type: 'warning' },
  )
  await deleteGenTable(targetIds.join(','))
  ElMessage.success(gp('deleteSuccess'))
  await loadPage()
}

async function handleSync(row: GenTable) {
  await ElMessageBox.confirm(
    gp('syncConfirm', { name: row.tableName }),
    gp('syncTitle'),
    { type: 'info' },
  )
  await synchGenDb(row.tableName)
  ElMessage.success(gp('syncSuccess'))
}

async function handlePreview(row: GenTable) {
  const res = await previewGenTable(row.id!)
  previewMap.value = res.data.data
  previewTab.value = Object.keys(previewMap.value)[0] ?? ''
  previewVisible.value = true
}

async function handleGen(row: GenTable) {
  if (isGenCustomPath(row.genType)) {
    await ElMessageBox.confirm(
      gp('genCustomConfirm', { name: row.tableName }),
      t('common.tip'),
    )
    await genCodeToDisk(row.tableName)
    ElMessage.success(gp('genCustomSuccess'))
    return
  }
  await downloadZip(`/tool/gen/download/${row.tableName}`, `${row.tableName}_code.zip`)
}

async function handleBatchGen() {
  if (!names.value.length) return
  const selectedRows = tableList.value.filter((item) => names.value.includes(item.tableName))
  const customRows = selectedRows.filter((item) => isGenCustomPath(item.genType))
  if (customRows.length) {
    await ElMessageBox.confirm(
      gp('batchGenConfirm', { custom: customRows.length }),
      t('common.tip'),
    )
    for (const row of customRows) {
      await genCodeToDisk(row.tableName)
    }
  }
  const zipNames = selectedRows
    .filter((item) => !isGenCustomPath(item.genType))
    .map((item) => item.tableName)
  if (zipNames.length) {
    await downloadZip(`/tool/gen/batchGenCode?tables=${zipNames.join(',')}`, 'icoolkj_code.zip')
  }
  ElMessage.success(gp('batchGenSuccess'))
}

function shortPath(path: string) {
  if (path === '_OUTPUT_PATHS.md') return 'OUTPUT_PATHS'
  const parts = path.replace(/\\/g, '/').split('/')
  return parts.length > 3 ? `.../${parts.slice(-3).join('/')}` : path
}

function highlight(path: string, code: string) {
  const lang = path.endsWith('.java')
    ? 'java'
    : path.endsWith('.ts') || path.endsWith('.vue')
      ? 'typescript'
      : 'xml'
  return hljs.highlight(code, { language: lang }).value
}

async function copyCode(code: string) {
  await navigator.clipboard.writeText(code)
  ElMessage.success(t('common.copied'))
}

onMounted(loadPage)
</script>

<style scoped>
.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: #1677ff;
}
.preview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.preview-path {
  font-size: 12px;
  color: var(--ic-text-secondary);
  font-family: ui-monospace, monospace;
}
.code-block {
  max-height: 62vh;
  overflow: auto;
  margin: 0;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}
</style>
