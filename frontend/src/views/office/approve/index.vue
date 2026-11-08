<!-- 在线办公 · 统一 OA 审批页 — gen-page-shell + WorkflowApproveLayout -->
<template>
  <div class="gen-page-shell">
  <WorkflowApproveLayout
    class="office-approve-page"
    :loading="loading"
    :title="pageTitle"
    :description="documentTitle"
    :alert-message="!bizId ? gp('missingBizId') : undefined"
    show-progress
    :progress-title="gp('timelineTitle')"
    :approve-title="approvePanelTitle"
    :back-label="gp('back')"
    :current-node-label="currentNodeName"
    :current-node-prefix="gp('currentNodePrefix')"
    :info-tab-label="gp('bizPanel')"
    :flow-tab-label="gp('progressTabFlow')"
    :empty-flow-label="gp('emptyFlow')"
    :instance="detail?.instance as any"
    :field-labels="flowFieldLabels"
    :current-task-id="isViewMode ? undefined : taskId"
    :progress-labels="timelineLabels"
    :reason-code-labels="reasonCodeLabels"
    @back="goBack"
  >
    <template #business>
      <template v-if="detail?.document">
        <el-descriptions
          :column="2"
          border
          class="office-approve-page__meta"
          style="--oa-detail-label-width: 108px"
        >
          <el-descriptions-item :label="gf('instanceTitle', 'label')" :span="2">
            {{ detail.document.instanceTitle || '—' }}
          </el-descriptions-item>
          <el-descriptions-item :label="gf('approveStatus', 'label')" :span="2">
            <DictTag :options="approveStatusOptions" :value="detail.document.approveStatus" />
          </el-descriptions-item>
        </el-descriptions>
        <OaInstanceDetailView
          class="office-approve-page__biz-detail"
          :fields="detailViewFields"
          :form-data="editForm"
          :layout-snapshot-json="layoutSnapshotJson"
          :remark="detail.document.remark"
          :resolve-label="fieldLabel"
        />
      </template>
      <el-empty v-else :description="gp('emptyBiz')" />
    </template>

    <template #form>
      <template v-if="!isViewMode">
        <WorkflowApproveActionPanel
          v-model:approved="form.approved"
          v-model:comment="form.comment"
          :task-id="taskId"
          :submitting="submitting"
          :cc-mode="isCcTask"
          :pass-quick-items="passItems"
          :reject-quick-items="rejectItems"
          :decision-label="gf('approved', 'label')"
          :comment-label="gf('comment', 'label')"
          :comment-placeholder="commentPlaceholder"
          :quick-select-placeholder="gp('quickCommentPlaceholder')"
          :pass-label="gp('resultPass')"
          :reject-label="rejectButtonLabel"
          :submit-label="gp('submitApprove')"
          :cc-ack-label="gp('ccAck')"
          :task-id-label="gf('taskId', 'label')"
          :transfer-label="gp('actionTransfer')"
          :delegate-label="gp('actionDelegate')"
          :jump-label="gp('jumpTitle')"
          :add-sign-label="gp('actionAddSign')"
          :remove-sign-label="gp('actionRemoveSign')"
          :can-transfer="canTransfer"
          :can-delegate="canDelegate"
          :can-jump="canJump"
          :can-add-sign="canAddSign"
          :can-remove-sign="canRemoveSign"
          @submit="handleSubmitApprove"
          @ack="handleComplete(true)"
          @transfer="openTransfer"
          @delegate="openDelegate"
          @jump="openJump"
          @add-sign="openAddSign"
          @remove-sign="openRemoveSign"
        >
          <template v-if="writableFormFields.length" #prepend>
            <div class="office-approve-page__writable">
              <div class="office-approve-page__writable-title">{{ gp('writableFields') }}</div>
              <OaFormLayoutRuntime
                ref="writableFormRef"
                v-model="editForm"
                :fields="writableFormFields"
                :resolve-label="fieldLabel"
              />
            </div>
          </template>
        </WorkflowApproveActionPanel>
      </template>
      <div v-else-if="canUrge" class="office-approve-page__actions">
        <el-button
          type="warning"
          plain
          :disabled="!urgeTaskId || submitting"
          :loading="submitting && submitAction === 'urge'"
          @click="handleUrge"
        >
          {{ gp('actionUrge') }}
        </el-button>
      </div>
    </template>
  </WorkflowApproveLayout>

  <UserPickerDialog
    v-model="pickerVisible"
    :title="pickerTitle"
    :pending-title="gp('pickerPendingList')"
    :selected-text="gp('pickerSelected')"
    :add-current-text="gp('pickerAddCurrentPage')"
    :clear-text="gp('pickerClearSelected')"
    :search-placeholder="gp('pickerUserSearch')"
    :search-text="t('common.search')"
    :reset-text="t('common.reset')"
    :cancel-text="t('common.cancel')"
    :confirm-text="t('common.confirm')"
    :action-label="t('common.actions')"
    :dept-title="gp('pickerDeptTree')"
    :nickname-label="gp('pickerUserNickname')"
    :username-label="gp('pickerUserAccount')"
    @confirm="onUsersPicked"
  />

  <el-dialog v-model="jumpVisible" :title="gp('jumpTitle')" width="420px">
    <el-form label-position="top">
      <el-form-item :label="gp('targetActivityId')">
        <el-select v-model="jumpTargetActivityId" filterable style="width: 100%">
          <el-option
            v-for="opt in jumpNodeOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="jumpVisible = false">{{ t('common.cancel') }}</el-button>
      <el-button
        type="primary"
        :disabled="!jumpTargetActivityId"
        :loading="submitting"
        @click="confirmJump"
      >
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="delegateUntilVisible" :title="gp('delegateUntilTitle')" width="420px">
    <p class="pass-mode-hint">{{ gp('delegateUntilHint') }}</p>
    <el-date-picker
      v-model="delegateUntil"
      type="datetime"
      value-format="YYYY-MM-DDTHH:mm:ss"
      style="width: 100%"
      :placeholder="gp('delegateUntilPh')"
    />
    <template #footer>
      <el-button @click="delegateUntilVisible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" @click="confirmDelegate">
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="addSignModeVisible" :title="gp('addSignMode')" width="420px">
    <el-radio-group v-model="addSignMode">
      <el-radio value="before">{{ gp('addSignBefore') }}</el-radio>
      <el-radio value="after">{{ gp('addSignAfter') }}</el-radio>
    </el-radio-group>
    <template #footer>
      <el-button @click="addSignModeVisible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="confirmAddSignMode">{{ t('common.confirm') }}</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="removeSignVisible" :title="gp('removeSignTitle')" width="420px">
    <p class="pass-mode-hint">{{ gp('removeSignHint') }}</p>
    <el-checkbox-group v-if="parallelAssignees.length" v-model="removeSignUserIds">
      <div v-for="row in parallelAssignees" :key="row.userId" style="margin-bottom: 8px">
        <el-checkbox :value="row.userId">
          {{ row.userName || row.userId }}
        </el-checkbox>
      </div>
    </el-checkbox-group>
    <el-empty v-else :description="gp('removeSignEmpty')" :image-size="64" />
    <template #footer>
      <el-button @click="removeSignVisible = false">{{ t('common.cancel') }}</el-button>
      <el-button
        type="primary"
        :disabled="!removeSignUserIds.length"
        :loading="submitting"
        @click="confirmRemoveSign"
      >
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import DictTag from '@/components/DictTag.vue'
import WorkflowApproveLayout from '@/components/workflow/WorkflowApproveLayout.vue'
import WorkflowApproveActionPanel from '@/components/workflow/WorkflowApproveActionPanel.vue'
import UserPickerDialog, { type PickerUserVO } from '@/components/picker/UserPickerDialog.vue'
import OaFormLayoutRuntime from '@/views/oa/components/OaFormLayoutRuntime.vue'
import OaInstanceDetailView from '@/views/oa/components/OaInstanceDetailView.vue'
import { useApproveQuickComments } from '@/composables/useApproveQuickComments'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useDict } from '@/composables/useDict'
import {
  addSignWorkflowTask,
  completeWorkflowTask,
  delegateWorkflowTask,
  getWorkflowTask,
  jumpWorkflowTask,
  markWorkflowCcRead,
  PERM_WorkflowTodo,
  removeSignWorkflowTask,
  transferWorkflowTask,
  urgeWorkflowTask,
} from '@/api/workflow/task'
import { PERM_WorkflowSubmitted } from '@/api/workflow/instance'
import { getOaInstanceApproveDetail } from '@/api/oa/oaInstance'
import { OA_WORKFLOW_CANCEL_REASON } from '@/constants/oaWorkflowCancelReason'
import type { OaInstanceApproveDetailVO } from '@/types/oa/oaInstance'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'
import type { WorkflowTaskDetailVO } from '@/types/workflow/task'
import { categoryFieldLabel, resolveCategoryGenI18n } from '@/utils/oa/categoryGenI18n'
import { hasPermission } from '@/utils/permission'
import { navigateAfterWorkflowApprove } from '@/utils/workflowApproveNav'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'

