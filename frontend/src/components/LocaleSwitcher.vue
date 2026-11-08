<template>
  <el-tooltip v-if="compact" :content="t('layout.language')" placement="bottom">
    <el-dropdown
      trigger="click"
      placement="bottom-end"
      popper-class="navbar-tool-popper"
      @command="handleChange"
    >
      <button type="button" class="shell-header-icon-btn" :aria-label="t('layout.language')">
        <el-icon><Operation /></el-icon>
      </button>
      <template #dropdown>
        <el-dropdown-menu class="navbar-tool-menu">
          <el-dropdown-item
            v-for="item in localeOptions"
            :key="item.value"
            :command="item.value"
            :class="{ active: item.value === locale }"
          >
            {{ item.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </el-tooltip>

  <el-dropdown v-else trigger="click" @command="handleChange">
    <span class="locale-switch">
      <el-icon><Operation /></el-icon>
      <span>{{ currentLabel }}</span>
      <el-icon class="arrow"><ArrowDown /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="item in localeOptions"
          :key="item.value"
          :command="item.value"
          :class="{ active: item.value === locale }"
        >
          {{ item.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowDown, Operation } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useLocaleStore } from '@/stores/locale'
import { reloadAppRoutes } from '@/router'
import { getToken } from '@/utils/request'
import type { LocaleCode } from '@/i18n'

withDefaults(defineProps<{ compact?: boolean }>(), { compact: false })

const { t } = useI18n()
const localeStore = useLocaleStore()
const { locale, localeOptions } = storeToRefs(localeStore)

const currentLabel = computed(
  () => localeOptions.value.find((item) => item.value === locale.value)?.label ?? locale.value,
)

async function handleChange(code: LocaleCode) {
  if (code === locale.value) {
    return
  }
  await localeStore.setLocale(code)
  if (getToken()) {
    await reloadAppRoutes()
  }
}
</script>

<style scoped>
.locale-switch {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #595959;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.locale-switch:hover {
  background: #f5f5f5;
}

.arrow {
  font-size: 12px;
}

:deep(.el-dropdown-menu__item.active) {
  color: var(--ic-primary);
  font-weight: 600;
}
</style>
