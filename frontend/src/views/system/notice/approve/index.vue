<!-- 公告审批页 — gen-page-shell + WorkflowApproveLayout -->
<template>
  <div class="gen-page-shell">
  <WorkflowApproveLayout
    class="notice-approve-page"
    :loading="loading"
    :title="isViewMode ? gp('viewTitle') : gp('title')"
    :description="noticeTitle"
    :alert-message="!bizId ? gp('missingBizId') : undefined"
    show-progress
    :progress-title="gp('timelinePanel')"
    :approve-title="gp('approvePanel')"
    :current-node-label="currentNodeName"
    :info-tab-label="gp('noticePanel')"
    :flow-tab-label="gp('progressTabFlow')"
    :empty-flow-label="gp('emptyFlow')"
    :instance="detail?.instance"
    :current-task-id="isViewMode ? undefined : taskId"
    :progress-labels="timelineLabels"
    :reason-code-labels="reasonCodeLabels"
    @back="goBack"
  >
    <template #business>
      <template v-if="detail?.notice">
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="gf('noticeTitle', 'label')" :span="2">
            {{ detail.notice.noticeTitle || '—' }}
          </el-descriptions-item>
          <el-descriptions-item :label="gf('approveStatus', 'label')">
            <DictTag :options="approveStatusOptions" :value="detail.notice.approveStatus" />
          </el-descriptions-item>
          <el-descriptions-item :label="gf('noticeType', 'label')">
            <DictTag :options="noticeTypeOptions" :value="detail.notice.noticeType" />
          </el-descriptions-item>
          <el-descriptions-item :label="gf('publishLocations', 'label')" :span="2">
            {{ detail.notice.publishLocations || '—' }}
          </el-descriptions-item>
          <el-descriptions-item :label="gf('noticeSummary', 'label')" :span="2">
            {{ detail.notice.noticeSummary || '—' }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="detail.notice.coverPreviewUrl"
            :label="gf('coverPreviewUrl', 'label')"
            :span="2"
          >
            <AuthImage
              :src="detail.notice.coverPreviewUrl"
              download-scope="notice"
              fit="cover"
              image-class="notice-approve-page__cover"
              previewable
            />
          </el-descriptions-item>
        </el-descriptions>
        <div class="notice-approve-page__content-block">
          <div class="notice-approve-page__content-label">{{ gf('noticeContent', 'label') }}</div>
          <RichHtmlContent :active="true" :html="detail.notice.noticeContent" />
        </div>
      </template>
      <el-empty v-else :description="gp('emptyNotice')" />
    </template>

    <template v-if="!isViewMode" #form>
      <WorkflowApproveActionPanel
        v-model:approved="form.approved"
        v-model:comment="form.comment"
        :task-id="taskId"
        :submitting="submitting"
        :pass-quick-items="passItems"
        :reject-quick-items="rejectItems"
        :decision-label="gf('approved', 'label')"
        :comment-label="gf('comment', 'label')"
        :comment-placeholder="commentPlaceholder"
        :quick-select-placeholder="gp('quickCommentPlaceholder')"
        :pass-label="gp('resultPass')"
        :reject-label="gp('resultReject')"
        :submit-label="gp('submitApprove')"
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
        @submit="handleComplete"
        @transfer="openTransfer"
        @delegate="openDelegate"
        @jump="openJump"
        @add-sign="openAddSign"
        @remove-sign="openRemoveSign"
      />
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
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import DictTag from '@/components/DictTag.vue'
import AuthImage from '@/components/AuthImage.vue'
import RichHtmlContent from '@/components/RichHtmlContent.vue'
import WorkflowApproveLayout from '@/components/workflow/WorkflowApproveLayout.vue'
import WorkflowApproveActionPanel from '@/components/workflow/WorkflowApproveActionPanel.vue'
import UserPickerDialog, { type PickerUserVO } from '@/components/picker/UserPickerDialog.vue'
import { useApproveQuickComments } from '@/composables/useApproveQuickComments'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useDict } from '@/composables/useDict'
import {
  addSignWorkflowTask,
  completeWorkflowTask,
  delegateWorkflowTask,
  getWorkflowTask,
  jumpWorkflowTask,
  PERM_WorkflowTodo,
  removeSignWorkflowTask,
  transferWorkflowTask,
} from '@/api/workflow/task'
import { getNoticeApproveDetail } from '@/api/system/notice'
import { NOTICE_WORKFLOW_CANCEL_REASON } from '@/constants/noticeWorkflowCancelReason'
import type { NoticeApproveDetailVO } from '@/types/system/notice'
import type { WorkflowTaskDetailVO } from '@/types/workflow/task'
import { hasPermission } from '@/utils/permission'
import { navigateAfterWorkflowApprove } from '@/utils/workflowApproveNav'
import { useUserStore } from '@/stores/user'

