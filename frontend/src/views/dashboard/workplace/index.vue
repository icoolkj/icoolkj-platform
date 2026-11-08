<template>
  <div class="gen-page-shell">
    <GenListLayout
      v-loading="loading"
      plain
      :title="gp('title')"
      :description="gp('description')"
      class="workbench-index"
    >
    <el-row :gutter="16">
      <el-col :xs="24" :lg="16" class="workbench-main">
        <el-card shadow="never" class="workbench-card workbench-hero">
          <div class="workbench-hero__body">
            <el-avatar :size="72" :src="avatarUrl">{{ avatarFallback }}</el-avatar>
            <div class="workbench-hero__content">
              <div class="workbench-hero__greeting">{{ greetingText }}</div>
              <div class="workbench-hero__meta">
                <span>{{ tenantLabel }}</span>
                <template v-if="header?.deptName">
                  <span class="dot">|</span>
                  <span>{{ header.deptName }}</span>
                </template>
                <template v-if="header?.postLabel">
                  <span class="dot">|</span>
                  <span>{{ header.postLabel }}</span>
                </template>
              </div>
            </div>
          </div>
        </el-card>

        <el-row :gutter="16" class="stats-row">
          <el-col v-for="(item, index) in statCards" :key="item.key" :xs="12" :sm="12" :lg="6">
            <el-card shadow="never" class="workbench-card stat-card" :class="`stat-card--${index}`">
              <div class="stat-card__label">{{ gp(item.labelKey) }}</div>
              <div class="stat-card__value">{{ formatStatValue(item.value) }}</div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-card shadow="never" class="workbench-card">
              <template #header>
                <div class="panel-header">
                  <span>{{ gp('recentLogins') }}</span>
                  <el-button v-if="canViewLoginLog" link type="primary" @click="goLoginLog">
                    {{ gp('more') }}
                  </el-button>
                </div>
              </template>
              <el-empty v-if="!workbench?.recentLoginLogs?.length" :image-size="64" />
              <div v-else class="activity-list">
                <div
                  v-for="(item, idx) in workbench.recentLoginLogs"
                  :key="idx"
                  class="activity-item"
                >
                  <div class="activity-item__title">{{ item.username }}</div>
                  <div class="activity-item__desc">
                    {{ item.location || item.ip || '—' }}
                    <span v-if="item.browser">· {{ item.browser }}</span>
                  </div>
                  <div class="activity-item__time">{{ formatDateTime(item.loginAt) }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-card shadow="never" class="workbench-card">
              <template #header>
                <div class="panel-header">
                  <span>{{ gp('recentOperations') }}</span>
                  <el-button v-if="canViewOperationLog" link type="primary" @click="goOperationLog">
                    {{ gp('more') }}
                  </el-button>
                </div>
              </template>
              <el-empty v-if="!workbench?.recentOperationLogs?.length" :image-size="64" />
              <div v-else class="activity-list">
                <div
                  v-for="(item, idx) in workbench.recentOperationLogs"
                  :key="idx"
                  class="activity-item"
                >
                  <div class="activity-item__title">
                    {{ translateModule(item.moduleName) }}
                    <el-tag
                      size="small"
                      :type="isStatusNormal(item.operationStatus) ? 'success' : 'danger'"
                      class="activity-item__tag"
                    >
                      {{ isStatusNormal(item.operationStatus) ? gp('opSuccess') : gp('opFailed') }}
                    </el-tag>
                  </div>
                  <div class="activity-item__desc">
                    {{ item.operatorName }} · {{ translateBusinessType(item.businessType) }}
                  </div>
                  <div class="activity-item__time">{{ formatDateTime(item.operationAt) }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <NoticeBoardPanel />
      </el-col>

      <el-col :xs="24" :lg="8" class="workbench-side">
        <el-card shadow="never" class="workbench-card">
          <template #header>
            <span>{{ gp('bannerTitle') }}</span>
          </template>
          <WorkbenchBannerPanel :gp="gp" :display-position="bannerDisplayPosition" />
        </el-card>

        <el-card shadow="never" class="workbench-card">
          <template #header>
            <span>{{ gp('quickLinks') }}</span>
          </template>
          <div class="quick-grid">
            <button
              v-for="item in quickLinks"
              :key="item.path"
              type="button"
              class="quick-item"
              @click="goQuickLink(item)"
            >
              <el-icon :size="22"><component :is="item.icon" /></el-icon>
              <span class="quick-item__label">{{ item.label }}</span>
              <span v-if="item.badge != null" class="quick-item__badge">{{ item.badge }}</span>
            </button>
          </div>
        </el-card>

        <el-card shadow="never" class="workbench-card">
          <template #header>
            <div class="panel-header">
              <span>{{ gp('tenantInfo') }}</span>
            </div>
          </template>
          <dl class="tenant-info">
            <div class="tenant-info__row">
              <dt>{{ gp('tenantName') }}</dt>
              <dd>{{ header?.tenantName || '—' }}</dd>
            </div>
            <div class="tenant-info__row">
              <dt>{{ gp('tenantCode') }}</dt>
              <dd>{{ header?.tenantCode || '—' }}</dd>
            </div>
            <div class="tenant-info__row">
              <dt>{{ gp('scopeLabel') }}</dt>
              <dd>{{ header?.platformOperator ? gp('scopePlatform') : gp('scopeTenant') }}</dd>
            </div>
            <div class="tenant-info__row">
              <dt>{{ gp('unreadMessages') }}</dt>
              <dd>{{ workbench?.unreadSiteMessageCount ?? 0 }}</dd>
            </div>
          </dl>
        </el-card>
      </el-col>
    </el-row>
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Message, User, UserFilled } from '@element-plus/icons-vue'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import { isStatusNormal } from '@/constants/flags'
import NoticeBoardPanel from '@/views/dashboard/workplace/NoticeBoardPanel.vue'
import WorkbenchBannerPanel from '@/views/dashboard/workplace/WorkbenchBannerPanel.vue'
import { fetchWorkbench } from '@/api/dashboard/workbench'
import { useAuthImageUrl } from '@/composables/useAuthImageUrl'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useOperationLogI18n } from '@/composables/useOperationLogI18n'
import { useMenuStore } from '@/stores/menu'
import { useUserStore } from '@/stores/user'
import { findMenuPath } from '@/utils/menuPath'
import { formatDateTime } from '@/utils/formatDateTime'
import type { WorkbenchStat, WorkbenchVO } from '@/types/dashboard/workbench'
const { gp } = useGenPageShellI18n('workbench')
const { translateModule, translateBusinessType } = useOperationLogI18n()

