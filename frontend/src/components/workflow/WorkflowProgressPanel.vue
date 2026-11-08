<template>
  <div v-if="instance" class="workflow-progress-panel" :class="{ 'is-compact': isCompact }">
    <div
      v-if="!isCompact && (instance.processDefinitionName || instance.bizTitle)"
      class="workflow-progress-panel__summary"
    >
      <div v-if="instance.processDefinitionName" class="workflow-progress-panel__title">
        {{ instance.processDefinitionName }}
      </div>
      <div v-if="instance.bizTitle" class="workflow-progress-panel__subtitle">
        {{ instance.bizTitle }}
      </div>
      <div class="workflow-progress-panel__meta">
        <span v-if="instance.starterUserName">
          {{ labels.starter }}：{{ instance.starterUserName }}
        </span>
        <span v-if="instance.status">
          <DictTag :options="statusOptions" :value="instance.status" />
        </span>
      </div>
    </div>

    <div
      v-if="isCompact && instance.starterUserName"
      class="workflow-progress-panel__meta workflow-progress-panel__meta--compact"
    >
      <span>{{ labels.starter }}：{{ instance.starterUserName }}</span>
      <DictTag v-if="instance.status" :options="statusOptions" :value="instance.status" />
    </div>

    <div v-if="canToggleHistory" class="workflow-progress-panel__toggle">
      <el-button link type="primary" @click="historyExpanded = !historyExpanded">
        {{ historyExpanded ? collapseLabel : expandLabel }}
      </el-button>
    </div>

    <el-timeline v-if="visibleTimelineItems.length" class="workflow-progress-panel__timeline">
      <el-timeline-item
        v-for="item in visibleTimelineItems"
        :key="item.key"
        :type="item.type"
        :timestamp="item.timestamp"
        placement="top"
        :hollow="item.hollow"
      >
        <div
          class="workflow-progress-panel__task-name"
          :class="{ 'is-cycle': item.kind === 'cycle' }"
        >
          {{ item.title }}
        </div>
        <div v-if="item.actionLabel" class="workflow-progress-panel__task-meta">
          {{ item.actionLabel }}
        </div>
        <div v-if="item.assignee" class="workflow-progress-panel__task-meta">
          {{ labels.assignee }}：{{ item.assignee }}
        </div>
        <div v-if="item.toUser" class="workflow-progress-panel__task-meta">
          {{ labels.toUser || 'To' }}：{{ item.toUser }}
        </div>
        <div v-if="item.resultLabel" class="workflow-progress-panel__task-meta">
          {{ labels.result }}：
          <DictTag :options="approveResultOptions" :value="item.resultLabel" />
        </div>
        <div v-if="item.comment" class="workflow-progress-panel__comment">{{ item.comment }}</div>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else :description="labels.emptyTimeline" :image-size="48" />
  </div>
  <el-empty v-else :description="labels.emptyInstance" :image-size="48" />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import DictTag from '@/components/DictTag.vue'
import { useDict } from '@/composables/useDict'
import type {
  WorkflowHistoricTaskVO,
  WorkflowInstanceDetailVO,
  WorkflowProcessCycleVO,
} from '@/types/workflow/instance'

const props = withDefaults(
  defineProps<{
    instance?: WorkflowInstanceDetailVO
    labels: {
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
      /** 已发申请系统码 starter_withdraw */
      reasonStarterWithdraw?: string
      /** 通用 cancelled */
      reasonCancelled?: string
    }
    /** 业务原因码 → 文案（审批页注入） */
    reasonCodeLabels?: Record<string, string>
    currentTaskId?: string
    variant?: 'full' | 'compact'
  }>(),
  {
    variant: 'full',
  },
)

/** Multi-cycle history starts collapsed (latest cycle only). */
const historyExpanded = ref(false)

const { getDict } = useDict(['wf_instance_status', 'wf_approve_result'])
const statusOptions = computed(() => getDict('wf_instance_status'))
const approveResultOptions = computed(() => getDict('wf_approve_result'))

