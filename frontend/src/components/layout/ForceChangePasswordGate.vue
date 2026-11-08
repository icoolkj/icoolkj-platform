<template>
  <el-dialog
    v-if="formMode === 'dialog'"
    :model-value="visible"
    :title="t('login.forceChangeTitle')"
    width="480px"
    :show-close="false"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :destroy-on-close="false"
    append-to-body
    align-center
    class="force-pwd-gate"
  >
    <ForceChangePasswordForm ref="formRef" @submit="submit" />
    <template #footer>
      <el-button type="primary" :loading="saving" @click="submit">
        {{ t('login.forceChangeSubmit') }}
      </el-button>
      <el-button :disabled="saving" @click="logout">
        {{ t('login.forceChangeLogout') }}
      </el-button>
    </template>
  </el-dialog>

  <el-drawer
    v-else
    :model-value="visible"
    :title="t('login.forceChangeTitle')"
    size="480px"
    :show-close="false"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :destroy-on-close="false"
    append-to-body
    class="force-pwd-gate"
  >
    <ForceChangePasswordForm ref="formRef" @submit="submit" />
    <template #footer>
      <el-button type="primary" :loading="saving" @click="submit">
        {{ t('login.forceChangeSubmit') }}
      </el-button>
      <el-button :disabled="saving" @click="logout">
        {{ t('login.forceChangeLogout') }}
      </el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useLayoutStore } from '@/stores/layout'
import { useUserStore } from '@/stores/user'
import ForceChangePasswordForm from './ForceChangePasswordForm.vue'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const layoutStore = useLayoutStore()
const { formMode } = storeToRefs(layoutStore)

const formRef = ref<InstanceType<typeof ForceChangePasswordForm> | null>(null)
const saving = ref(false)

const visible = computed(() => !!userStore.user?.mustChangePassword)

async function submit() {
  if (!formRef.value || saving.value) {
    return
  }
  saving.value = true
  try {
    const ok = await formRef.value.submit()
    if (ok) {
      await userStore.logout()
      await router.replace({ path: '/login', query: {} })
    }
  } finally {
    saving.value = false
  }
}

async function logout() {
  await userStore.logout()
  await router.replace({ path: '/login', query: {} })
}
</script>
