<template>
  <div v-if="i18nLoadError" class="i18n-boot-error">
    <p class="i18n-boot-error__title">{{ boot.errorTitle }}</p>
    <p class="i18n-boot-error__detail">{{ i18nLoadError }}</p>
    <p class="i18n-boot-error__hint">{{ boot.errorHint }}</p>
    <el-button type="primary" :loading="retrying" @click="retryLoad">{{ boot.retry }}</el-button>
  </div>
  <div
    v-else-if="!i18nReady"
    class="i18n-boot-loading"
    v-loading="true"
    :element-loading-text="boot.loading"
  />
  <el-config-provider v-else :locale="elementLocale" :size="UI_BUTTON_SIZE">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { i18nLoadError, i18nReady, loadRemoteI18n, LOCALE_STORAGE_KEY } from '@/i18n'
import { useLocaleStore } from '@/stores/locale'
import { resolveShellBootstrap } from '../shell.config'
import { UI_BUTTON_SIZE } from '@/constants/ui'

const localeStore = useLocaleStore()
const { elementLocale } = storeToRefs(localeStore)
const retrying = ref(false)

const boot = computed(() => {
  try {
    return resolveShellBootstrap(localStorage.getItem(LOCALE_STORAGE_KEY))
  } catch {
    return resolveShellBootstrap(null)
  }
})

async function retryLoad() {
  retrying.value = true
  try {
    await loadRemoteI18n()
  } finally {
    retrying.value = false
  }
}
</script>

<style scoped>
.i18n-boot-loading {
  min-height: 100vh;
}

.i18n-boot-error {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px;
  text-align: center;
  color: #606266;
}

.i18n-boot-error__title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.i18n-boot-error__detail {
  margin: 0;
  color: #909399;
  max-width: 520px;
  word-break: break-word;
}

.i18n-boot-error__hint {
  margin: 0 0 8px;
  font-size: 13px;
  color: #909399;
}
</style>
