<template>
  <GenFormShell
    v-model="visibleModel"
    :title="dialogTitle"
    :width="920"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="108px">
      <el-form-item :label="gf('title')" prop="title">
        <el-input v-model="form.title" :placeholder="gf('title', 'placeholder')" maxlength="200" />
      </el-form-item>
      <el-form-item :label="gf('status')" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="gf('remark')" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          maxlength="500"
          :placeholder="gf('remark', 'placeholder')"
        />
      </el-form-item>

      <div class="attachment-demo-section-title">{{ gp('sectionAttachments') }}</div>
      <CatalogAttachmentManager
        ref="catalogRef"
        v-model="form.assets"
        :catalog-code="catalogCode"
        :biz-id="recordId"
        :download-scope="FILE_DOWNLOAD_SCOPE_INFRA"
        :gp="gp"
      />
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
import { CatalogAttachmentManager, FILE_DOWNLOAD_SCOPE_INFRA } from '@/components/attachment'
import { STATUS_NORMAL } from '@/constants/flags'
import { useDict } from '@/composables/useDict'
import type { GenFieldValidation } from '@/types/tool/genI18n'
import {
  createAttachmentDemo,
  getAttachmentDemo,
  updateAttachmentDemo,
} from '@/api/infra/attachmentDemo'
import type { AttachmentDemoForm } from '@/types/infra/attachmentDemo'

const props = defineProps<{
  modelValue: boolean
  recordId?: string
  catalogCode: string
  gp: (key: string, params?: Record<string, string | number>) => string
  gf: (field: string, prop?: 'label' | 'placeholder') => string
  gv: (field: string, rule: keyof GenFieldValidation) => string
  formRules: FormRules
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  success: []
}>()

const { t } = useI18n()
const { getDict } = useDict(['sys_common_status'])
const statusOptions = computed(() => getDict('sys_common_status'))
const formRef = ref<FormInstance>()
const catalogRef = ref<InstanceType<typeof CatalogAttachmentManager>>()
const submitting = ref(false)

const visibleModel = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const dialogTitle = computed(() =>
  props.recordId ? props.gp('editTitle') : props.gp('addTitle'),
)

const form = reactive<AttachmentDemoForm>({
  title: '',
  assets: {},
  status: STATUS_NORMAL,
  remark: '',
})

const rules = computed<FormRules>(() => props.formRules)

function resetForm() {
  form.title = ''
  form.assets = {}
  form.status = STATUS_NORMAL
  form.remark = ''
  form.id = undefined
}

async function loadDetail(id: string) {
  const res = await getAttachmentDemo(id)
  const data = res.data.data
  form.id = data.id
  form.title = data.title
  form.assets = { ...(data.assets || {}) }
  form.status = data.status
  form.remark = data.remark || ''
}

watch(
  () => props.modelValue,
  async (open) => {
    if (!open) return
    resetForm()
    if (props.recordId) {
      await loadDetail(props.recordId)
    }
  },
)

async function submit() {
  await formRef.value?.validate()
  if (!catalogRef.value?.validate()) {
    return
  }
  submitting.value = true
  try {
    const payload: AttachmentDemoForm = {
      id: form.id,
      title: form.title.trim(),
      assets: form.assets || {},
      status: form.status,
      remark: form.remark?.trim() || undefined,
    }
    if (props.recordId) {
      await updateAttachmentDemo(payload)
    } else {
      await createAttachmentDemo(payload)
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
.attachment-demo-section-title {
  margin: 4px 0 10px;
  padding-left: 8px;
  font-weight: 600;
  font-size: 14px;
  color: var(--el-text-color-primary);
  border-left: 3px solid var(--el-color-primary);
}
</style>