const { gp, gf, gv } = useGenPageShellI18n('noticeApprove')
const { t } = useI18n()
const userStore = useUserStore()
const { getDict } = useDict(['sys_notice_approve_status', 'sys_notice_type'])
const { passItems, rejectItems } = useApproveQuickComments()
const approveStatusOptions = computed(() => getDict('sys_notice_approve_status'))
const noticeTypeOptions = computed(() => getDict('sys_notice_type'))

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const detail = ref<NoticeApproveDetailVO>()
const taskMeta = ref<WorkflowTaskDetailVO>()
const form = reactive({
  approved: 'pass' as 'pass' | 'reject',
  comment: '',
})
const pickerVisible = ref(false)
const pickerMode = ref<'transfer' | 'delegate' | 'addSign'>('transfer')
const addSignModeVisible = ref(false)
const addSignMode = ref<'before' | 'after'>('before')
const removeSignVisible = ref(false)
const removeSignUserIds = ref<string[]>([])
const jumpVisible = ref(false)
const jumpTargetActivityId = ref('')
const pickerTitle = computed(() => {
  if (pickerMode.value === 'delegate') return gp('delegateTitle')
  if (pickerMode.value === 'transfer') return gp('transferTitle')
  return gp('addSignTitle')
})
const canTransfer = computed(() => taskMeta.value?.allowTransfer === true)
const canDelegate = computed(() => taskMeta.value?.allowDelegate === true)
const jumpNodeOptions = computed(() => {
  const nodes = detail.value?.instance?.progressNodes ?? []
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

const commentPlaceholder = computed(() =>
  form.approved === 'reject'
    ? gf('comment', 'placeholderReject') || gf('comment', 'placeholder')
    : gf('comment', 'placeholder'),
)

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
  [NOTICE_WORKFLOW_CANCEL_REASON.withdraw]: gp('timelineReasonNoticeWithdraw'),
  [NOTICE_WORKFLOW_CANCEL_REASON.closeAfterReturn]: gp('timelineReasonNoticeCloseAfterReturn'),
}))

const isViewMode = computed(() => String(route.query.mode ?? '') === 'view')
const taskId = computed(() => {
  if (isViewMode.value) return ''
  return String(route.query.taskId ?? detail.value?.taskId ?? '')
})
const bizId = computed(() => String(route.query.bizId ?? ''))
const noticeTitle = computed(() => detail.value?.notice?.noticeTitle?.trim() || undefined)

const currentNodeName = computed(() => {
  const nodes = detail.value?.instance?.progressNodes ?? []
  const active = nodes.find((n) => n.nodeStatus === 'active')
  if (active?.nodeName) return active.nodeName
  const tid = taskId.value
  if (!tid) return undefined
  const match = detail.value?.instance?.tasks?.find((t) => t.taskId === tid)
  return match?.taskName
})

async function loadDetail() {
  if (!bizId.value) return
  loading.value = true
  try {
    const res = await getNoticeApproveDetail(bizId.value)
    detail.value = res.data.data
    const tid = String(route.query.taskId ?? detail.value?.taskId ?? '')
    taskMeta.value = undefined
    if (tid && !isViewMode.value) {
      try {
        const taskRes = await getWorkflowTask(tid)
        taskMeta.value = taskRes.data.data
      } catch {
        taskMeta.value = undefined
      }
    }
  } finally {
    loading.value = false
  }
}

async function handleComplete() {
  if (!taskId.value) {
    ElMessage.warning(gp('missingTaskId'))
    return
  }
  const approved = form.approved === 'pass'
  if (!approved && !String(form.comment || '').trim()) {
    ElMessage.warning(gv('comment', 'requiredOnReject') || gf('comment', 'placeholderReject'))
    return
  }
  if (approved && detail.value?.instance?.commentRequired && !String(form.comment || '').trim()) {
    ElMessage.warning(gf('comment', 'placeholder'))
    return
  }
  submitting.value = true
  try {
    await completeWorkflowTask(taskId.value, {
      approved,
      comment: String(form.comment || '').trim() || undefined,
    })
    ElMessage.success(gp('completeSuccess'))
    goBack()
  } finally {
    submitting.value = false
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
      comment: String(form.comment || '').trim() || undefined,
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
      comment: String(form.comment || '').trim() || undefined,
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
        comment: String(form.comment || '').trim() || undefined,
      })
      ElMessage.success(gp('transferSuccess'))
    } else {
      await addSignWorkflowTask(taskId.value, {
        mode: addSignMode.value,
        userIds: ids,
        comment: String(form.comment || '').trim() || undefined,
      })
      ElMessage.success(gp('addSignSuccess'))
    }
    goBack()
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
      comment: String(form.comment || '').trim() || undefined,
    })
    ElMessage.success(gp('jumpSuccess'))
    jumpVisible.value = false
    await loadDetail()
  } finally {
    submitting.value = false
  }
}

function goBack() {
  navigateAfterWorkflowApprove(router, {
    from: route.query.from as string | undefined,
    viewMode: isViewMode.value,
    viewFallback: 'done',
  })
}

onMounted(loadDetail)
</script>

<style scoped>
.notice-approve-page__card {
  margin-bottom: 16px;
}

.notice-approve-page__cover {
  width: 160px;
  height: 90px;
  border-radius: 4px;
}

.notice-approve-page__content-block {
  margin-top: 16px;
}

.notice-approve-page__content-label {
  margin-bottom: 8px;
  font-weight: 500;
}
</style>
