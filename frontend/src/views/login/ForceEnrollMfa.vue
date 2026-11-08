<!-- 强制绑定 MFA（会话 mustEnrollMfa） -->
<template>
  <div class="force-mfa-page">
    <el-card class="force-mfa-card" shadow="always">
      <div class="force-mfa-header">
        <h2>{{ t('login.forceEnrollTitle') }}</h2>
        <LocaleSwitcher />
      </div>
      <el-alert
        class="force-mfa-alert"
        type="warning"
        :closable="false"
        show-icon
        :title="t('login.forceEnrollHint')"
      />
      <p v-if="userStore.user" class="force-mfa-user">
        {{ userStore.user.tenantCode }} / {{ userStore.user.username }}
      </p>

      <div v-if="otpAuthUri" class="force-mfa-qr">
        <img :src="qrImageUrl" :alt="t('login.mfaQrAlt')" class="force-mfa-qr__img" />
        <p class="force-mfa-hint">{{ t('login.forceEnrollScan') }}</p>
        <el-input :model-value="otpAuthUri" readonly type="textarea" :rows="2" />
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent
      >
        <el-form-item :label="t('login.mfaCode')" prop="code">
          <el-input
            v-model="form.code"
            :placeholder="t('login.mfaCodePlaceholder')"
            maxlength="6"
            @keyup.enter="handleConfirm"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="force-mfa-btn" :loading="saving" @click="handleConfirm">
            {{ t('login.forceEnrollSubmit') }}
          </el-button>
          <el-button class="force-mfa-btn-secondary" :disabled="saving" @click="handleLogout">
            {{ t('login.forceChangeLogout') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import LocaleSwitcher from '@/components/LocaleSwitcher.vue'
import { confirmMfaEnroll, startMfaEnroll } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { findFirstRegisteredPath, setupDynamicRoutes } from '@/router/dynamicRoutes'
import { useMenuStore } from '@/stores/menu'
import { useProductStore } from '@/stores/product'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()
const productStore = useProductStore()

const formRef = ref<FormInstance>()
const saving = ref(false)
const otpAuthUri = ref('')
const form = reactive({ code: '' })

const rules = computed<FormRules>(() => ({
  code: [
    { required: true, message: t('login.mfaCodeRequired'), trigger: 'blur' },
    { pattern: /^\d{6}$/, message: t('login.mfaCodeRequired'), trigger: 'blur' },
  ],
}))

const qrImageUrl = computed(() => {
  if (!otpAuthUri.value) return ''
  return `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(otpAuthUri.value)}`
})

onMounted(async () => {
  try {
    const data = await startMfaEnroll()
    otpAuthUri.value = data.otpAuthUri
  } catch {
    /* interceptor shows message */
  }
})

async function handleConfirm() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await confirmMfaEnroll(form.code)
    await userStore.loadProfile()
    ElMessage.success(t('login.forceEnrollSuccess'))
    await setupDynamicRoutes(router, { force: true })
    await productStore.loadMine(true)
    await productStore.ensureSidebar()
    const home = productStore.current?.homePath?.trim()
    const landing =
      (home ? (home.startsWith('/') ? home : `/${home}`) : null) ||
      findFirstRegisteredPath(router, menuStore.allRouters) ||
      '/no-access'
    await router.replace(landing)
  } finally {
    saving.value = false
  }
}

async function handleLogout() {
  await userStore.logout()
  await router.replace('/login')
}
</script>

<style scoped>
.force-mfa-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(160deg, #f0f4f8 0%, #e8eef5 100%);
}
.force-mfa-card {
  width: 100%;
  max-width: 440px;
}
.force-mfa-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.force-mfa-header h2 {
  margin: 0;
  font-size: 20px;
}
.force-mfa-alert {
  margin-bottom: 12px;
}
.force-mfa-user {
  color: var(--el-text-color-secondary);
  margin: 0 0 16px;
  font-size: 13px;
}
.force-mfa-qr {
  text-align: center;
  margin-bottom: 16px;
}
.force-mfa-qr__img {
  width: 180px;
  height: 180px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
}
.force-mfa-hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin: 8px 0;
}
.force-mfa-btn,
.force-mfa-btn-secondary {
  width: 100%;
  margin-left: 0 !important;
  margin-bottom: 8px;
}
</style>
