<template>
  <div class="layout-navbar">
    <NavbarMessages />

    <LocaleSwitcher compact />

    <el-tooltip
      :content="isFullscreen ? t('layout.fullscreenExit') : t('layout.fullscreen')"
      placement="bottom"
    >
      <button
        type="button"
        class="shell-header-icon-btn"
        :aria-label="isFullscreen ? t('layout.fullscreenExit') : t('layout.fullscreen')"
        @click="toggleFullscreen"
      >
        <el-icon>
          <ScaleToOriginal v-if="isFullscreen" />
          <FullScreen v-else />
        </el-icon>
      </button>
    </el-tooltip>

    <el-tooltip :content="t('layout.settings.title')" placement="bottom">
      <button
        type="button"
        class="shell-header-icon-btn"
        :aria-label="t('layout.settings.title')"
        @click="layoutStore.openSettings()"
      >
        <el-icon><Setting /></el-icon>
      </button>
    </el-tooltip>

    <span class="navbar-divider" />

    <el-dropdown trigger="click" placement="bottom-end" popper-class="navbar-user-popper">
      <button type="button" class="user-trigger">
        <el-avatar :size="32" :src="navbarAvatarUrl || undefined" class="user-avatar">
          {{ avatarText }}
        </el-avatar>
        <span class="user-name">
          {{ userStore.user?.nickname || userStore.user?.username || '-' }}
        </span>
        <el-icon class="user-arrow"><ArrowDown /></el-icon>
      </button>
      <template #dropdown>
        <div class="user-panel">
          <div class="user-panel-header">
            <el-avatar :size="44" :src="navbarAvatarUrl || undefined" class="user-avatar-lg">
              {{ avatarText }}
            </el-avatar>
            <div class="user-panel-meta">
              <span class="user-panel-name">
                {{ userStore.user?.nickname || userStore.user?.username || '-' }}
              </span>
              <span class="user-panel-tenant">
                {{
                  t('layout.currentTenantLabel', { code: userStore.user?.tenantCode || 'platform' })
                }}
              </span>
            </div>
          </div>
          <div class="user-panel-divider" />
          <button type="button" class="user-panel-action user-panel-link" @click="openProfile">
            <el-icon><User /></el-icon>
            <span>{{ gp('openProfile') }}</span>
          </button>
          <button type="button" class="user-panel-action" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>{{ t('layout.logout') }}</span>
          </button>
        </div>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowDown, FullScreen, ScaleToOriginal, Setting, SwitchButton, User } from '@element-plus/icons-vue'
import LocaleSwitcher from '@/components/LocaleSwitcher.vue'
import NavbarMessages from '@/components/layout/NavbarMessages.vue'
import { useAuthImageUrl } from '@/composables/useAuthImageUrl'
import { useFullscreen } from '@/composables/useFullscreen'
import { useLayoutStore } from '@/stores/layout'
import { useUserStore } from '@/stores/user'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import genI18nZhCN from '@/i18n/gen/profile/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/profile/en-US.json'
import { useGenI18n } from '@/composables/useGenI18n'

const GEN_I18N_FILES: Record<string, GenI18nMessages> = {
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}
const { gp } = useGenI18n('profile', 'auth', GEN_I18N_FILES)

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const layoutStore = useLayoutStore()
const { isFullscreen, toggle: toggleFullscreen } = useFullscreen()

const avatarText = computed(() =>
  (userStore.user?.nickname || userStore.user?.username || 'U').charAt(0).toUpperCase(),
)

const navbarAvatarUrl = useAuthImageUrl(
  computed(() => userStore.user?.avatarUrlSm || userStore.user?.avatarUrl || ''),
)

async function handleLogout() {
  await userStore.logout()
  await router.replace('/login')
}

function openProfile() {
  router.push('/account/profile')
}
</script>

<style scoped>
.layout-navbar {
  display: flex;
  align-items: center;
  gap: 4px;
}

.navbar-divider {
  width: 1px;
  height: 20px;
  margin: 0 8px;
  background: var(--ic-header-border, #e8e8e8);
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px 4px 4px;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  color: var(--ic-header-text, #262626);
  transition: background 0.2s;
}

.user-trigger:hover {
  background: var(--ic-header-hover, #f5f5f5);
}

.user-avatar {
  background: var(--el-color-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 500;
}

.user-arrow {
  font-size: 12px;
  color: var(--ic-header-text-muted, #8c8c8c);
}

.user-panel {
  width: 240px;
  padding: 4px 0;
}

.user-panel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}

.user-avatar-lg {
  flex-shrink: 0;
  background: var(--el-color-primary);
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.user-panel-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-panel-name {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-panel-tenant {
  font-size: 12px;
  color: #8c8c8c;
}

.user-panel-divider {
  height: 1px;
  margin: 0 12px;
  background: #f0f0f0;
}

.user-panel-action {
  display: flex;
  align-items: center;
  gap: 8px;
  width: calc(100% - 16px);
  margin: 4px 8px;
  padding: 10px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #595959;
  font-size: 14px;
  cursor: pointer;
  transition:
    background 0.2s,
    color 0.2s;
}

.user-panel-action:hover {
  background: #fff1f0;
  color: #ff4d4f;
}

.user-panel-link:hover {
  background: #f5f5f5;
  color: var(--el-color-primary);
}
</style>

<style>
.navbar-user-popper.el-popper {
  padding: 0 !important;
  border-radius: 8px !important;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08) !important;
}

.navbar-user-popper .el-popper__arrow {
  display: none;
}
</style>
