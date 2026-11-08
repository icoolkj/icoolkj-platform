<template>
  <el-alert type="info" :closable="false" show-icon class="form-block-tip">
    {{ gp('tableWorkflowHint') }}
  </el-alert>

  <el-tabs v-model="tableMode" class="table-wizard-tabs">
    <el-tab-pane :label="gp('tableModeCreate')" name="create">
      <el-form label-width="120px" style="max-width: 960px">
        <el-form-item :label="gp('apiModule')">
          <el-select v-model="createApiModule" style="width: 240px">
            <el-option
              v-for="mod in moduleOptions"
              :key="mod.apiModule"
              :label="t(mod.labelKey)"
              :value="mod.apiModule"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="gp('createTablePrompt')" required>
          <el-input
            v-model="createTablePrompt"
            type="textarea"
            :rows="4"
            :placeholder="gp('createTableHint')"
          />
        </el-form-item>
      </el-form>
      <div class="wizard-actions">
        <el-button type="primary" :loading="loading" @click="runGenerateCreateDdl">
          {{ gp('aiGenerateDdl') }}
        </el-button>
      </div>

      <el-alert
        v-if="createDdlSummary"
        type="success"
        :closable="false"
        show-icon
        class="form-block-tip"
      >
        {{ createDdlSummary }}
      </el-alert>

      <DdlEditorPanel
        v-model="ddlText"
        :ddl-record-id="ddlRecordId"
        :hint="gp('ddlEditorHint')"
        :placeholder="gp('ddlEditorPlaceholder')"
      />
    </el-tab-pane>

    <el-tab-pane :label="gp('tableModeAlter')" name="alter">
      <el-form label-width="120px" style="max-width: 960px">
        <el-form-item :label="gp('dbTable')" required>
          <el-select
            v-model="alterTableName"
            filterable
            remote
            reserve-keyword
            :remote-method="searchDbTables"
            :loading="dbTableLoading"
            :placeholder="gp('alterTablePlaceholder')"
            style="width: 100%"
            @change="loadAlterSchema"
          >
            <el-option
              v-for="row in dbTableOptions"
              :key="row.tableName"
              :label="`${row.tableName}${row.tableComment ? ` (${row.tableComment})` : ''}`"
              :value="row.tableName!"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="gp('alterTablePrompt')" required>
          <el-input
            v-model="alterTablePrompt"
            type="textarea"
            :rows="4"
            :placeholder="gp('alterTableHint')"
            :disabled="!alterTableName"
          />
        </el-form-item>
      </el-form>

      <div v-if="alterSchema" v-loading="schemaLoading" style="margin-bottom: 16px">
        <el-collapse>
          <el-collapse-item :title="gp('currentSchemaHint')" name="schema">
            <el-table :data="alterSchema.columns" stripe border size="small" max-height="220">
              <el-table-column :label="ggen('columnName')" prop="columnName" width="160" />
              <el-table-column
                :label="ggen('columnComment')"
                prop="columnComment"
                min-width="120"
              />
              <el-table-column :label="ggen('columnType')" prop="columnType" width="130" />
            </el-table>
          </el-collapse-item>
        </el-collapse>
      </div>

      <div class="wizard-actions">
        <el-button
          type="primary"
          :disabled="!alterTableName"
          :loading="loading"
          @click="runGenerateAlterDdl"
        >
          {{ gp('aiGenerateDdl') }}
        </el-button>
      </div>

      <el-alert
        v-if="alterDdlSummary"
        type="success"
        :closable="false"
        show-icon
        class="form-block-tip"
      >
        {{ alterDdlSummary }}
      </el-alert>

      <DdlEditorPanel
        v-model="ddlText"
        :ddl-record-id="ddlRecordId"
        :hint="gp('alterDdlEditorHint')"
        :placeholder="gp('alterDdlEditorPlaceholder')"
        :rows="14"
      />
    </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useToolAiI18n } from '@/composables/useToolAiI18n'
import { useToolGenI18n } from '@/composables/useToolGenI18n'
import DdlEditorPanel from './components/DdlEditorPanel.vue'
import {
  designAlterDdlPageSpecTable,
  designCreateDdlPageSpecTable,
  getPageSpecTableSchema,
  listPageSpecModules,
  pageSpecDbTables,
  type GenModuleOption,
  type GenTableSchema,
} from '@/api/tool/pageSpec'
import type { GenTable } from '@/types/tool/gen'

