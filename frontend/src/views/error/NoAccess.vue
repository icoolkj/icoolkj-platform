<template>
  <div class="no-access-page">
    <el-result
      icon="warning"
      :title="t('layout.noAccessTitle')"
      :sub-title="t('layout.noAccessSubtitle')"
    >
      <template #extra>
        <el-button type="primary" @click="goLogin">{{ t('layout.noAccessRelogin') }}</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { resetRouterState } from '@/router'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

async function goLogin() {
  await userStore.logout()
  resetRouterState()
  router.replace('/login')
}
</script>

<style scoped>
.no-access-page {
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
</style>