const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()

const loading = ref(false)
const workbench = ref<WorkbenchVO | null>(null)

const header = computed(() => workbench.value?.header)
/** 工作台问候区头像：优先大图，其次中图，最后导航用小图 */
const avatarSource = computed(
  () => userStore.user?.avatarUrlLg || userStore.user?.avatarUrl || userStore.user?.avatarUrlSm,
)
const avatarUrl = useAuthImageUrl(avatarSource, 'infra')
const avatarFallback = computed(() => (header.value?.nickname ?? 'U').slice(0, 1))

const statLabelMap: Record<string, string> = {
  tenantCount: 'statTenants',
  userCount: 'statUsers',
  deptCount: 'statDepts',
  roleCount: 'statRoles',
  postCount: 'statPosts',
  todayLogins: 'statTodayLogins',
}

const statCards = computed(() =>
  (workbench.value?.stats ?? []).map((item: WorkbenchStat) => ({
    key: item.key,
    value: item.value,
    labelKey: statLabelMap[item.key] ?? item.key,
  })),
)

const greetingText = computed(() => {
  const name = header.value?.nickname ?? gp('welcome')
  const hour = new Date().getHours()
  if (hour < 6) return gp('greetingNight', { name })
  if (hour < 12) return gp('greetingMorning', { name })
  if (hour < 18) return gp('greetingAfternoon', { name })
  return gp('greetingEvening', { name })
})

const tenantLabel = computed(() => {
  if (header.value?.platformOperator) {
    return gp('platformTenantLabel', { name: header.value?.tenantName ?? gp('scopePlatform') })
  }
  return header.value?.tenantName ?? header.value?.tenantCode ?? '—'
})

/** 平台运营工作台用 admin_system，租户业务工作台用 admin_biz */
const bannerDisplayPosition = computed(() =>
  header.value?.platformOperator ? 'admin_system' : 'admin_biz',
)

const permissions = computed(() => userStore.permissions ?? [])
const canViewLoginLog = computed(() => permissions.value.includes('monitor:loginLog:view'))
const canViewOperationLog = computed(() => permissions.value.includes('monitor:operationLog:view'))

