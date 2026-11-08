<template>
  <div class="policy-panel">
    <div class="policy-toolbar">
      <el-button
        type="primary"
        v-if="!demoStore.isDemo"
        v-hasPermi="[PERM.edit]"
        :loading="saveLoading"
        @click="handleSaveConfig"
      >
        {{ gp('saveConfig') }}
      </el-button>
    </div>
    <el-form
      ref="configFormRef"
      v-loading="configLoading"
      :model="configForm"
      :rules="configRules"
      label-width="120px"
      class="config-form"
    >
      <el-form-item :label="gf('enabled', 'label')">
        <el-switch v-model="configForm.enabled" :disabled="!canEdit" />
      </el-form-item>
      <el-form-item :label="gf('cron', 'label')" prop="cron">
        <el-input v-model="configForm.cron" :disabled="!canEdit" placeholder="0 0 2 * * ?" />
      </el-form-item>
      <el-form-item :label="gf('outputDir', 'label')" prop="outputDir">
        <el-input v-model="configForm.outputDir" :disabled="!canEdit" />
      </el-form-item>
      <el-form-item :label="gf('retentionDays', 'label')" prop="retentionDays">
        <el-input-number
          v-model="configForm.retentionDays"
          :min="0"
          :max="3650"
          controls-position="right"
          :disabled="!canEdit"
        />
      </el-form-item>
      <el-form-item :label="gf('compress', 'label')">
        <el-switch v-model="configForm.compress" :disabled="!canEdit" />
      </el-form-item>
      <el-form-item :label="gf('mysqldumpCommand', 'label')">
        <el-input :model-value="configForm.mysqldumpCommand" disabled />
        <div class="form-tip">{{ gp('mysqldumpYamlHint') }}</div>
      </el-form-item>
    </el-form>
    <el-alert class="hint" :title="gp('hint')" type="info" :closable="false" show-icon />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  PERM_DatabaseBackup,
  getDatabaseBackupConfig,
  updateDatabaseBackupConfig,
} from '@/api/infra/databaseBackup'
import type { DatabaseBackupConfigUpdateRequest } from '@/types/infra/databaseBackup'
import { useUserStore } from '@/stores/user'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useDemoStore } from '@/stores/demo'
import { hasPermission } from '@/utils/permission'

const PERM = PERM_DatabaseBackup
const { t } = useI18n()
const userStore = useUserStore()
const demoStore = useDemoStore()
const { gp, gf } = useGenPageShellI18n('databaseBackup')

const configFormRef = ref<FormInstance>()
const configLoading = ref(false)
const saveLoading = ref(false)

const configForm = reactive({
  enabled: false,
  cron: '0 0 2 * * ?',
  outputDir: './data/db-backup',
  retentionDays: 14,
  compress: true,
  mysqldumpCommand: 'mysqldump',
})

const configRules: FormRules = {
  cron: [
    {
      required: true,
      message: () => gf('cron', 'placeholder') || t('common.required'),
      trigger: 'blur',
    },
  ],
  outputDir: [{ required: true, message: () => t('common.required'), trigger: 'blur' }],
  retentionDays: [
    { required: true, type: 'number', message: () => t('common.required'), trigger: 'change' },
  ],
}

const canEdit = computed(() => hasPermission(userStore.permissions, PERM.edit) && !demoStore.isDemo)

async function loadConfig() {
  configLoading.value = true
  try {
    const res = await getDatabaseBackupConfig()
    const data = res.data.data
    if (!data) return
    configForm.enabled = data.enabled ?? false
    configForm.cron = data.cron ?? '0 0 2 * * ?'
    configForm.outputDir = data.outputDir ?? './data/db-backup'
    configForm.retentionDays = data.retentionDays ?? 14
    configForm.compress = data.compress ?? true
    configForm.mysqldumpCommand = data.mysqldumpCommand ?? 'mysqldump'
  } finally {
    configLoading.value = false
  }
}

async function handleSaveConfig() {
  if (!canEdit.value) return
  await configFormRef.value?.validate()
  saveLoading.value = true
  try {
    const payload: DatabaseBackupConfigUpdateRequest = {
      enabled: configForm.enabled,
      cron: configForm.cron.trim(),
      outputDir: configForm.outputDir.trim(),
      retentionDays: configForm.retentionDays,
      compress: configForm.compress,
    }
    await updateDatabaseBackupConfig(payload)
    ElMessage.success(gp('configSaved'))
    await loadConfig()
  } finally {
    saveLoading.value = false
  }
}

defineExpose({ loadConfig })
</script>

<style scoped>
.policy-panel {
  padding-top: 4px;
}
.policy-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}
.config-form {
  max-width: 720px;
}
.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.hint {
  margin-top: 16px;
  max-width: 720px;
}
</style>
