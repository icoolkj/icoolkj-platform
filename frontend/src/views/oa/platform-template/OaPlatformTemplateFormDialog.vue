<template>
  <GenFormShell
    v-model="visibleModel"
    :title="dialogTitle"
    :width="640"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="108px">
      <el-form-item :label="gf('categoryCode', 'label')" prop="categoryCode">
        <el-select
          v-model="form.categoryCode"
          :placeholder="gf('categoryCode', 'placeholder')"
          :disabled="!!templateId"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="gf('templateCode', 'label')" prop="templateCode">
        <el-input
          v-model="form.templateCode"
          :placeholder="gf('templateCode', 'placeholder')"
          :disabled="!!templateId"
          maxlength="64"
        />
      </el-form-item>
      <el-form-item :label="gf('templateName', 'label')" prop="templateName">
        <el-input
          v-model="form.templateName"
          :placeholder="gf('templateName', 'placeholder')"
          maxlength="100"
        />
      </el-form-item>
      <el-form-item :label="gf('wfModelId', 'label')" prop="wfModelId">
        <el-select
          v-model="form.wfModelId"
          :placeholder="gf('wfModelId', 'placeholder')"
          clearable
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
import type { GenFieldValidation } from '@/types/tool/genI18n'
import type { OaPlatformTemplateForm } from '@/types/oa/oaPlatformTemplate'
import {
  createOaPlatformTemplate,
  getOaPlatformTemplate,
  listOaPlatformTemplateModelOptions,
  updateOaPlatformTemplate,
} from '@/api/oa/oaPlatformTemplate'

const props = defineProps<{
  modelValue: boolean
  templateId?: string
  gp: (key: string, params?: Record<string, string | number>) => string
  gf: (field: string, prop?: 'label' | 'placeholder') => string
  gv: (field: string, rule: keyof GenFieldValidation) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  success: []
}>()

const { t } = useI18n()
const { getDict } = useDict(['wf_biz_type', 'sys_common_status'])

const formRef = ref<FormInstance>()
const submitting = ref(false)
const modelOptions = ref<{ value: string; label: string }[]>([])

const form = reactive<OaPlatformTemplateForm>({
  categoryCode: '',
  templateCode: '',
  templateName: '',
  wfModelId: '',
  status: STATUS_NORMAL,
  remark: '',
})

const visibleModel = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const dialogTitle = computed(() =>
  props.templateId ? props.gp('editTitle') : props.gp('addTitle'),
)
const categoryOptions = computed(() => getDict('wf_biz_type'))
const statusOptions = computed(() => getDict('sys_common_status'))

const rules: FormRules = {
  categoryCode: [
    { required: true, message: props.gv('categoryCode', 'required'), trigger: 'change' },
  ],
  templateCode: [
    { required: true, message: props.gv('templateCode', 'required'), trigger: 'blur' },
  ],
  templateName: [
    { required: true, message: props.gv('templateName', 'required'), trigger: 'blur' },
  ],
}

watch(
  () => props.modelValue,
  async (visible) => {
    if (!visible) return
    resetForm()
    await loadModelOptions()
    if (props.templateId) {
      await loadDetail(props.templateId)
    }
  },
)

async function loadModelOptions() {
  const res = await listOaPlatformTemplateModelOptions()
  modelOptions.value = res.data.data ?? []
}

function resetForm() {
  form.categoryCode = ''
  form.templateCode = ''
  form.templateName = ''
  form.wfModelId = ''
  form.status = STATUS_NORMAL
  form.remark = ''
  formRef.value?.clearValidate()
}

async function loadDetail(id: string) {
  const res = await getOaPlatformTemplate(id)
  const data = res.data.data
  form.categoryCode = data.categoryCode ?? ''
  form.templateCode = data.templateCode ?? ''
  form.templateName = data.templateName ?? ''
  form.wfModelId = data.wfModelId ?? ''
  form.status = data.status ?? STATUS_NORMAL
  form.remark = data.remark ?? ''
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (props.templateId) {
      await updateOaPlatformTemplate({
        id: props.templateId,
        categoryCode: form.categoryCode,
        templateCode: form.templateCode,
        templateName: form.templateName,
        wfModelId: form.wfModelId || undefined,
        status: form.status,
        remark: form.remark,
      })
    } else {
      await createOaPlatformTemplate({
        categoryCode: form.categoryCode,
        templateCode: form.templateCode,
        templateName: form.templateName,
        wfModelId: form.wfModelId || undefined,
        status: form.status,
        remark: form.remark,
      })
    }
    ElMessage.success(t('common.success'))
    visibleModel.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>
