<!--
  统一审批操作区：结论 → 常用意见（可筛选下拉，适配多/长文案）→ 意见 → 提交审批；
  次要操作与任务 ID 次要展示。抄送模式仅「已知晓」。
-->
<template>
  <el-form label-position="top" class="wf-approve-action" @submit.prevent>
    <slot name="prepend" />

    <template v-if="ccMode">
      <el-form-item :label="commentLabel">
        <el-input
          v-model="commentProxy"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          :placeholder="commentPlaceholder"
          :disabled="disabled"
        />
      </el-form-item>
      <div class="wf-approve-action__actions">
        <el-button
          type="primary"
          :disabled="disabled || !taskId"
          :loading="submitting"
          @click="emit('ack')"
        >
          {{ ccAckLabel }}
        </el-button>
      </div>
    </template>

    <template v-else>
      <el-form-item :label="decisionLabel" required>
        <el-radio-group v-model="approvedProxy" :disabled="disabled" class="wf-approve-action__decision">
          <el-radio value="pass" class="wf-approve-action__radio is-pass">
            {{ passLabel }}
          </el-radio>
          <el-radio value="reject" class="wf-approve-action__radio is-reject">
            {{ rejectLabel }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="commentLabel">
        <el-select
          v-if="quickItems.length"
          :model-value="quickSelectValue"
          filterable
          clearable
          class="wf-approve-action__quick"
          :placeholder="quickSelectPlaceholder"
          :disabled="disabled"
          @change="onQuickSelect"
        >
          <el-option
            v-for="item in quickItems"
            :key="item.id"
            :label="item.text"
            :value="item.text"
          >
            <span class="wf-approve-action__opt" :title="item.text">{{ item.text }}</span>
          </el-option>
        </el-select>
        <el-input
          v-model="commentProxy"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          :placeholder="commentPlaceholder"
          :disabled="disabled"
        />
      </el-form-item>

      <div class="wf-approve-action__actions">
        <el-button
          type="primary"
          :disabled="disabled || !taskId"
          :loading="submitting"
          @click="emit('submit')"
        >
          {{ submitLabel }}
        </el-button>
        <el-button
          v-if="canTransfer"
          plain
          :disabled="disabled || !taskId"
          v-hasPermi="[permTransfer]"
          @click="emit('transfer')"
        >
          {{ transferLabel }}
        </el-button>
        <el-button
          v-if="canDelegate"
          plain
          :disabled="disabled || !taskId"
          v-hasPermi="[permDelegate]"
          @click="emit('delegate')"
        >
          {{ delegateLabel }}
        </el-button>
        <el-button
          v-if="canJump"
          plain
          :disabled="disabled || !taskId"
          v-hasPermi="[permJump]"
          @click="emit('jump')"
        >
          {{ jumpLabel }}
        </el-button>
        <el-button
          v-if="canAddSign"
          plain
          :disabled="disabled || !taskId"
          v-hasPermi="[permAddSign]"
          @click="emit('addSign')"
        >
          {{ addSignLabel }}
        </el-button>
        <el-button
          v-if="canRemoveSign"
          plain
          :disabled="disabled || !taskId"
          v-hasPermi="[permRemoveSign]"
          @click="emit('removeSign')"
        >
          {{ removeSignLabel }}
        </el-button>
      </div>
    </template>

    <div v-if="taskId" class="wf-approve-action__meta">
      <span>{{ taskIdLabel }}</span>
      <code :title="taskId">{{ taskId }}</code>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { PERM_WorkflowTodo } from '@/api/workflow/task'

export type ApproveDecision = 'pass' | 'reject'

const props = withDefaults(
  defineProps<{
    approved: ApproveDecision
    comment: string
    taskId?: string
    submitting?: boolean
    disabled?: boolean
    ccMode?: boolean
    passQuickItems?: { id: string; text: string }[]
    rejectQuickItems?: { id: string; text: string }[]
    decisionLabel: string
    commentLabel: string
    commentPlaceholder?: string
    quickSelectPlaceholder: string
    passLabel: string
    rejectLabel: string
    submitLabel: string
    ccAckLabel?: string
    taskIdLabel: string
    transferLabel: string
    delegateLabel: string
    jumpLabel: string
    addSignLabel: string
    removeSignLabel: string
    canTransfer?: boolean
    canDelegate?: boolean
    canJump?: boolean
    canAddSign?: boolean
    canRemoveSign?: boolean
  }>(),
  {
    submitting: false,
    disabled: false,
    ccMode: false,
    passQuickItems: () => [],
    rejectQuickItems: () => [],
    canTransfer: false,
    canDelegate: false,
    canJump: false,
    canAddSign: false,
    canRemoveSign: false,
  },
)

const emit = defineEmits<{
  'update:approved': [value: ApproveDecision]
  'update:comment': [value: string]
  submit: []
  ack: []
  transfer: []
  delegate: []
  jump: []
  addSign: []
  removeSign: []
}>()

const permTransfer = PERM_WorkflowTodo.transfer
const permDelegate = PERM_WorkflowTodo.delegate
const permJump = PERM_WorkflowTodo.jump
const permAddSign = PERM_WorkflowTodo.addSign
const permRemoveSign = PERM_WorkflowTodo.removeSign

const quickSelectValue = ref<string | undefined>()

const approvedProxy = computed({
  get: () => props.approved,
  set: (v: ApproveDecision) => emit('update:approved', v),
})

const commentProxy = computed({
  get: () => props.comment,
  set: (v: string) => emit('update:comment', v),
})

const quickItems = computed(() =>
  props.approved === 'reject' ? props.rejectQuickItems : props.passQuickItems,
)

watch(
  () => props.approved,
  () => {
    quickSelectValue.value = undefined
  },
)

function onQuickSelect(text: string | undefined) {
  quickSelectValue.value = text
  if (text) emit('update:comment', text)
}
</script>

<style scoped>
.wf-approve-action__decision {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 20px;
  align-items: center;
}

.wf-approve-action__radio.is-pass :deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.wf-approve-action__radio.is-pass :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: var(--el-color-primary);
  font-weight: 600;
}

.wf-approve-action__radio.is-reject :deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: var(--el-color-danger);
  border-color: var(--el-color-danger);
}

.wf-approve-action__radio.is-reject :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: var(--el-color-danger);
  font-weight: 600;
}

.wf-approve-action__radio.is-reject:not(.is-disabled):hover :deep(.el-radio__inner) {
  border-color: var(--el-color-danger);
}

.wf-approve-action__quick {
  width: 100%;
  margin-bottom: 8px;
}

.wf-approve-action__opt {
  display: block;
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wf-approve-action__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.wf-approve-action__meta {
  margin-top: 14px;
  padding-top: 10px;
  border-top: 1px dashed var(--el-border-color-lighter);
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
  word-break: break-all;
}

.wf-approve-action__meta code {
  flex: 1;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 11px;
  color: var(--el-text-color-regular);
}
</style>
