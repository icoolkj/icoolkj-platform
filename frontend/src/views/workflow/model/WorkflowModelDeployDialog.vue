<template>
  <GenFormShell
    v-model="visibleModel"
    :title="gp('deployTitle')"
    :width="860"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-form ref="formRef" :model="form" :rules="deployRules" label-width="108px">
      <el-form-item :label="gf('bpmnXml', 'label')" prop="bpmnXml">
        <el-input
          v-model="form.bpmnXml"
          type="textarea"
          :rows="18"
          :placeholder="gf('bpmnXml', 'placeholder')"
        />
      </el-form-item>
      <el-form-item :label="gf('deployRemark', 'label')" prop="remark">
        <el-input
          v-model="form.remark"
          maxlength="500"
          :placeholder="gf('deployRemark', 'placeholder')"
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
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { deployWorkflowModel, getWorkflowModel } from '@/api/workflow/model'

const props = defineProps<{
  modelId?: string
}>()

const emit = defineEmits<{ success: [] }>()

const visibleModel = defineModel<boolean>({ default: false })
const { t } = useI18n()

const { gp, gf, formRules } = useGenPageShellI18n('workflowModel')

const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  bpmnXml: '',
  remark: '',
})

const deployRules = computed<FormRules>(() => ({
  bpmnXml: formRules.value.bpmnXml ?? [],
  remark: formRules.value.remark ?? [],
}))

watch(visibleModel, async (open) => {
  if (!open || !props.modelId) return
  form.bpmnXml = ''
  form.remark = ''
  formRef.value?.clearValidate()
  const res = await getWorkflowModel(props.modelId)
  form.bpmnXml = res.data.data?.bpmnXml ?? ''
})

async function submit() {
  if (!props.modelId) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await deployWorkflowModel({
      modelId: props.modelId,
      bpmnXml: form.bpmnXml,
      remark: form.remark || undefined,
    })
    ElMessage.success(gp('deploySuccess'))
    visibleModel.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>