const { gp, gf, gv } = useGenPageShellI18n('officeApprove')
const { locale, t } = useI18n()
const userStore = useUserStore()

function fieldLabel(field: OaFieldDefVO) {
  const code = field.fieldCode || ''
  const category = detail.value?.document?.categoryCode
  const pack = resolveCategoryGenI18n(category)
  const messages = pack?.[locale.value] ?? pack?.['zh-CN'] ?? pack?.['en-US']
  return categoryFieldLabel(messages, code, field.fieldName || code)
}

const detail = ref<OaInstanceApproveDetailVO>()
const taskMeta = ref<WorkflowTaskDetailVO>()
const route = useRoute()
const router = useRouter()
const isViewMode = computed(() => String(route.query.mode ?? '') === 'view')
const isCcFromQuery = computed(() => String(route.query.from ?? '') === 'cc')
const taskCcFlag = ref(false)
const isCcTask = computed(() => isCcFromQuery.value || taskCcFlag.value)

const displayFields = computed(() => {
  const list = (detail.value?.fields ?? detail.value?.document?.fields ?? []) as OaFieldDefVO[]
  return [...list].sort((a, b) => (a.sortNo ?? 0) - (b.sortNo ?? 0))
})

/** 流程图条件：请假天数 而非 leaveDays */
const flowFieldLabels = computed(() => {
  const map: Record<string, string> = {}
  for (const f of displayFields.value) {
    if (f.fieldCode && f.fieldName) map[f.fieldCode] = f.fieldName
  }
  return map
})

