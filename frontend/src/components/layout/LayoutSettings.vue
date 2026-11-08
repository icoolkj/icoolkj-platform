<template>
  <el-drawer
    v-model="visible"
    :title="t('layout.settings.title')"
    direction="rtl"
    size="320px"
    :append-to-body="true"
    :close-on-click-modal="true"
    class="layout-settings-drawer"
  >
    <div class="settings-section">
      <div class="settings-label">{{ t('layout.settings.themeColor') }}</div>
      <div class="theme-options">
        <button
          v-for="item in themeOptions"
          :key="item.id"
          type="button"
          class="theme-option"
          :class="{ active: item.id === themeId }"
          @click="handleTheme(item.id)"
        >
          <span class="theme-option__bar" :style="{ background: item.primary }" />
          <span class="theme-option__label">{{ t(`layout.themes.${item.id}`) }}</span>
        </button>
      </div>
    </div>

    <div class="settings-section">
      <div class="settings-label">{{ t('layout.settings.headerStyle') }}</div>
      <el-radio-group
        :model-value="settings.headerStyle"
        @change="
          (value: string | number | boolean | undefined) =>
            handleLayout({ headerStyle: value as 'light' | 'primary' })
        "
      >
        <el-radio-button value="primary">{{ t('layout.settings.headerPrimary') }}</el-radio-button>
        <el-radio-button value="light">{{ t('layout.settings.headerLight') }}</el-radio-button>
      </el-radio-group>
    </div>

    <div class="settings-section">
      <div class="settings-label">{{ t('layout.settings.sidebarStyle') }}</div>
      <el-radio-group
        :model-value="settings.sidebarTheme"
        @change="
          (value: string | number | boolean | undefined) =>
            handleLayout({ sidebarTheme: value as 'dark' | 'light' })
        "
      >
        <el-radio-button value="dark">{{ t('layout.settings.sidebarDark') }}</el-radio-button>
        <el-radio-button value="light">{{ t('layout.settings.sidebarLight') }}</el-radio-button>
      </el-radio-group>
    </div>

    <div class="settings-section">
      <div class="settings-label">{{ t('layout.settings.interface') }}</div>
      <div class="settings-switch">
        <span>{{ t('layout.settings.breadcrumb') }}</span>
        <el-switch
          :model-value="settings.breadcrumb"
          @change="(v: boolean) => handleLayout({ breadcrumb: v })"
        />
      </div>
      <div class="settings-switch">
        <span>{{ t('layout.settings.tagsView') }}</span>
        <el-switch
          :model-value="settings.tagsView"
          @change="(v: boolean) => handleLayout({ tagsView: v })"
        />
      </div>
      <div class="settings-switch">
        <span>{{ t('layout.settings.pageHeader') }}</span>
        <el-switch
          :model-value="settings.pageHeader"
          @change="(v: boolean) => handleLayout({ pageHeader: v })"
        />
      </div>
      <div class="settings-switch">
        <span>{{ t('layout.settings.watermark') }}</span>
        <el-switch
          :model-value="settings.watermark"
          @change="(v: boolean) => handleLayout({ watermark: v })"
        />
      </div>
      <div class="settings-switch">
        <span>{{ t('layout.settings.menuUniqueOpened') }}</span>
        <el-switch
          :model-value="settings.menuUniqueOpened"
          @change="(v: boolean) => handleLayout({ menuUniqueOpened: v })"
        />
      </div>
    </div>

    <div class="settings-section">
      <div class="settings-label">{{ t('layout.settings.formMode') }}</div>
      <el-select
        :model-value="settings.formMode"
        style="width: 100%"
        @change="(v: 'dialog' | 'drawer') => handleLayout({ formMode: v })"
      >
        <el-option :label="t('layout.settings.formDialog')" value="dialog" />
        <el-option :label="t('layout.settings.formDrawer')" value="drawer" />
      </el-select>
    </div>

    <div class="settings-footer">
      <el-button :loading="resetting" @click="handleReset">
        {{ t('layout.settings.reset') }}
      </el-button>
    </div>

    <el-alert
      :title="t('layout.settings.hint')"
      type="info"
      :closable="false"
      show-icon
      class="settings-hint"
    />
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useLayoutStore } from '@/stores/layout'
import { useThemeStore } from '@/stores/theme'
import type { LayoutSettings } from '@/types/layout'

const { t } = useI18n()
const layoutStore = useLayoutStore()
const themeStore = useThemeStore()
const { settings, settingsVisible } = storeToRefs(layoutStore)
const { themeId, themeOptions } = storeToRefs(themeStore)
const resetting = ref(false)

const visible = computed({
  get: () => settingsVisible.value,
  set: (value) => {
    if (value) {
      layoutStore.openSettings()
    } else {
      layoutStore.closeSettings()
    }
  },
})

async function handleTheme(id: string) {
  if (id === themeId.value) {
    return
  }
  await themeStore.setTheme(id)
  layoutStore.applyShellStyles()
}

async function handleLayout(partial: Partial<LayoutSettings>) {
  await layoutStore.updateSettings(partial)
}

async function handleReset() {
  if (resetting.value) {
    return
  }
  resetting.value = true
  try {
    await layoutStore.resetToDefaults()
  } finally {
    resetting.value = false
  }
}
</script>

<style scoped>
.settings-section {
  margin-bottom: 20px;
}

.settings-label {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #262626;
}

.theme-options {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.theme-option {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 6px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
}

.theme-option__bar {
  display: block;
  height: 28px;
  border-radius: 6px;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.theme-option__label {
  font-size: 11px;
  color: #8c8c8c;
  text-align: center;
  line-height: 1.2;
}

.theme-option.active .theme-option__bar {
  border-color: #262626;
  box-shadow: 0 0 0 2px #fff inset;
}

.theme-option.active .theme-option__label {
  color: #262626;
  font-weight: 600;
}

.settings-switch {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
  color: #595959;
}

.settings-footer {
  margin-top: 8px;
}

.settings-hint {
  margin-top: 16px;
}
</style>
