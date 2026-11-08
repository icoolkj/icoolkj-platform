<template>
  <GenFormShell
    v-model="visibleModel"
    :title="dialogTitle"
    :width="720"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item :label="gf('modelId', 'label')" prop="modelId">
        <el-select
          v-model="form.modelId"
          :placeholder="gf('modelId', 'placeholder')"
          :disabled="!!ruleId"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="item in modelOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="gf('taskDefinitionKey', 'label')" prop="taskDefinitionKey">
        <el-input
          v-model="form.taskDefinitionKey"
          :placeholder="gf('taskDefinitionKey', 'placeholder')"
          :disabled="!!ruleId"
          maxlength="64"
        />
      </el-form-item>
      <el-form-item :label="gf('variableName', 'label')" prop="variableName">
        <el-input
          v-model="form.variableName"
          :placeholder="gf('variableName', 'placeholder')"
          maxlength="64"
        />
      </el-form-item>
      <el-form-item :label="gf('strategyType', 'label')" prop="strategyType">
        <el-select
          v-model="form.strategyType"
          :placeholder="gf('strategyType', 'placeholder')"
          style="width: 100%"
        >
          <el-option
            v-for="item in strategyOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="gf('strategyConfig', 'label')" prop="strategyConfig">
        <el-input
          v-model="form.strategyConfig"
          type="textarea"
          :rows="4"
          :placeholder="strategyConfigHint"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>
      <el-form-item :label="gf('resolveScope', 'label')" prop="resolveScope">
        <el-select
          v-model="form.resolveScope"
          :placeholder="gf('resolveScope', 'placeholder')"
          style="width: 100%"
        >
          <el-option
            v-for="item in resolveScopeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="gf('sortNo', 'label')" prop="sortNo">
        <el-input-number v-model="form.sortNo" :min="0" controls-position="right" />
      </el-form-item>
      <el-form-item :label="gf('status', 'label')" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio v-for="item in statusOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="gf('remark', 'label')" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          :placeholder="gf('remark', 'placeholder')"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visibleModel = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { useDict } from '@/composables/useDict'
import { STATUS_NORMAL } from '@/constants/flags'
import type {
  WorkflowApproverRuleForm,
  WorkflowApproverRuleUpdateForm,
  WorkflowModelOptionVO,
} from '@/types/workflow/approverRule'
import type { GenFieldValidation } from '@/types/tool/genI18n'
import {
  createWorkflowApproverRule,
  getWorkflowApproverRule,
  listWorkflowApproverRuleModelOptions,
  updateWorkflowApproverRule,
} from '@/api/workflow/approverRule'

const props = defineProps<{
  modelValue: boolean
  ruleId?: string
  /** 新建时预填模型（向导内使用） */
  presetModelId?: string
  /** 新建时预填节点 ID（向导内使用） */
  presetTaskKey?: string
  gp: (key: string, params?: Record<string, string | number>) => string
  gf: (field: string, prop?: 'label' | 'placeholder') => string
  gv: (field: string, rule: keyof GenFieldValidation) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  success: []
}>()

const { t } = useI18n()
const { getDict } = useDict([
  'wf_approver_strategy',
  'wf_approver_resolve_scope',
  'sys_common_status',
])

const formRef = ref<FormInstance>()
const submitting = ref(false)
const modelOptions = ref<WorkflowModelOptionVO[]>([])

const form = reactive<WorkflowApproverRuleForm>({
  modelId: '',
  taskDefinitionKey: '',
  variableName: '',
  strategyType: '',
  strategyConfig: '',
  resolveScope: 'biz_tenant',
  sortNo: 1,
  status: STATUS_NORMAL,
  remark: '',
})

const visibleModel = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const dialogTitle = computed(() => (props.ruleId ? props.gp('editTitle') : props.gp('addTitle')))