function fieldPermOf(field: OaFieldDefVO): 'R' | 'W' | 'H' {
  const p = field.fieldPerm
  return p === 'W' || p === 'H' ? p : 'R'
}

/** 业务详情区：查看/抄送展示全部可见字段；审批中可写字段放到右侧面板编辑 */
const detailViewFields = computed(() => {
  if (isViewMode.value || isCcTask.value) return displayFields.value
  return displayFields.value.filter((f) => fieldPermOf(f) !== 'W')
})

const writableFormFields = computed(() => {
  if (isViewMode.value || isCcTask.value) return [] as OaFieldDefVO[]
  return displayFields.value.filter((f) => fieldPermOf(f) === 'W')
})

const dictTypes = computed(() => ['oa_approve_status'])
const { getDict } = useDict(dictTypes)
const approveStatusOptions = computed(() => getDict('oa_approve_status'))

const timelineLabels = computed(() => ({
  starter: gp('timelineStarter'),
  assignee: gp('timelineAssignee'),
  toUser: gp('timelineToUser'),
  result: gp('timelineResult'),
  emptyTimeline: gp('emptyTimeline'),
  emptyInstance: gp('emptyInstance'),
  pending: gp('nodePending'),
  skipped: gp('nodeSkipped'),
  waitingAssignee: gp('waitingAssignee'),
  pass: gp('resultPass'),
  reject: gp('resultReject'),
  startEvent: gp('startEvent'),
  cycleSubmit: gp('cycleSubmit'),
  expandHistory: gp('expandHistory'),
  collapseHistory: gp('collapseHistory'),
  actionTransfer: gp('timelineActionTransfer'),
  actionDelegate: gp('timelineActionDelegate'),
  actionDelegateReturn: gp('timelineActionDelegateReturn'),
  actionWithdraw: gp('timelineActionWithdraw'),
  actionTerminate: gp('timelineActionTerminate'),
  actionJump: gp('timelineActionJump'),
  actionRejectJump: gp('timelineActionRejectJump'),
  actionTimeout: gp('timelineActionTimeout'),
  actionAddSign: gp('timelineActionAddSign'),
  actionAddSignHandoff: gp('timelineActionAddSignHandoff'),
  actionAddSignReturn: gp('timelineActionAddSignReturn'),
  actionRemoveSign: gp('timelineActionRemoveSign'),
  reasonStarterWithdraw: gp('timelineReasonStarterWithdraw'),
  reasonCancelled: gp('timelineReasonCancelled'),
}))