const props = defineProps<{ aiReady: boolean }>()

const { t } = useI18n()
const { gp } = useToolAiI18n()
const { gp: ggen } = useToolGenI18n()

const loading = ref(false)
const schemaLoading = ref(false)
const tableMode = ref<'create' | 'alter'>('create')
const alterTableName = ref<string>()
const dbTableOptions = ref<GenTable[]>([])
const dbTableLoading = ref(false)
const createTablePrompt = ref('')
const alterTablePrompt = ref('')
const createApiModule = ref('biz')
const moduleOptions = ref<GenModuleOption[]>([])
const alterSchema = ref<GenTableSchema | null>(null)
const ddlText = ref('')
const ddlRecordId = ref<string>()
const createDdlSummary = ref('')
const alterDdlSummary = ref('')

watch(tableMode, () => {
  ddlText.value = ''
  ddlRecordId.value = undefined
  createDdlSummary.value = ''
  alterDdlSummary.value = ''
})

async function loadModules() {
  const res = await listPageSpecModules()
  moduleOptions.value = res.data.data ?? []
  if (
    moduleOptions.value.length &&
    !moduleOptions.value.some((m) => m.apiModule === createApiModule.value)
  ) {
    createApiModule.value = moduleOptions.value[0].apiModule
  }
}

async function searchDbTables(keyword: string) {
  dbTableLoading.value = true
  try {
    const res = await pageSpecDbTables({
      pageNo: 1,
      pageSize: 50,
      tableName: keyword || undefined,
    })
    dbTableOptions.value = res.data.data.list ?? []
  } finally {
    dbTableLoading.value = false
  }
}

async function loadAlterSchema(tableName: string) {
  if (!tableName) {
    alterSchema.value = null
    ddlText.value = ''
    ddlRecordId.value = undefined
    alterDdlSummary.value = ''
    return
  }
  schemaLoading.value = true
  try {
    const res = await getPageSpecTableSchema(tableName)
    alterSchema.value = res.data.data
    ddlText.value = ''
    ddlRecordId.value = undefined
    alterDdlSummary.value = ''
  } finally {
    schemaLoading.value = false
  }
}

function ensureAi() {
  if (!props.aiReady) {
    ElMessage.warning(gp('notEnabled'))
    return false
  }
  return true
}

async function runGenerateCreateDdl() {
  if (!ensureAi()) return
  if (!createTablePrompt.value.trim()) {
    ElMessage.warning(t('common.pleaseInput', { field: gp('createTablePrompt') }))
    return
  }
  loading.value = true
  try {
    const res = await designCreateDdlPageSpecTable({
      userPrompt: createTablePrompt.value.trim(),
      apiModule: createApiModule.value,
    })
    ddlText.value = res.data.data.ddl ?? ''
    ddlRecordId.value = res.data.data.ddlRecordId
    createDdlSummary.value = res.data.data.summary ?? gp('ddlGenerateSuccess')
    ElMessage.success(gp('ddlGenerateSuccess'))
  } finally {
    loading.value = false
  }
}

async function runGenerateAlterDdl() {
  if (!ensureAi()) return
  if (!alterTableName.value) return
  if (!alterTablePrompt.value.trim()) {
    ElMessage.warning(t('common.pleaseInput', { field: gp('alterTablePrompt') }))
    return
  }
  loading.value = true
  try {
    const res = await designAlterDdlPageSpecTable({
      tableName: alterTableName.value,
      userPrompt: alterTablePrompt.value.trim(),
    })
    ddlText.value = res.data.data.ddl ?? ''
    ddlRecordId.value = res.data.data.ddlRecordId
    alterDdlSummary.value = res.data.data.summary ?? gp('ddlGenerateSuccess')
    ElMessage.success(gp('ddlGenerateSuccess'))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadModules(), searchDbTables('')])
})
</script>

<style scoped lang="scss">
@use '@/styles/tool-ai.scss';

.table-wizard-tabs :deep(.el-tabs__content) {
  padding-top: 8px;
}
</style>
