<template>
  <div>
    <el-steps :active="step" finish-status="success" align-center style="margin-bottom: 24px">
      <el-step :title="gp('stepSelectTable')" />
      <el-step :title="gp('stepDescribe')" />
      <el-step :title="gp('stepConfirm')" />
      <el-step :title="gp('stepPublish')" />
    </el-steps>

    <!-- Step 0 -->
    <section v-show="step === 0">
      <el-form label-width="120px" style="max-width: 960px">
        <el-form-item :label="gp('genTable')" required>
          <el-select
            v-model="selectedGenTableId"
            filterable
            remote
            reserve-keyword
            :remote-method="searchGenTables"
            :loading="genTableLoading"
            :placeholder="gp('genTablePlaceholder')"
            style="width: 100%"
            @change="onGenTablePick"
          >
            <el-option
              v-for="row in genTableOptions"
              :key="row.id"
              :label="`${row.tableName}${row.tableComment ? ` (${row.tableComment})` : ''}`"
              :value="row.id!"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="gp('pageKey')">
          <el-input v-model="pageKey" placeholder="user" style="width: 240px" />
        </el-form-item>
        <el-form-item :label="gp('layout')">
          <el-select v-model="layout" style="width: 240px">
            <el-option :label="gp('layoutStandardCrud')" value="standard-crud" />
            <el-option :label="gp('layoutTreeCrud')" value="tree-crud" />
          </el-select>
        </el-form-item>
      </el-form>
      <p class="form-tip">{{ gp('codegenSelectHint') }}</p>
      <el-alert type="info" :closable="false" show-icon class="form-block-tip">
        {{ gp('codegenWorkflowHint') }}
      </el-alert>
      <div class="wizard-actions">
        <el-button
          type="primary"
          :disabled="!selectedGenTableId"
          :loading="loading"
          @click="startWizard"
        >
          {{ gp('nextStep') }}
        </el-button>
      </div>
    </section>

    <!-- Step 1 -->
    <section v-show="step === 1">
      <el-alert
        v-if="draft?.summary"
        type="success"
        :closable="false"
        show-icon
        class="form-block-tip"
      >
        {{ draft.summary }}
      </el-alert>
      <el-input
        v-model="userPrompt"
        type="textarea"
        :rows="6"
        :placeholder="gp('userPromptPlaceholder')"
      />
      <p class="form-tip">{{ gp('stepDescribeHint') }}</p>
      <div class="wizard-actions">
        <el-button @click="step = 0">{{ gp('prevStep') }}</el-button>
        <el-button @click="skipAiAndConfirm">{{ gp('useDefaultPlan') }}</el-button>
        <el-button type="primary" :loading="loading" @click="runAnalyze">
          {{ gp('aiAnalyze') }}
        </el-button>
      </div>
    </section>

    <!-- Step 2 -->
    <section v-show="step === 2">
      <div v-if="draft" class="form-block-tip">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item :label="gp('pageKey')">
            {{ draft.pageKey }}
          </el-descriptions-item>
          <el-descriptions-item :label="gp('layout')">
            {{ draft.layout }}
          </el-descriptions-item>
          <el-descriptions-item :label="ggen('tableName')">
            {{ draft.entity?.tableName }}
          </el-descriptions-item>
        </el-descriptions>
        <p v-if="draft.summary" class="form-tip">{{ draft.summary }}</p>
      </div>
      <el-table :data="draft?.fields ?? []" stripe border max-height="520" size="small">
        <el-table-column :label="gp('fieldOrder')" width="72" align="center" fixed>
          <template #default="{ $index }">
            <div class="order-actions">
              <el-button
                link
                type="primary"
                :disabled="$index === 0"
                :icon="ArrowUp"
                @click="moveField($index, -1)"
              />
              <el-button
                link
                type="primary"
                :disabled="$index === (draft?.fields?.length ?? 0) - 1"
                :icon="ArrowDown"
                @click="moveField($index, 1)"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column type="index" width="50" fixed />
        <el-table-column :label="ggen('javaField')" prop="javaField" width="130" fixed />
        <el-table-column :label="ggen('columnComment')" min-width="140">
          <template #default="{ row }">
            <el-input v-model="row.columnComment" size="small" />
          </template>
        </el-table-column>
        <el-table-column :label="ggen('isQuery')" width="70" align="center">
          <template #default="{ row }"><el-checkbox v-model="row.isQuery" /></template>
        </el-table-column>
        <el-table-column :label="ggen('queryType')" width="100">
          <template #default="{ row }">
            <el-select v-model="row.queryType" :disabled="!row.isQuery" size="small">
              <el-option label="=" value="EQ" />
              <el-option label="LIKE" value="LIKE" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column :label="ggen('isList')" width="70" align="center">
          <template #default="{ row }"><el-checkbox v-model="row.isList" /></template>
        </el-table-column>
        <el-table-column :label="ggen('isExport')" width="70" align="center">
          <template #default="{ row }"><el-checkbox v-model="row.isExport" /></template>
        </el-table-column>
        <el-table-column :label="ggen('isImport')" width="70" align="center">
          <template #default="{ row }"><el-checkbox v-model="row.isImport" /></template>
        </el-table-column>
        <el-table-column :label="ggen('isInsert')" width="70" align="center">
          <template #default="{ row }"><el-checkbox v-model="row.isInsert" /></template>
        </el-table-column>
        <el-table-column :label="ggen('isEdit')" width="70" align="center">
          <template #default="{ row }"><el-checkbox v-model="row.isEdit" /></template>
        </el-table-column>
        <el-table-column :label="ggen('htmlType')" width="120">
          <template #default="{ row }">
            <el-select v-model="row.htmlType" size="small" @change="onHtmlTypeChange(row)">
              <el-option :label="ggen('htmlInput')" value="input" />
              <el-option :label="ggen('htmlTextarea')" value="textarea" />
              <el-option :label="ggen('htmlSelect')" value="select" />
              <el-option :label="ggen('htmlRadio')" value="radio" />
              <el-option :label="ggen('htmlDatetime')" value="datetime" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column :label="ggen('dictType')" min-width="200">
          <template #default="{ row }">
            <el-select
              v-model="row.dictType"
              filterable
              clearable
              allow-create
              default-first-option
              :disabled="!needsDictType(row.htmlType)"
              :placeholder="ggen('dictTypePlaceholder')"
              size="small"
              style="width: 100%"
            >
              <el-option
                v-for="dt in dictTypeOptions"
                :key="dt.dictType"
                :label="`${dt.dictName} (${dt.dictType})`"
                :value="dt.dictType"
              />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <div class="wizard-actions">
        <el-button @click="step = 1">{{ gp('prevStep') }}</el-button>
        <el-button type="primary" @click="step = 3">{{ gp('nextStep') }}</el-button>
      </div>
    </section>

    <!-- Step 3 -->
    <section v-show="step === 3">
      <el-result
        icon="info"
        :title="gp('readyPublishTitle')"
        :sub-title="gp('readyPublishDesc')"
      >
        <template #extra>
          <el-button
            type="primary"
            :loading="loading"
            v-hasPermi="[PERM.generate, PERM_GEN.code]"
            @click="handlePublish"
          >
            {{ gp('publishPage') }}
          </el-button>
        </template>
      </el-result>
      <el-collapse v-model="advancedOpen">
        <el-collapse-item :title="gp('advancedYaml')" name="yaml">
          <pre class="code-block">{{ yamlPreview }}</pre>
        </el-collapse-item>
      </el-collapse>
      <div class="wizard-actions">
        <el-button @click="step = 2">{{ gp('prevStep') }}</el-button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useToolAiI18n } from '@/composables/useToolAiI18n'
