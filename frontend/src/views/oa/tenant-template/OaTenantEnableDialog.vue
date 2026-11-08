<template>
  <GenFormShell
    v-model="visible"
    :title="gp('enableTitle')"
    :width="520"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-form ref="formRef" :model="form" :rules="enableRules" label-width="120px">
      <el-form-item :label="gf('platformTemplateId', 'label')" prop="platformTemplateId">
        <el-select
          v-model="form.platformTemplateId"
          :placeholder="gf('platformTemplateId', 'placeholder')"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="opt in options"
            :key="opt.id"
            :label="`${opt.templateName} (${opt.categoryCode})`"
            :value="opt.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="gf('templateName', 'label')" prop="templateName">
        <el-input v-model="form.templateName" :placeholder="gf('templateName', 'placeholder')" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="loading" @click="submit">
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
import { enableOaTenantTemplate, listOaPlatformTemplateOptions } from '@/api/oa/oaTenantTemplate'
import type { OaPlatformTemplateOptionVO } from '@/types/oa/oaTenantTemplate'

const emit = defineEmits<{
  success: []
}>()

const visible = defineModel<boolean>({ default: false })

const { gp, gf, gv } = useGenPageShellI18n('oaTenantTemplate')
const { t } = useI18n()

const loading = ref(false)
const formRef = ref<FormInstance>()
const options = ref<OaPlatformTemplateOptionVO[]>([])
const form = reactive({ platformTemplateId: '', templateName: '' })

const enableRules = computed<FormRules>(() => ({
  platformTemplateId: [
    {
      required: true,
      message: gv('platformTemplateId', 'required') || gf('platformTemplateId'),
      trigger: 'change',
    },
  ],
}))

watch(
  () => visible.value,
  async (open) => {
    if (!open) return
    const res = await listOaPlatformTemplateOptions()
    options.value = res.data.data
    form.platformTemplateId = ''
    form.templateName = ''
    formRef.value?.clearValidate()
  },
)

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await enableOaTenantTemplate({
      platformTemplateId: form.platformTemplateId,
      templateName: form.templateName || undefined,
    })
    ElMessage.success(gp('enabled'))
    visible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}
</script>