const reasonCodeLabels = computed<Record<string, string>>(() => ({
  [OA_WORKFLOW_CANCEL_REASON.withdraw]: gp('timelineReasonOaInstanceWithdraw'),
  [OA_WORKFLOW_CANCEL_REASON.closeAfterReturn]: gp('timelineReasonOaCloseAfterReturn'),
}))

const loading = ref(false)
const submitting = ref(false)
const submitAction = ref<'pass' | 'reject' | 'urge' | null>(null)
const rejectIntent = ref(false)
const form = reactive({ approved: 'pass' as 'pass' | 'reject', comment: '' })
const editForm = ref<Record<string, unknown>>({})
const writableFormRef = ref<InstanceType<typeof OaFormLayoutRuntime>>()
const layoutSnapshotJson = computed(
  () => detail.value?.document?.layoutSnapshotJson ?? null,
)
const { passItems, rejectItems } = useApproveQuickComments()

const pickerVisible = ref(false)
const pickerMode = ref<'transfer' | 'delegate' | 'addSign'>('transfer')
const addSignModeVisible = ref(false)
const addSignMode = ref<'before' | 'after'>('before')
const removeSignVisible = ref(false)
const removeSignUserIds = ref<string[]>([])
const jumpVisible = ref(false)
const jumpTargetActivityId = ref('')

const taskId = computed(() => {
  if (isViewMode.value) return ''
  return String(route.query.taskId ?? detail.value?.taskId ?? '')
})
const urgeTaskId = computed(() => {
  if (taskId.value) return taskId.value
  const fromDetail = String(detail.value?.taskId ?? '')
  if (fromDetail) return fromDetail
  const nodes =
    (
      detail.value?.instance as
        { progressNodes?: { nodeStatus?: string; taskId?: string }[] } | undefined
    )?.progressNodes ?? []
  const active = nodes.find((n) => n.nodeStatus === 'active' && n.taskId)
  return active?.taskId ? String(active.taskId) : ''
})
const bizId = computed(() => String(route.query.bizId ?? ''))

const pageTitle = computed(() => {
  if (isViewMode.value) return gp('viewTitle')
  return isCcTask.value ? gp('ccTitle') : gp('title')
})
const approvePanelTitle = computed(() => (isCcTask.value ? gp('ccPanel') : gp('approvePanel')))
const documentTitle = computed(() => detail.value?.document?.instanceTitle?.trim() || undefined)

const rejectButtonLabel = computed(() => {
  const mode = taskMeta.value?.rejectMode || 'end'
  if (mode === 'starterEdit') return gp('approveRejectStarterEdit') || gp('approveRejectReturn')
  return mode !== 'end' ? gp('approveRejectReturn') : gp('approveReject')
})
const canTransfer = computed(() => taskMeta.value?.allowTransfer === true)
const canDelegate = computed(() => taskMeta.value?.allowDelegate === true)
const jumpNodeOptions = computed(() => {
  const nodes =
    (
      detail.value?.instance as
        { progressNodes?: { nodeKey?: string; nodeName?: string }[] } | undefined
    )?.progressNodes ?? []
  const seen = new Set<string>()
  const opts: { value: string; label: string }[] = []
  for (const n of nodes) {
    const key = n.nodeKey?.trim()
    if (!key || seen.has(key)) continue
    seen.add(key)
    opts.push({ value: key, label: n.nodeName ? `${n.nodeName} (${key})` : key })
  }
  return opts
})
const canJump = computed(
  () =>
    Boolean(taskId.value) &&
    jumpNodeOptions.value.length > 0 &&
    hasPermission(userStore.permissions, PERM_WorkflowTodo.jump),
)
const canAddSign = computed(() => taskMeta.value?.allowAddSign === true)
const parallelAssignees = computed(() => taskMeta.value?.parallelAssignees ?? [])
const canRemoveSign = computed(
  () => taskMeta.value?.allowRemoveSign === true && parallelAssignees.value.length > 0,
)
const canUrge = computed(() => {
  if (detail.value?.instance?.onUrge === false) return false
  const hasUrgePerm = hasPermission(userStore.permissions, PERM_WorkflowTodo.urge)
  const hasSubmittedList = hasPermission(userStore.permissions, PERM_WorkflowSubmitted.list)
  return Boolean(urgeTaskId.value) && (hasUrgePerm || hasSubmittedList)
})
const pickerTitle = computed(() => {
  if (pickerMode.value === 'delegate') return gp('delegateTitle')
  if (pickerMode.value === 'transfer') return gp('transferTitle')
  return gp('addSignTitle')
})

