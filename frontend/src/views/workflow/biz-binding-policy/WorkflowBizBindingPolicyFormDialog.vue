<template>
  <GenFormShell
    v-model="visibleModel"
    :title="dialogTitle"
    :width="640"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="168px">
      <el-form-item :label="gf('bizType', 'label')" prop="bizType">
        <el-input
          v-model="form.bizType"
          :placeholder="gf('bizType', 'placeholder')"
          :disabled="!!policyId"
          maxlength="64"
        />
      </el-form-item>
      <el-form-item :label="gf('bindingMode', 'label')" prop="bindingMode">
        <el-select
          v-model="form.bindingMode"
          :placeholder="gf('bindingMode', 'placeholder')"
          style="width: 100%"
        >
          <el-option
            v-for="item in bindingModeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="form.bindingMode === 'platform_mandatory'"
        :label="gf('allowTenantApproverOverride', 'label')"
        prop="allowTenantApproverOverride"
      >
        <el-radio-group v-model="form.allowTenantApproverOverride">
          <el-radio v-for="item in boolOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
        <div class="field-hint">{{ gp('allowOverrideHint') }}</div>
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
import type {
  WorkflowBizBindingPolicyForm,
  WorkflowBizBindingPolicyUpdateForm,
} from '@/types/workflow/bizBindingPolicy'
import type { GenFieldValidation } from '@/types/tool/genI18n'
import {
  createWorkflowBizBindingPolicy,
  getWorkflowBizBindingPolicy,
  updateWorkflowBizBindingPolicy,
} from '@/api/workflow/bizBindingPolicy'
import { YES } from '@/constants/flags'

const props = defineProps<{
  modelValue: boolean
  policyId?: string
  gp: (key: string, params?: Record<string, string | number>) => string
  gf: (field: string, prop?: 'label' | 'placeholder') => string
  gv: (field: string, rule: keyof GenFieldValidation) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  success: []
}>()

const { t } = useI18n()
const { getDict } = useDict(['wf_binding_mode', 'sys_bool'])

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<WorkflowBizBindingPolicyForm>({
  bizType: '',
  bindingMode: 'tenant_preferred',
  allowTenantApproverOverride: YES,
  remark: '',
})

const visibleModel = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const dialogTitle = computed(() => (props.policyId ? props.gp('editTitle') : props.gp('addTitle')))
const bindingModeOptions = computed(() => getDict('wf_binding_mode'))
const boolOptions = computed(() => getDict('sys_bool'))

const rules: FormRules = {
  bizType: [{ required: true, message: props.gv('bizType', 'required'), trigger: 'blur' }],
  bindingMode: [
    { required: true, message: props.gv('bindingMode', 'required'), trigger: 'change' },
  ],
  allowTenantApproverOverride: [
    {
      validator: (_rule, _value, callback) => {
        if (form.bindingMode === 'platform_mandatory' && !form.allowTenantApproverOverride) {
          callback(new Error(props.gv('allowTenantApproverOverride', 'required')))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

watch(
  () => props.modelValue,
  async (visible) => {
    if (!visible) return
    resetForm()
    if (props.policyId) {
      await loadDetail(props.policyId)
    }
  },
)

watch(
  () => form.bindingMode,
  (mode) => {
    if (mode !== 'platform_mandatory') {
      form.allowTenantApproverOverride = YES
    }
  },
)

function resetForm() {
  form.id = undefined
  form.bizType = ''
  form.bindingMode = 'tenant_preferred'
  form.allowTenantApproverOverride = YES
  form.remark = ''
  formRef.value?.clearValidate()
}

async function loadDetail(id: string) {
  const res = await getWorkflowBizBindingPolicy(id)
  const data = res.data.data
  form.id = data.id
  form.bizType = data.bizType ?? ''
  form.bindingMode = data.bindingMode ?? 'tenant_preferred'
  form.allowTenantApproverOverride = data.allowTenantApproverOverride ?? YES
  form.remark = data.remark ?? ''
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (form.id) {
      const payload: WorkflowBizBindingPolicyUpdateForm = {
        id: form.id,
        bindingMode: form.bindingMode,
        allowTenantApproverOverride: form.allowTenantApproverOverride,
        remark: form.remark,
      }
      await updateWorkflowBizBindingPolicy(payload)
    } else {
      await createWorkflowBizBindingPolicy({ ...form })
    }
    ElMessage.success(t('common.success'))
    visibleModel.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.field-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
</style>