const isCompact = computed(() => props.variant === 'compact')
const tasks = computed(() => props.instance?.tasks ?? [])
const cycles = computed(() => props.instance?.processCycles ?? [])
const canToggleHistory = computed(() => cycles.value.length > 1)

const expandLabel = computed(() => {
  const n = cycles.value.length
  const template = props.labels.expandHistory || ''
  if (!template) return String(n)
  return template.includes('{n}') ? template.replace(/\{n\}/g, String(n)) : template
})
const collapseLabel = computed(() => props.labels.collapseHistory || '')

watch(
  () => props.instance?.instanceId,
  () => {
    historyExpanded.value = false
  },
)

type TimelineItem = {
  key: string
  kind?: 'cycle' | 'task'
  cycleNo?: number
  title: string
  actionLabel?: string
  assignee?: string
  toUser?: string
  resultLabel?: string
  comment?: string
  timestamp: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  hollow?: boolean
}

const allTimelineItems = computed<TimelineItem[]>(() => {
  const items: TimelineItem[] = []
  const cycleList = cycles.value
  const taskList = tasks.value
  const multiCycle = cycleList.length > 1

  if (cycleList.length) {
    const byCycle = new Map<number, WorkflowHistoricTaskVO[]>()
    for (const task of taskList) {
      const no = task.cycleNo ?? 1
      const bucket = byCycle.get(no) ?? []
      bucket.push(task)
      byCycle.set(no, bucket)
    }
    for (const cycle of cycleList) {
      const no = cycle.cycleNo ?? 1
      items.push(cycleStartItem(cycle, multiCycle))
      for (const task of byCycle.get(no) ?? []) {
        items.push(taskItem(task, no))
      }
    }
    return items
  }

  const inst = props.instance
  if (inst && (inst.starterUserName || inst.startTime)) {
    items.push({
      key: 'start-event',
      kind: 'cycle',
      cycleNo: 1,
      title: props.labels.startEvent || 'start',
      assignee: inst.starterUserName,
      timestamp: formatTime(inst.startTime),
      type: 'success',
    })
  }
  for (const task of taskList) {
    items.push(taskItem(task, 1))
  }
  return items
})

const visibleTimelineItems = computed(() => {
  const all = allTimelineItems.value
  if (!canToggleHistory.value || historyExpanded.value) {
    return all
  }
  const latestNo = cycles.value[cycles.value.length - 1]?.cycleNo ?? 1
  return all.filter((item) => (item.cycleNo ?? 1) === latestNo)
})

function cycleStartItem(cycle: WorkflowProcessCycleVO, multiCycle: boolean): TimelineItem {
  const no = cycle.cycleNo ?? 1
  const base = props.labels.startEvent || 'start'
  const title = multiCycle ? formatCycleTitle(no, base) : base
  return {
    key: `cycle-${cycle.processInstanceId || no}`,
    kind: 'cycle',
    cycleNo: no,
    title,
    assignee: cycle.starterUserName,
    timestamp: formatTime(cycle.startTime),
    type: 'success',
  }
}

function formatCycleTitle(cycleNo: number, startEvent: string) {
  const template = props.labels.cycleSubmit
  if (template?.includes('{n}')) {
    return `${template.replace('{n}', String(cycleNo))} · ${startEvent}`
  }
  if (template) {
    return `${template} ${cycleNo} · ${startEvent}`
  }
  return `${startEvent} #${cycleNo}`
}