const commentPlaceholder = computed(() => {
  if (isCcTask.value) return gp('ccCommentPlaceholder')
  return form.approved === 'reject'
    ? gf('comment', 'placeholderReject') || gf('comment', 'placeholder')
    : gf('comment', 'placeholder')
})

function handleSubmitApprove() {
  return handleComplete(form.approved === 'pass')
}

const currentNodeName = computed(() => {
  const instance = detail.value?.instance as
    | {
        progressNodes?: { nodeStatus?: string; nodeName?: string }[]
        tasks?: { taskId?: string; taskName?: string }[]
      }
    | undefined
  const nodes = instance?.progressNodes ?? []
  const active = nodes.find((n) => n.nodeStatus === 'active')
  if (active?.nodeName) return active.nodeName
  const tid = taskId.value || urgeTaskId.value
  if (!tid) return undefined
  const match = instance?.tasks?.find((t) => t.taskId === tid)
  return match?.taskName
})

async function loadDetail() {
  if (!bizId.value) return
  loading.value = true
  try {
    const res = await getOaInstanceApproveDetail(bizId.value)
    detail.value = res.data.data
    const data = detail.value?.formData ?? detail.value?.document?.formData ?? {}
    editForm.value = { ...data }
    const tid = String(route.query.taskId ?? '')
    if (tid && !isViewMode.value) {
      try {
        const taskRes = await getWorkflowTask(tid)
        taskMeta.value = taskRes.data.data
        taskCcFlag.value = Boolean(taskRes.data.data?.ccTask)
        if (isCcFromQuery.value || taskRes.data.data?.ccTask) {
          try {
            await markWorkflowCcRead(tid)
          } catch {
            /* non-blocking */
          }
        }
      } catch {
        taskCcFlag.value = isCcFromQuery.value
      }
    }
  } finally {
    loading.value = false
  }
}

async function handleComplete(approved: boolean) {
  if (!taskId.value) {
    ElMessage.warning(gp('missingTaskId'))
    return
  }
  rejectIntent.value = !approved
  if (!isCcTask.value && !approved && !form.comment.trim()) {
    ElMessage.warning(gv('comment', 'requiredOnReject') || gf('comment', 'placeholderReject'))
    return
  }
  if (
    !isCcTask.value &&
    approved &&
    detail.value?.instance?.commentRequired &&
    !form.comment.trim()
  ) {
    ElMessage.warning(gf('comment', 'placeholder') || gp('commentRequired'))
    return
  }
  if (writableFormFields.value.length) {
    const ok = await writableFormRef.value?.validate().catch(() => false)
    if (!ok) return
  }
  submitting.value = true
  submitAction.value = approved ? 'pass' : 'reject'
  try {
    const formData: Record<string, unknown> = {}
    for (const field of writableFormFields.value) {
      if (!field.fieldCode) continue
      formData[field.fieldCode] = editForm.value[field.fieldCode]
    }
    await completeWorkflowTask(taskId.value, {
      approved: isCcTask.value ? true : approved,
      comment: form.comment.trim() || undefined,
      formData: Object.keys(formData).length ? formData : undefined,
    })
    ElMessage.success(isCcTask.value ? gp('ccCompleteSuccess') : gp('completeSuccess'))
    goBack()
  } finally {
    submitting.value = false
    submitAction.value = null
  }
}

function openTransfer() {
  pickerMode.value = 'transfer'
  pickerVisible.value = true
}

const delegateUntilVisible = ref(false)
const delegateUntil = ref('')
const pendingDelegateUserId = ref('')

