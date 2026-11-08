<!-- OA 审批模板配置内页：表单配置 → 流程设计 → 发布 — GenListLayout -->
<template>
  <div class="gen-page-shell">
    <GenListLayout
      v-loading="loading"
      :title="pageTitle"
      :description="pageDescription"
      :show-search="false"
      :show-right-toolbar="false"
      class="configure-page"
    >
      <template #header-extra>
        <el-button @click="goBack">{{ t('common.back') }}</el-button>
        <template v-if="!viewMode">
          <el-button
            v-if="activeTab === 'form' || activeTab === 'layout'"
            type="primary"
            plain
            :loading="saving"
            @click="saveFieldsOnly"
          >
            {{ t('common.save') }}
          </el-button>
          <el-button
            v-if="activeTab !== 'publish'"
            type="primary"
            :loading="advancing"
            @click="goNextTab"
          >
            {{ gp('wizardNext') }}
          </el-button>
          <el-button
            v-else
            type="primary"
            :loading="publishing"
            :disabled="!readiness?.ready"
            v-hasPermi="[PERM_OaTenantTemplate.publish]"
            @click="doPublish"
          >
            {{ gp('publishAction') }}
          </el-button>
        </template>
      </template>

      <el-tabs v-model="activeTab" class="configure-tabs" :before-leave="beforeTabLeave">
        <el-tab-pane :label="gp('wizardStepForm')" name="form" :disabled="!canVisit('form')">
          <OaFieldDefEditorPanel
            ref="fieldEditorRef"
            tenant-mode
            :readonly="viewMode"
            :version-no="versionNo"
            :version-status="versionStatus"
            :fields="fields"
            :max-height="tableMaxHeight"
          />
        </el-tab-pane>

        <el-tab-pane :label="gp('wizardStepLayout')" name="layout" :disabled="!canVisit('layout')">
          <p class="configure-layout-hint">{{ gp('wizardLayoutHint') }}</p>
          <FormDesigner
            v-if="!viewMode"
            v-model="layoutDocument"
            host-mode="bound"
            :field-pool="liveFormFields"
            class="configure-layout-designer"
          />
          <FormRuntime
            v-else
            :document="layoutDocument"
            :model-value="{}"
            device="pc"
            :empty-text="gp('wizardLayoutEmpty')"
          />
        </el-tab-pane>

        <el-tab-pane :label="gp('wizardStepFlow')" name="flow" :disabled="!canVisit('flow')">
          <VerticalFlowView
            v-if="viewMode"
            :design-json="viewDesignJson"
            :field-labels="conditionFieldLabels"
            :empty-text="gp('wizardNoPlatformModel')"
          />
          <VerticalFlowDesigner
            v-else-if="modelId"
            ref="designerRef"
            :model-id="modelId"
            :model-key="modelDetail?.modelKey"
            :toolbar-title="modelDetail?.modelName || gp('wizardStepFlow')"
            :condition-fields="conditionFields"
            :form-fields="fields"
            @deployed="onDesignerDeployed"
            @open-xml="deployVisible = true"
          >
            <template #toolbar-tags>
              <el-tag size="small" type="info" effect="plain">
                {{ modelDetail?.modelKey || '—' }}
              </el-tag>
              <el-tag size="small" :type="deployed ? 'success' : 'warning'" effect="light">
                {{ deployed ? gp('wizardDeployed') : gp('wizardNotDeployed') }}
                · v{{ modelDetail?.latestVersion ?? 0 }}
              </el-tag>
            </template>
            <template v-if="conditionFields.length" #meta>
              <span class="vfd-toolbar-meta__label">{{ gp('wizardConditionFields') }}</span>
              <el-tag
                v-for="f in conditionFields"
                :key="f.fieldCode"
                size="small"
                effect="plain"
                type="warning"
              >
                {{ f.fieldName }}
              </el-tag>
            </template>
          </VerticalFlowDesigner>
        </el-tab-pane>

        <el-tab-pane
          :label="gp('wizardStepPublish')"
          name="publish"
          :disabled="!canVisit('publish')"
        >
          <el-alert
            :type="readiness?.ready ? 'success' : 'warning'"
            :closable="false"
            show-icon
            class="mb12"
          >
            <template #title>
              {{ readiness?.ready ? gp('wizardReady') : gp('wizardNotReady') }}
            </template>
          </el-alert>
          <el-descriptions :column="1" border size="small" class="mb12">
            <el-descriptions-item :label="gp('wizardCheckFields')">
              <el-tag :type="readiness?.fieldsReady ? 'success' : 'danger'" size="small">
                {{ readiness?.fieldsReady ? gp('wizardCheckOk') : gp('wizardCheckFail') }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="gp('wizardCheckSynced')">
              <el-tag :type="readiness?.modelSynced ? 'success' : 'danger'" size="small">
                {{ readiness?.modelSynced ? gp('wizardCheckOk') : gp('wizardCheckFail') }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="gp('wizardCheckDeployed')">
              <el-tag :type="readiness?.modelDeployed ? 'success' : 'danger'" size="small">
                {{ readiness?.modelDeployed ? gp('wizardCheckOk') : gp('wizardCheckFail') }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="gp('wizardCheckApprovers')">
              <el-tag :type="readiness?.approversReady ? 'success' : 'danger'" size="small">
                {{ readiness?.approversReady ? gp('wizardCheckOk') : gp('wizardCheckFail') }}
              </el-tag>
              <span v-if="readiness?.missingApproverTaskKeys?.length" class="muted missing-keys">
                {{ readiness.missingApproverTaskKeys.join(', ') }}
              </span>
              <el-button
                v-if="!readiness?.approversReady && !viewMode"
                link
                type="primary"
                class="ml8"
                @click="goToFlowTab"
              >
                {{ gp('wizardApproverGoFlow') }}
              </el-button>
            </el-descriptions-item>
          </el-descriptions>
          <div class="mb12 label">{{ gp('wizardApproverTip') }}</div>
          <el-table :data="approverRows" border size="small" :max-height="280">
            <el-table-column
              prop="taskDefinitionKey"
              :label="gp('wizardTaskKey')"
              min-width="160"
            />
            <el-table-column :label="gp('wizardApproverStatus')" width="120">
              <template #default="{ row }">
                <el-tag :type="row.ruleId ? 'success' : 'danger'" size="small">
                  {{ row.ruleId ? gp('wizardApproverConfigured') : gp('wizardApproverMissing') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              prop="variableName"
              :label="gp('wizardVariableName')"
              min-width="140"
            />
            <el-table-column prop="strategyType" :label="gp('wizardStrategyType')" width="140" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </GenListLayout>

    <WorkflowModelDeployDialog
      v-if="!viewMode"
      v-model="deployVisible"
      :model-id="modelId"
      @success="onDeployed"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import FormDesigner from '@/components/form-designer/FormDesigner.vue'
import FormRuntime from '@/components/form-designer/FormRuntime.vue'
import { prepareBoundLayoutSave } from '@/components/form-designer/boundHost'
import { composeBoundDocument, persistLayout } from '@/components/form-designer/oaBridge'
import type { FormDocument } from '@/types/form-designer/schema'
import OaFieldDefEditorPanel from '@/views/oa/components/OaFieldDefEditorPanel.vue'
import VerticalFlowDesigner from '@/views/workflow/designer/VerticalFlowDesigner.vue'
import VerticalFlowView from '@/components/workflow/VerticalFlowView.vue'
import WorkflowModelDeployDialog from '@/views/workflow/model/WorkflowModelDeployDialog.vue'
import type { OaFieldDefVO, OaTenantPublishReadinessVO } from '@/types/oa/oaTenantTemplate'
import type { WorkflowModelDetailVO } from '@/types/workflow/model'
import type { WorkflowApproverRuleVO } from '@/types/workflow/approverRule'
import {
  collectLeafTaskKeys,
  extractDesignNodes,
  extractDesignNodesFromJson,
  type WorkflowProcessConfig,
} from '@/types/workflow/design'
import {
  PERM_OaTenantTemplate,
  createOaTenantDraftVersion,
  getOaTenantPublishReadiness,
  getOaTenantTemplate,
  getOaTenantTemplateVersion,
  publishOaTenantVersion,
  saveOaTenantFieldDefs,
} from '@/api/oa/oaTenantTemplate'
import {
  getWorkflowModel,
  listWorkflowModelSyncCatalog,
  syncWorkflowModelFromPlatform,
} from '@/api/workflow/model'
import { pageWorkflowApproverRule } from '@/api/workflow/approverRule'
import { isYes } from '@/constants/flags'
type TabName = 'form' | 'layout' | 'flow' | 'publish'

interface ApproverRow {
  taskDefinitionKey: string
  ruleId?: string
  variableName?: string
  strategyType?: string
}

const TAB_ORDER: TabName[] = ['form', 'layout', 'flow', 'publish']

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const { gp } = useGenPageShellI18n('oaTenantTemplate')

const loading = ref(false)
const advancing = ref(false)
const saving = ref(false)
const publishing = ref(false)
const activeTab = ref<TabName>('form')
/** 已解锁到的最大 tab 下标（可回看已解锁页签） */
const unlockedIndex = ref(0)
const skipBeforeLeave = ref(false)

const templateId = computed(() => String(route.params.id || ''))
const viewMode = computed(() => String(route.query.mode || '') === 'view')
const templateName = ref('')
const categoryCode = ref('')
const versionId = ref('')
const versionNo = ref<number>()
const versionStatus = ref<string>()
const fields = ref<OaFieldDefVO[]>([])
const fieldEditorRef = ref<InstanceType<typeof OaFieldDefEditorPanel>>()
const layoutDocument = ref<FormDocument>(composeBoundDocument({ fields: [] }))
const liveFormFields = computed(
  () => fieldEditorRef.value?.getFields() ?? fields.value,
)

watch(activeTab, (tab) => {
  if (tab !== 'layout' || viewMode.value) return
  const rows = fieldEditorRef.value?.getFields() ?? fields.value
  layoutDocument.value = composeBoundDocument({
    fields: rows,
    layoutJson: persistLayout(layoutDocument.value),
  })
})

const modelId = ref<string>()
const modelDetail = ref<WorkflowModelDetailVO>()
const deployVisible = ref(false)
const readiness = ref<OaTenantPublishReadinessVO>()
const approverRows = ref<ApproverRow[]>([])
const designerRef = ref<InstanceType<typeof VerticalFlowDesigner>>()

const pageTitle = computed(() => {
  const name = templateName.value
  const base = viewMode.value ? gp('viewMappingTitle') : gp('wizardTitle')
  return name ? `${base} · ${name}` : base
})

const pageDescription = computed(() =>
  viewMode.value ? gp('viewMappingDescription') : gp('wizardPageDescription'),
)

const tableMaxHeight = computed(() => 'calc(100vh - 320px)')

const conditionFields = computed(() =>
  fields.value.filter((f) => {
    if (!f.fieldCode) return false
    const flag = f.usedInCondition as unknown
    return isYes(f.usedInCondition) || flag === 1 || flag === true
  }),
)

const conditionFieldLabels = computed(() => {
  const map: Record<string, string> = {}
  for (const f of fields.value) {
    if (f.fieldCode && f.fieldName) map[f.fieldCode] = f.fieldName
  }
  return map
})

const viewDesignJson = computed(() => {
  const detail = modelDetail.value
  if (!detail) return undefined
  if (detail.designJson) return detail.designJson
  if (detail.processConfig) {
    try {
      const cfg = JSON.parse(detail.processConfig) as WorkflowProcessConfig
      if (cfg.design) return JSON.stringify(cfg.design)
    } catch {
      /* ignore */
    }
  }
  return undefined
})

const deployed = computed(() => {
  const latest = modelDetail.value?.latestVersion ?? 0
  if (latest <= 0) return false
  const pub = modelDetail.value?.versions?.find((v) => v.published || v.versionNo === latest)
  return !!(pub?.deploymentId && pub?.processDefinitionId)
})

function canVisit(tab: TabName) {
  if (viewMode.value) return true
  return TAB_ORDER.indexOf(tab) <= unlockedIndex.value
}

function tabIndex(tab: TabName) {
  return TAB_ORDER.indexOf(tab)
}

onMounted(() => {
  void bootstrap()
})

async function bootstrap() {
  if (!templateId.value) {
    ElMessage.warning(gp('noDraft'))
    goBack()
    return
  }
  loading.value = true
  try {
    const detail = (await getOaTenantTemplate(templateId.value)).data.data
    templateName.value = detail.templateName || detail.categoryCode || ''
    categoryCode.value = detail.categoryCode || ''

    if (viewMode.value) {
      const published = detail.publishedVersion
      if (!published?.id) {
        ElMessage.warning(gp('viewMappingNoPublished'))
        goBack()
        return
      }
      versionId.value = published.id
      versionNo.value = published.versionNo
      versionStatus.value = published.versionStatus
      modelId.value = published.wfModelId
      unlockedIndex.value = TAB_ORDER.length - 1
      await loadFields()
      if (modelId.value) {
        await reloadModelDetail()
        await loadReadiness()
        await reloadApprovers()
      }
      return
    }

    let nextDetail = detail
    if (!nextDetail.draftVersion) {
      await createOaTenantDraftVersion({ templateId: templateId.value })
      ElMessage.success(gp('draftCreated'))
      nextDetail = (await getOaTenantTemplate(templateId.value)).data.data
    }
    const draft = nextDetail.draftVersion
    if (!draft) {
      ElMessage.warning(gp('noDraft'))
      goBack()
      return
    }
    versionId.value = draft.id
    versionNo.value = draft.versionNo
    versionStatus.value = draft.versionStatus
    await loadFields()
  } finally {
    loading.value = false
  }
}

async function loadFields() {
  if (!versionId.value) return
  const ver = await getOaTenantTemplateVersion(versionId.value)
  fields.value = ver.data.data.fields ?? []
  layoutDocument.value = composeBoundDocument({
    fields: fields.value,
    layoutJson: ver.data.data.layoutJson,
  })
}

function gatedLayoutJson(rows: OaFieldDefVO[]): string | null {
  const prepared = prepareBoundLayoutSave({
    fields: rows,
    document: layoutDocument.value,
  })
  if (!prepared.ok) {
    ElMessage.error(gp('wizardLayoutValidateFail') || prepared.reason)
    return null
  }
  layoutDocument.value = prepared.document
  return prepared.layoutJson
}

async function saveFieldsOnly() {
  if (!versionId.value) return
  const rows = fieldEditorRef.value?.getFields() ?? fields.value
  const layoutJson = gatedLayoutJson(rows)
  if (layoutJson == null) return
  saving.value = true
  try {
    await saveOaTenantFieldDefs({
      versionId: versionId.value,
      fields: rows,
      layoutJson,
    })
    fields.value = rows
    ElMessage.success(t('common.success'))
  } finally {
    saving.value = false
  }
}

async function persistFormFields() {
  if (!versionId.value) return
  const rows = fieldEditorRef.value?.getFields() ?? fields.value
  const layoutJson = gatedLayoutJson(rows)
  if (layoutJson == null) {
    throw new Error('layout_validate_failed')
  }
  await saveOaTenantFieldDefs({
    versionId: versionId.value,
    fields: rows,
    layoutJson,
  })
  fields.value = rows
}

async function ensureModelSynced() {
  if (!categoryCode.value) {
    ElMessage.warning(gp('wizardNoCategory'))
    return false
  }
  const catalogRes = await listWorkflowModelSyncCatalog()
  const catalog = catalogRes.data.data ?? []
  const item = catalog.find((c) => c.bizType === categoryCode.value)
  if (!item) {
    ElMessage.warning(gp('wizardNoPlatformModel'))
    return false
  }
  if (!item.synced) {
    if (!item.platformModelId) {
      ElMessage.warning(gp('wizardNoPlatformModel'))
      return false
    }
    const syncRes = await syncWorkflowModelFromPlatform({ platformModelId: item.platformModelId })
    modelId.value = syncRes.data.data
    ElMessage.success(gp('wizardSyncDone'))
  } else {
    modelId.value = item.syncedModelId
  }
  if (!modelId.value) return false
  await reloadModelDetail()
  return true
}

async function reloadModelDetail() {
  if (!modelId.value) return
  const res = await getWorkflowModel(modelId.value)
  modelDetail.value = res.data.data
}

async function onDeployed() {
  await reloadModelDetail()
  await designerRef.value?.reload()
}

async function onDesignerDeployed() {
  await reloadModelDetail()
  await loadReadiness()
  await reloadApprovers()
}

function goToFlowTab() {
  skipBeforeLeave.value = true
  activeTab.value = 'flow'
}

function taskKeysFromDesign(detail?: { designJson?: string; processConfig?: string }): string[] {
  if (!detail) return []
  let nodes = extractDesignNodesFromJson(detail.designJson)
  if (!nodes.length && detail.processConfig) {
    try {
      nodes = extractDesignNodes(JSON.parse(detail.processConfig) as WorkflowProcessConfig)
    } catch {
      nodes = []
    }
  }
  return collectLeafTaskKeys(nodes)
}

async function reloadApprovers() {
  if (!modelId.value) return
  const detail = modelDetail.value ?? (await getWorkflowModel(modelId.value)).data.data
  modelDetail.value = detail
  const taskKeys = readiness.value?.userTaskKeys?.length
    ? readiness.value.userTaskKeys
    : taskKeysFromDesign(detail)
  const rulesRes = await pageWorkflowApproverRule({
    modelId: modelId.value,
    pageNo: 1,
    pageSize: 200,
  })
  const rules = (rulesRes.data.data?.list ?? []) as WorkflowApproverRuleVO[]
  const byKey = new Map<string, WorkflowApproverRuleVO>()
  for (const r of rules) {
    if (r.taskDefinitionKey) byKey.set(r.taskDefinitionKey, r)
  }
  const keys = taskKeys.length ? taskKeys : [...byKey.keys()]
  approverRows.value = keys.map((key) => {
    const rule = byKey.get(key)
    return {
      taskDefinitionKey: key,
      ruleId: rule?.id,
      variableName: rule?.variableName,
      strategyType: rule?.strategyType,
    }
  })
  if (!keys.length && rules.length) {
    approverRows.value = rules.map((r) => ({
      taskDefinitionKey: r.taskDefinitionKey || '',
      ruleId: r.id,
      variableName: r.variableName,
      strategyType: r.strategyType,
    }))
  }
}

async function loadReadiness() {
  if (!versionId.value) return
  const res = await getOaTenantPublishReadiness(versionId.value)
  readiness.value = res.data.data
  if (readiness.value.modelId) {
    modelId.value = readiness.value.modelId
  }
}

/** 从 fromTab 前进到下一页前的门禁；成功则解锁并返回 true */
async function prepareAdvanceFrom(fromTab: TabName): Promise<boolean> {
  if (fromTab === 'form') {
    await persistFormFields()
    layoutDocument.value = composeBoundDocument({
      fields: fields.value,
      layoutJson: persistLayout(layoutDocument.value),
    })
    return true
  }
  if (fromTab === 'layout') {
    try {
      await persistFormFields()
    } catch {
      return false
    }
    const ok = await ensureModelSynced()
    return ok
  }
  if (fromTab === 'flow') {
    await reloadModelDetail()
    if (!deployed.value) {
      ElMessage.warning(gp('wizardNeedDeploy'))
      return false
    }
    await loadReadiness()
    await reloadApprovers()
    if (readiness.value && !readiness.value.approversReady) {
      ElMessage.warning(gp('wizardNeedApprover'))
      return false
    }
    await loadReadiness()
    return true
  }
  return true
}

async function beforeTabLeave(nextName: string | number, currentName: string | number) {
  if (viewMode.value) return true
  if (skipBeforeLeave.value) {
    skipBeforeLeave.value = false
    return true
  }
  const next = String(nextName) as TabName
  const current = String(currentName) as TabName
  const nextIdx = tabIndex(next)
  const curIdx = tabIndex(current)
  if (nextIdx < 0 || curIdx < 0) return false
  // 回看已解锁页签
  if (nextIdx <= unlockedIndex.value && nextIdx <= curIdx) {
    return true
  }
  // 已解锁范围内的前进切换
  if (nextIdx <= unlockedIndex.value) {
    return true
  }
  // 仅允许一步一步解锁
  if (nextIdx !== curIdx + 1) {
    ElMessage.warning(gp('wizardNeedStepByStep'))
    return false
  }
  advancing.value = true
  try {
    const ok = await prepareAdvanceFrom(current)
    if (!ok) return false
    unlockedIndex.value = Math.max(unlockedIndex.value, nextIdx)
    return true
  } finally {
    advancing.value = false
  }
}

async function goNextTab() {
  const curIdx = tabIndex(activeTab.value)
  if (curIdx < 0 || curIdx >= TAB_ORDER.length - 1) return
  const next = TAB_ORDER[curIdx + 1]
  advancing.value = true
  try {
    const ok = await prepareAdvanceFrom(activeTab.value)
    if (!ok) return
    unlockedIndex.value = Math.max(unlockedIndex.value, curIdx + 1)
    skipBeforeLeave.value = true
    activeTab.value = next
  } finally {
    advancing.value = false
  }
}

async function doPublish() {
  if (!versionId.value) return
  await loadReadiness()
  if (!readiness.value?.ready) {
    ElMessage.warning(gp('wizardNotReady'))
    return
  }
  publishing.value = true
  try {
    await publishOaTenantVersion({ versionId: versionId.value })
    ElMessage.success(gp('published'))
    goBack()
  } finally {
    publishing.value = false
  }
}

function goBack() {
  router.push({ path: '/oa/tenant-template' }).catch(() => {
    router.back()
  })
}
</script>

<style scoped>
.configure-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.configure-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: var(--el-border-color-light);
}

.configure-tabs :deep(.el-tabs__item) {
  font-weight: 500;
  color: var(--el-text-color-regular);
}

.configure-tabs :deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary);
  font-weight: 600;
}

.configure-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--el-color-primary);
  height: 2px;
  border-radius: 2px;
}

.configure-tabs :deep(.el-tabs__content) {
  padding-top: 0;
}

.configure-tabs :deep(.vfd-toolbar-meta__label) {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.mb12 {
  margin-bottom: 12px;
}

.label {
  font-weight: 500;
  margin-right: 8px;
}

.tag-gap {
  margin-right: 6px;
  margin-bottom: 4px;
}

.muted {
  color: var(--el-text-color-secondary);
}

.missing-keys {
  margin-left: 8px;
}

.ml8 {
  margin-left: 8px;
}

.configure-layout-hint {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.configure-layout-designer {
  height: calc(100vh - 260px);
  min-height: 480px;
  max-height: calc(100vh - 200px);
}
</style>