import { useToolGenI18n } from '@/composables/useToolGenI18n'
import { PERM_AI as PERM } from '@/api/tool/ai/catalog'
import { pageGenTable, PERM_GEN } from '@/api/tool/gen'
import { listDictTypeOptions } from '@/api/system/dict'
import {
  analyzePageSpec,
  getPageSpecDraft,
  publishPageSpec,
  type GenPageSpecDraft,
  type GenPageSpecFieldDraft,
} from '@/api/tool/pageSpec'
import type { GenTable } from '@/types/tool/gen'
import type { DictTypeVO } from '@/types/system/dict'

const props = defineProps<{ aiReady: boolean }>()

const { gp } = useToolAiI18n()
const { gp: ggen } = useToolGenI18n()

const step = ref(0)
const loading = ref(false)
const selectedGenTableId = ref<string>()
const genTableOptions = ref<GenTable[]>([])
const genTableLoading = ref(false)
const tableId = ref<string>()
const pageKey = ref('')
const layout = ref('standard-crud')
const userPrompt = ref('')
const draft = ref<GenPageSpecDraft | null>(null)
const advancedOpen = ref<string[]>([])
const dictTypeOptions = ref<DictTypeVO[]>([])

const yamlPreview = computed(() => {
  if (!draft.value) return ''
  return JSON.stringify(draft.value, null, 2)
})