async function confirmDelegate() {
  if (!taskId.value || !pendingDelegateUserId.value) return
  submitting.value = true
  try {
    await delegateWorkflowTask(taskId.value, {
      targetUserId: pendingDelegateUserId.value,
      comment: form.comment.trim() || undefined,
      delegateUntil: delegateUntil.value || undefined,
    })
    ElMessage.success(gp('delegateSuccess'))
    delegateUntilVisible.value = false
    goBack()
  } finally {
    submitting.value = false
  }
}

function openDelegate() {
  pickerMode.value = 'delegate'
  pickerVisible.value = true
}

function openJump() {
  jumpTargetActivityId.value = jumpNodeOptions.value[0]?.value || ''
  jumpVisible.value = true
}

function openAddSign() {
  addSignMode.value = 'before'
  addSignModeVisible.value = true
}

function confirmAddSignMode() {
  addSignModeVisible.value = false
  pickerMode.value = 'addSign'
  pickerVisible.value = true
}

function openRemoveSign() {
  removeSignUserIds.value = []
  removeSignVisible.value = true
}

async function confirmRemoveSign() {
  if (!taskId.value || !removeSignUserIds.value.length) return
  submitting.value = true
  try {
    await removeSignWorkflowTask(taskId.value, {
      userIds: [...removeSignUserIds.value],
      comment: form.comment.trim() || undefined,
    })
    ElMessage.success(gp('removeSignSuccess'))
    removeSignVisible.value = false
    await loadDetail()
  } finally {
    submitting.value = false
  }
}

async function onUsersPicked(users: PickerUserVO[]) {
  const ids = users.map((u) => u.id!).filter(Boolean)
  if (!ids.length || !taskId.value) return
  if (pickerMode.value === 'delegate') {
    pendingDelegateUserId.value = ids[0]
    delegateUntil.value = ''
    delegateUntilVisible.value = true
    return
  }
  submitting.value = true
  try {
    if (pickerMode.value === 'transfer') {
      await transferWorkflowTask(taskId.value, {
        targetUserId: ids[0],
        comment: form.comment.trim() || undefined,
      })
      ElMessage.success(gp('transferSuccess'))
      goBack()
    } else {
      await addSignWorkflowTask(taskId.value, {
        mode: addSignMode.value,
        userIds: ids,
        comment: form.comment.trim() || undefined,
      })
      ElMessage.success(gp('addSignSuccess'))
      goBack()
    }
  } finally {
    submitting.value = false
  }
}

async function confirmJump() {
  if (!taskId.value || !jumpTargetActivityId.value) return
  submitting.value = true
  try {
    await jumpWorkflowTask(taskId.value, {
      targetActivityId: jumpTargetActivityId.value,
      comment: form.comment.trim() || undefined,
    })
    ElMessage.success(gp('jumpSuccess'))
    jumpVisible.value = false
    await loadDetail()
  } finally {
    submitting.value = false
  }
}

async function handleUrge() {
  if (!urgeTaskId.value) {
    ElMessage.warning(gp('missingTaskId'))
    return
  }
  await ElMessageBox.confirm(gp('urgeConfirm'), t('common.tip'))
  submitting.value = true
  submitAction.value = 'urge'
  try {
    await urgeWorkflowTask(urgeTaskId.value, {
      comment: form.comment.trim() || undefined,
    })
    ElMessage.success(gp('urgeSuccess'))
  } finally {
    submitting.value = false
    submitAction.value = null
  }
}

function goBack() {
  navigateAfterWorkflowApprove(router, {
    from: route.query.from as string | undefined,
    viewMode: isViewMode.value,
    viewFallback: 'submitted',
  })
}

onMounted(loadDetail)
</script>

<style scoped>
.office-approve-page__card {
  margin-bottom: 16px;
}

.office-approve-page__form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.office-approve-page__meta {
  margin-bottom: 16px;

  :deep(.el-descriptions__label) {
    width: var(--oa-detail-label-width, 108px);
    min-width: var(--oa-detail-label-width, 108px);
    max-width: var(--oa-detail-label-width, 108px);
    box-sizing: border-box;
    font-weight: 500;
  }
}

.office-approve-page__biz-detail {
  margin-top: 0;
}

.office-approve-page__writable {
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.office-approve-page__writable-title {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.office-approve-page__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