function taskItem(task: WorkflowHistoricTaskVO, cycleNo?: number): TimelineItem {
  const actionLabel = actionTypeLabel(task.actionType)
  const isAction = !!task.actionType
  return {
    key: task.taskId || `${task.processInstanceId}-${task.taskName}-${task.startTime}`,
    kind: 'task',
    cycleNo: cycleNo ?? task.cycleNo ?? 1,
    title:
      isAction && (task.actionType === 'withdraw' || task.actionType === 'terminate')
        ? actionLabel || task.taskName || '—'
        : task.taskName || '—',
    actionLabel:
      isAction && task.actionType !== 'withdraw' && task.actionType !== 'terminate'
        ? actionLabel
        : undefined,
    assignee: task.assigneeUserName || task.assigneeUserId,
    toUser: task.toUserName || task.toUserId,
    resultLabel: isAction
      ? undefined
      : task.approved == null
        ? undefined
        : task.approved
          ? 'pass'
          : 'reject',
    comment: resolveTimelineComment(task),
    timestamp: formatTime(task.endTime || task.startTime),
    type: timelineType(task),
    hollow: !!(props.currentTaskId && task.taskId === props.currentTaskId && task.approved == null),
  }
}

function resolveTimelineComment(task: WorkflowHistoricTaskVO) {
  if (task.comment?.trim()) return task.comment
  const code = task.reasonCode?.trim()
  if (!code) return undefined
  return reasonCodeLabel(code)
}

function reasonCodeLabel(code: string) {
  const fromBiz = props.reasonCodeLabels?.[code]
  if (fromBiz) return fromBiz
  const l = props.labels
  if (code === 'starter_withdraw') return l.reasonStarterWithdraw || l.reasonCancelled
  if (code === 'cancelled') return l.reasonCancelled
  return undefined
}

function actionTypeLabel(actionType?: string) {
  if (!actionType) return undefined
  const l = props.labels
  switch (actionType) {
    case 'transfer':
      return l.actionTransfer
    case 'delegate':
      return l.actionDelegate
    case 'delegate_return':
      return l.actionDelegateReturn
    case 'withdraw':
      return l.actionWithdraw
    case 'terminate':
      return l.actionTerminate
    case 'jump':
      return l.actionJump
    case 'reject_jump':
      return l.actionRejectJump
    case 'timeout':
      return l.actionTimeout
    case 'add_sign_before':
    case 'add_sign_after':
      return l.actionAddSign
    case 'add_sign_handoff':
      return l.actionAddSignHandoff
    case 'add_sign_return':
      return l.actionAddSignReturn
    case 'remove_sign':
      return l.actionRemoveSign
    default:
      return actionType
  }
}

function timelineType(task: WorkflowHistoricTaskVO) {
  if (props.currentTaskId && task.taskId === props.currentTaskId) return 'primary'
  if (
    task.actionType === 'withdraw' ||
    task.actionType === 'terminate' ||
    task.nodeStatus === 'cancelled'
  )
    return 'warning'
  if (
    task.actionType === 'transfer' ||
    task.actionType === 'delegate' ||
    task.actionType === 'jump'
  ) {
    return 'info'
  }
  if (task.actionType === 'reject_jump' || task.actionType === 'timeout') return 'danger'
  if (task.approved === false) return 'danger'
  if (task.approved === true) return 'success'
  if (task.nodeStatus === 'active' || task.nodeStatus === 'running') return 'primary'
  if (task.actionType) return 'info'
  return undefined
}

function formatTime(value?: string) {
  return value || '—'
}
</script>

<style scoped>
.workflow-progress-panel__summary {
  margin-bottom: 16px;
}

.workflow-progress-panel__title {
  font-weight: 600;
  font-size: 15px;
}

.workflow-progress-panel__subtitle {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
}

.workflow-progress-panel__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.workflow-progress-panel__meta--compact {
  margin: 0 0 12px;
}

.workflow-progress-panel__toggle {
  margin: 0 0 8px;
}

.workflow-progress-panel__timeline {
  margin-top: 8px;
}

.workflow-progress-panel.is-compact .workflow-progress-panel__timeline {
  margin-top: 0;
  padding-left: 4px;
}

.workflow-progress-panel__task-name {
  font-weight: 500;
}

.workflow-progress-panel__task-name.is-cycle {
  font-weight: 600;
}

.workflow-progress-panel__task-meta {
  margin-top: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.workflow-progress-panel__comment {
  margin-top: 6px;
  padding: 8px 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.5;
}
</style>