function derivePageKey(tableName: string) {
  const raw = tableName.replace(/^(sys_|infra_|biz_)/, '')
  const camel = raw.replace(/_([a-z])/g, (_, c: string) => c.toUpperCase())
  return camel.charAt(0).toLowerCase() + camel.slice(1)
}

async function searchGenTables(keyword: string) {
  genTableLoading.value = true
  try {
    const res = await pageGenTable({
      pageNo: 1,
      pageSize: 50,
      tableName: keyword || undefined,
    })
    genTableOptions.value = res.data.data.list ?? []
  } finally {
    genTableLoading.value = false
  }
}

function onGenTablePick(id: string) {
  const row = genTableOptions.value.find((r) => r.id === id)
  if (row?.tableName) {
    pageKey.value = derivePageKey(row.tableName)
  }
}

async function loadDictTypes() {
  const res = await listDictTypeOptions()
  dictTypeOptions.value = res.data.data ?? []
}

async function loadDraftAndAdvance(preparedTableId: string, preparedPageKey: string) {
  const res = await getPageSpecDraft(preparedTableId, preparedPageKey || undefined, layout.value)
  draft.value = res.data.data
  tableId.value = preparedTableId
  pageKey.value = draft.value.pageKey
  layout.value = draft.value.layout
  step.value = 1
}

async function startWizard() {
  if (!selectedGenTableId.value) return
  loading.value = true
  try {
    await loadDraftAndAdvance(selectedGenTableId.value, pageKey.value)
  } finally {
    loading.value = false
  }
}

function needsDictType(htmlType?: string) {
  return htmlType === 'select' || htmlType === 'radio'
}

function onHtmlTypeChange(row: GenPageSpecFieldDraft) {
  if (!needsDictType(row.htmlType)) {
    row.dictType = undefined
  }
}

function moveField(index: number, delta: number) {
  const fields = draft.value?.fields
  if (!fields) return
  const target = index + delta
  if (target < 0 || target >= fields.length) return
  const [item] = fields.splice(index, 1)
  fields.splice(target, 0, item)
}

function ensureAi() {
  if (!props.aiReady) {
    ElMessage.warning(gp('notEnabled'))
    return false
  }
  return true
}

async function runAnalyze() {
  if (!ensureAi()) return
  if (!tableId.value || !draft.value) return
  loading.value = true
  try {
    const res = await analyzePageSpec({
      tableId: tableId.value,
      pageKey: pageKey.value,
      layout: layout.value,
      userPrompt: userPrompt.value.trim() || undefined,
    })
    draft.value = res.data.data
    pageKey.value = draft.value.pageKey
    layout.value = draft.value.layout
    step.value = 2
    ElMessage.success(gp('analyzeSuccess'))
  } finally {
    loading.value = false
  }
}

function skipAiAndConfirm() {
  step.value = 2
}

async function handlePublish() {
  if (!draft.value || !tableId.value) return
  if (layout.value === 'master-detail') {
    ElMessage.warning(gp('masterDetailUnsupported'))
    return
  }
  await ElMessageBox.confirm(gp('readyPublishDesc'), gp('readyPublishTitle'), {
    type: 'info',
  })
  loading.value = true
  try {
    draft.value.pageKey = pageKey.value
    draft.value.layout = layout.value
    const res = await publishPageSpec({ tableId: tableId.value, draft: draft.value })
    const data = res.data.data
    ElMessage.success(
      gp('publishSuccess', { pageKey: data.pageKey, tableName: data.tableName }),
    )
    step.value = 0
    tableId.value = undefined
    selectedGenTableId.value = undefined
    draft.value = null
    userPrompt.value = ''
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([searchGenTables(''), loadDictTypes()])
})
</script>

<style scoped lang="scss">
@use '@/styles/tool-ai.scss';

.order-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
</style>
