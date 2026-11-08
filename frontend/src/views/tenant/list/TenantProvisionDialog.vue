<template>
  <el-dialog
    :model-value="modelValue"
    :title="gp('provisionTitle')"
    width="560px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-alert :title="gp('provisionWarn')" type="warning" :closable="false" show-icon class="mb-3" />
    <p class="provision-tip">{{ gp('provisionLoginTip') }}</p>

    <div v-if="info" class="provision-rows">
      <div class="provision-row">
        <span class="label">{{ gp('provisionTenantName') }}</span>
        <span class="value">{{ info.tenantName }}</span>
        <el-button link type="primary" @click="copyOne(info.tenantName)">{{ gp('provisionCopy') }}</el-button>
      </div>
      <div class="provision-row highlight">
        <span class="label">{{ gp('provisionTenantCode') }}</span>
        <span class="value mono">{{ info.tenantCode }}</span>
        <el-button link type="primary" @click="copyOne(info.tenantCode)">{{ gp('provisionCopy') }}</el-button>
      </div>
      <div class="provision-row highlight">
        <span class="label">{{ gp('provisionAdminUsername') }}</span>
        <span class="value mono">{{ info.adminUsername }}</span>
        <el-button link type="primary" @click="copyOne(info.adminUsername)">{{ gp('provisionCopy') }}</el-button>
      </div>
      <div class="provision-row highlight">
        <span class="label">{{ gp('provisionInitialPassword') }}</span>
        <span class="value mono">{{ showPassword ? info.initialPassword : '••••••••••' }}</span>
        <div class="row-actions">
          <el-button link type="primary" @click="showPassword = !showPassword">
            {{ showPassword ? gp('provisionHidePassword') : gp('provisionShowPassword') }}
          </el-button>
          <el-button link type="primary" @click="copyOne(info.initialPassword)">{{ gp('provisionCopy') }}</el-button>
        </div>
      </div>
      <div class="provision-row">
        <span class="label">{{ gp('provisionRootDept') }}</span>
        <span class="value">{{ info.rootDeptName }}</span>
      </div>
      <div v-if="info.rootDeptCode" class="provision-row">
        <span class="label">{{ gp('provisionRootDeptCode') }}</span>
        <span class="value mono">{{ info.rootDeptCode }}</span>
        <el-button link type="primary" @click="copyOne(info.rootDeptCode)">{{ gp('provisionCopy') }}</el-button>
      </div>
    </div>

    <template #footer>
      <el-button @click="copyAll">{{ gp('provisionCopyAll') }}</el-button>
      <el-button type="primary" @click="emit('update:modelValue', false)">{{ gp('provisionSaved') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { TenantProvisionVO } from '@/types/tenant/tenant'

const props = defineProps<{
  modelValue: boolean
  info: TenantProvisionVO | null
  gp: (key: string, params?: Record<string, string | number>) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const { t } = useI18n()
const showPassword = ref(true)

watch(
  () => props.modelValue,
  (open) => {
    if (open) showPassword.value = true
  },
)

async function copyOne(text?: string) {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('common.copied'))
  } catch {
    ElMessage.error(t('common.fail'))
  }
}

async function copyAll() {
  if (!props.info) return
  const text = props.gp('provisionCopyAllText', {
    tenantCode: props.info.tenantCode ?? '',
    adminUsername: props.info.adminUsername ?? '',
    initialPassword: props.info.initialPassword ?? '',
  })
  await copyOne(text)
}
</script>

<style scoped>
.mb-3 {
  margin-bottom: 12px;
}
.provision-tip {
  margin: 0 0 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.5;
}
.provision-rows {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.provision-row {
  display: grid;
  grid-template-columns: 100px 1fr auto;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
}
.provision-row.highlight {
  background: var(--el-color-primary-light-9);
}
.provision-row .label {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.provision-row .value {
  word-break: break-all;
}
.provision-row .mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-weight: 600;
}
.row-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