const quickLinks = computed(() => {
  const unread = workbench.value?.unreadSiteMessageCount ?? 0
  const perms = permissions.value
  const menus = menuStore.routers
  const links = [
    {
      routeName: 'SystemUser',
      fallbackPath: '/system/user',
      label: gp('quickUsers'),
      icon: User,
      badge: null as number | null,
      perm: 'system:user:view',
    },
    {
      routeName: 'SystemRole',
      fallbackPath: '/system/role',
      label: gp('quickRoles'),
      icon: UserFilled,
      badge: null,
      perm: 'system:role:view',
    },
    {
      routeName: 'SystemNotice',
      fallbackPath: '/system/notice',
      label: gp('quickNotices'),
      icon: Bell,
      badge: null,
      perm: 'system:notice:view',
    },
    {
      routeName: 'AccountProfile',
      fallbackPath: '/account/profile',
      query: { tab: 'messages' },
      label: gp('quickSiteMessages'),
      icon: Message,
      badge: unread > 0 ? unread : null,
      perm: null as string | null,
    },
  ]
  return links
    .filter((item) => !item.perm || perms.includes(item.perm))
    .map((item) => ({
      ...item,
      path:
        item.routeName === 'AccountProfile'
          ? item.fallbackPath
          : (findMenuPath(menus, item.routeName) ?? item.fallbackPath),
    }))
})

function formatStatValue(value?: number) {
  if (value == null) return '—'
  return String(value)
}

function resolveMenuRoute(routeName: string, fallbackPath: string) {
  return findMenuPath(menuStore.routers, routeName) ?? fallbackPath
}

function goLoginLog() {
  router.push(resolveMenuRoute('MonitorLoginLog', '/system/loginLog'))
}

function goOperationLog() {
  router.push(resolveMenuRoute('MonitorOperationLog', '/system/operationLog'))
}

function goQuickLink(item: { path: string; query?: Record<string, string> }) {
  if (item.query) {
    router.push({ path: item.path, query: item.query })
    return
  }
  router.push(item.path)
}

async function loadWorkbench() {
  loading.value = true
  try {
    workbench.value = await fetchWorkbench()
  } finally {
    loading.value = false
  }
}

onMounted(loadWorkbench)
</script>

<style scoped lang="scss">
.workbench-index {
  min-height: 100%;
}

.workbench-card {
  margin-bottom: 16px;
  border: none;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.workbench-hero__body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.workbench-hero__greeting {
  font-size: 22px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.workbench-hero__meta {
  margin-top: 8px;
  font-size: 14px;
  color: var(--el-text-color-secondary);

  .dot {
    margin: 0 8px;
    opacity: 0.5;
  }
}

.stats-row {
  margin-bottom: 0;
}

.stat-card {
  &__label {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin-bottom: 8px;
  }

  &__value {
    font-size: 28px;
    font-weight: 600;
    line-height: 1.2;
  }

  &--0 .stat-card__value {
    color: #1677ff;
  }
  &--1 .stat-card__value {
    color: #52c41a;
  }
  &--2 .stat-card__value {
    color: #fa8c16;
  }
  &--3 .stat-card__value {
    color: #f5222d;
  }
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--el-border-color-lighter);

  &:last-child {
    padding-bottom: 0;
    border-bottom: none;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  &__tag {
    flex-shrink: 0;
  }

  &__desc {
    margin-top: 4px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  &__time {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  background: var(--el-fill-color-blank);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;

  &:hover {
    border-color: var(--el-color-primary-light-5);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  &__label {
    font-size: 13px;
    color: var(--el-text-color-regular);
  }

  &__badge {
    min-width: 20px;
    padding: 0 6px;
    border-radius: 10px;
    background: var(--el-color-danger);
    color: #fff;
    font-size: 12px;
    line-height: 20px;
  }
}

.tenant-info {
  margin: 0;

  &__row {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    padding: 10px 0;
    border-bottom: 1px dashed var(--el-border-color-lighter);

    &:last-child {
      border-bottom: none;
    }

    dt {
      margin: 0;
      color: var(--el-text-color-secondary);
      font-size: 13px;
    }

    dd {
      margin: 0;
      text-align: right;
      color: var(--el-text-color-primary);
      font-size: 13px;
    }
  }
}
</style>
