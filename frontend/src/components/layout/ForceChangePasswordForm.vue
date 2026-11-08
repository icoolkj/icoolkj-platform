<template>
  <div class="force-pwd-form">
    <el-alert
      type="warning"
      :closable="false"
      show-icon
      :title="t('login.forceChangeHint')"
      :description="t('login.passwordPolicyHint')"
    />
    <p v-if="userStore.user" class="force-pwd-form__user">
      {{ userStore.user.tenantCode }} / {{ userStore.user.username }}
    </p>
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      @submit.prevent
    >
      <el-form-item :label="t('login.oldPassword')" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          show-password
          autocomplete="current-password"
          :placeholder="t('login.oldPasswordRequired')"
        />
      </el-form-item>
      <el-form-item :label="t('login.newPassword')" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          show-password
          autocomplete="new-password"
          :placeholder="t('login.newPasswordRequired')"
        />
      </el-form-item>
      <el-form-item :label="t('login.confirmPassword')" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          show-password
          autocomplete="new-password"
          :placeholder="t('login.confirmPasswordRequired')"
          @keyup.enter="emitSubmit"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { changePassword } from '@/api/profile'
import { useUserStore } from '@/stores/user'

const emit = defineEmits<{ submit: [] }>()

const { t } = useI18n()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const rules = computed<FormRules>(() => ({
  oldPassword: [{ required: true, message: t('login.oldPasswordRequired'), trigger: 'blur' }],
  newPassword: [
    { required: true, message: t('login.newPasswordRequired'), trigger: 'blur' },
    { min: 10, message: t('login.newPasswordMin'), trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        const pwd = String(value || '')
        if (!/[A-Z]/.test(pwd)) {
          callback(new Error(t('login.passwordRequireUpper')))
          return
        }
        if (!/[a-z]/.test(pwd)) {
          callback(new Error(t('login.passwordRequireLower')))
          return
        }
        if (!/\d/.test(pwd)) {
          callback(new Error(t('login.passwordRequireNumber')))
          return
        }
        if (!/[^A-Za-z0-9]/.test(pwd)) {
          callback(new Error(t('login.passwordRequireSpecial')))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: t('login.confirmPasswordRequired'), trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error(t('login.passwordMismatch')))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}))

function emitSubmit() {
  emit('submit')
}

/** @returns 是否改密成功 */
async function submit(): Promise<boolean> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return false
  }
  await changePassword({
    oldPassword: form.oldPassword,
    newPassword: form.newPassword,
  })
  ElMessage.success(t('login.forceChangeSuccess'))
  return true
}

defineExpose({ submit })
</script>

<style scoped>
.force-pwd-form :deep(.el-alert) {
  margin-bottom: 16px;
}
.force-pwd-form__user {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