const strategyOptions = computed(() => getDict('wf_approver_strategy'))
const resolveScopeOptions = computed(() => getDict('wf_approver_resolve_scope'))
const statusOptions = computed(() => getDict('sys_common_status'))

const strategyConfigHint = computed(() => {
  switch (form.strategyType) {
    case 'fixed_user':
      return props.gp('strategyConfigHintFixedUser')
    case 'starter_dept_role':
      return props.gp('strategyConfigHintStarterDeptRole')
    case 'expression':
      return props.gp('strategyConfigHintExpression')
    case 'role':
    default:
      return props.gp('strategyConfigHintRole')
  }
})

const rules: FormRules = {
  modelId: [{ required: true, message: props.gv('modelId', 'required'), trigger: 'change' }],
  taskDefinitionKey: [
    { required: true, message: props.gv('taskDefinitionKey', 'required'), trigger: 'blur' },
  ],
  variableName: [
    { required: true, message: props.gv('variableName', 'required'), trigger: 'blur' },
  ],
  strategyType: [
    { required: true, message: props.gv('strategyType', 'required'), trigger: 'change' },
  ],
  strategyConfig: [
    { required: true, message: props.gv('strategyConfig', 'required'), trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        try {
          JSON.parse(String(value))
          callback()
        } catch {
          callback(new Error(props.gp('strategyConfigInvalid')))
        }
      },
      trigger: 'blur',
    },
  ],
  resolveScope: [
    { required: true, message: props.gv('resolveScope', 'required'), trigger: 'change' },
  ],
  sortNo: [{ required: true, message: props.gv('sortNo', 'required'), trigger: 'change' }],
}

watch(
  () => props.modelValue,
  async (visible) => {
    if (!visible) return
    resetForm()
    await loadModelOptions()
    if (props.ruleId) {
      await loadDetail(props.ruleId)
    } else {
      if (props.presetModelId) {
        form.modelId = props.presetModelId
      }
      if (props.presetTaskKey) {
        form.taskDefinitionKey = props.presetTaskKey
        form.variableName = `${props.presetTaskKey}UserId`
      }
    }
  },
)

async function loadModelOptions() {
  const res = await listWorkflowApproverRuleModelOptions()
  modelOptions.value = res.data.data ?? []
}

function resetForm() {
  form.id = undefined
  form.modelId = ''
  form.taskDefinitionKey = ''
  form.variableName = ''
  form.strategyType = 'role'
  form.strategyConfig = '{"roleCodes":["dept_approver"]}'
  form.resolveScope = 'biz_tenant'
  form.sortNo = 1
  form.status = STATUS_NORMAL
  form.remark = ''
  formRef.value?.clearValidate()
}

async function loadDetail(id: string) {
  const res = await getWorkflowApproverRule(id)
  const data = res.data.data
  form.id = data.id
  form.modelId = data.modelId ?? ''
  form.taskDefinitionKey = data.taskDefinitionKey ?? ''
  form.variableName = data.variableName ?? ''
  form.strategyType = data.strategyType ?? ''
  form.strategyConfig = data.strategyConfig ?? ''
  form.resolveScope = data.resolveScope ?? 'biz_tenant'
  form.sortNo = data.sortNo ?? 1
  form.status = data.status ?? STATUS_NORMAL
  form.remark = data.remark ?? ''
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload: WorkflowApproverRuleForm = {
      ...form,
      strategyConfig: form.strategyConfig.trim(),
    }
    if (form.id) {
      const updatePayload: WorkflowApproverRuleUpdateForm = {
        id: form.id,
        variableName: payload.variableName,
        strategyType: payload.strategyType,
        strategyConfig: payload.strategyConfig,
        resolveScope: payload.resolveScope,
        sortNo: payload.sortNo,
        status: payload.status,
        remark: payload.remark,
      }
      await updateWorkflowApproverRule(updatePayload)
    } else {
      await createWorkflowApproverRule(payload)
    }
    ElMessage.success(t('common.success'))
    visibleModel.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>
