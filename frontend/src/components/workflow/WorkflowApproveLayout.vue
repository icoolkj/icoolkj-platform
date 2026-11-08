<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import VerticalFlowView from '@/components/workflow/VerticalFlowView.vue'
import WorkflowProgressPanel from '@/components/workflow/WorkflowProgressPanel.vue'
import { useGenI18n } from '@/composables/useGenI18n'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import type { WorkflowInstanceDetailVO } from '@/types/workflow/instance'
import genI18nZhCN from '@/i18n/gen/workflowFlowView/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/workflowFlowView/en-US.json'

const props = defineProps<{
  loading?: boolean
  title?: string
  description?: string
  alertMessage?: string
  showProgress?: boolean
  progressTitle?: string
  approveTitle?: string
  /** Prefer page gp(); falls back to workflowFlowView i18n */
  attachmentsTitle?: string
  backLabel?: string
  currentNodeLabel?: string
  currentNodePrefix?: string
  infoTabLabel?: string
  flowTabLabel?: string
  emptyFlowLabel?: string
  instance?: WorkflowInstanceDetailVO
  fieldLabels?: Record<string, string> | null
  currentTaskId?: string
  progressLabels: {
    starter: string
    assignee: string
    result: string
    emptyTimeline: string
    emptyInstance: string
    pending: string
    skipped?: string
    waitingAssignee: string
    pass: string
    reject: string
    startEvent?: string
    cycleSubmit?: string
    expandHistory?: string
    collapseHistory?: string
    toUser?: string
    actionTransfer?: string
    actionDelegate?: string
    actionDelegateReturn?: string
    actionWithdraw?: string
    actionTerminate?: string
    actionJump?: string
    actionRejectJump?: string
    actionTimeout?: string
    actionAddSign?: string
    actionAddSignHandoff?: string
    actionAddSignReturn?: string
    actionRemoveSign?: string
    reasonStarterWithdraw?: string
    reasonCancelled?: string
  }
  /** 业务域原因码文案；透传给 WorkflowProgressPanel */
  reasonCodeLabels?: Record<string, string>
}>()

const emit = defineEmits<{
  back: []
}>()

const GEN_I18N_FILES: Record<string, GenI18nMessages> = {
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}
const { gp } = useGenI18n('workflowFlowView', 'workflow', GEN_I18N_FILES)

const attachmentsTitleText = computed(() => props.attachmentsTitle || gp('attachmentsTitle'))
const backLabelText = computed(() => props.backLabel || gp('backLabel'))
const currentNodePrefixText = computed(() => props.currentNodePrefix || gp('currentNodePrefix'))
const infoTabLabelText = computed(() => props.infoTabLabel || gp('infoTabLabel'))
const flowTabLabelText = computed(() => props.flowTabLabel || gp('flowTabLabel'))
const emptyFlowLabelText = computed(() => props.emptyFlowLabel || gp('emptyFlowLabel'))

const mainTab = ref<'info' | 'flow'>('info')

watch(
  () => props.instance?.instanceId,
  () => {
    mainTab.value = 'info'
  },
)

const nodeMeta = computed(() => {
  const node = props.currentNodeLabel?.trim()
  if (!node) return undefined
  return `${currentNodePrefixText.value}：${node}`
})

function onBack() {
  emit('back')
}
</script>

<template>
  <div class="workflow-approve-layout page-container" v-loading="loading">
    <PageHeader force :title="title || ''" :description="description" :meta="nodeMeta">
      <template #extra>
        <el-button :icon="ArrowLeft" @click="onBack">{{ backLabelText }}</el-button>
      </template>
    </PageHeader>

    <el-alert
      v-if="alertMessage"
      class="workflow-approve-layout__alert"
      type="warning"
      show-icon
      :closable="false"
      :title="alertMessage"
    />

    <el-row v-else :gutter="16">
      <el-col :xs="24" :lg="15" :xl="16">
        <el-card shadow="never" class="workflow-approve-layout__card">
          <el-tabs v-model="mainTab" class="workflow-approve-layout__tabs">
            <el-tab-pane :label="infoTabLabelText" name="info">
              <slot name="business" />
              <div v-if="$slots.attachments" class="workflow-approve-layout__attachments">
                <div class="workflow-approve-layout__attachments-title">{{ attachmentsTitleText }}</div>
                <slot name="attachments" />
              </div>
            </el-tab-pane>
            <el-tab-pane :label="flowTabLabelText" name="flow" lazy>
              <VerticalFlowView
                :design-json="instance?.designJson"
                :progress-nodes="instance?.progressNodes"
                :field-labels="fieldLabels"
                :empty-text="emptyFlowLabelText"
              />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="9" :xl="8">
        <div class="workflow-approve-layout__side">
          <el-card v-if="$slots.form" shadow="never" class="workflow-approve-layout__card">
            <template #header>{{ approveTitle }}</template>
            <slot name="form" />
          </el-card>
          <el-card v-if="showProgress" shadow="never" class="workflow-approve-layout__card">
            <template #header>{{ progressTitle }}</template>
            <WorkflowProgressPanel
              variant="compact"
              :instance="instance"
              :current-task-id="currentTaskId"
              :labels="progressLabels"
              :reason-code-labels="reasonCodeLabels"
            />
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.workflow-approve-layout__alert {
  margin-bottom: 16px;
}

.workflow-approve-layout__card {
  margin-bottom: 16px;
}

.workflow-approve-layout__tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.workflow-approve-layout__attachments {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.workflow-approve-layout__attachments-title {
  margin-bottom: 12px;
  font-weight: 600;
  font-size: 14px;
}

.workflow-approve-layout__side {
  position: sticky;
  top: 16px;
}
</style>
