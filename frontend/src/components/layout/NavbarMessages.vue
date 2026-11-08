<template>
  <el-popover
    :visible="open"
    placement="bottom-end"
    :width="360"
    trigger="click"
    @update:visible="(v: boolean) => (open = v)"
  >
    <template #reference>
      <el-badge :value="unread || undefined" :hidden="!unread" :max="99">
        <el-tooltip :content="t('layout.messages')" placement="bottom">
          <button
            type="button"
            class="shell-header-icon-btn"
            :aria-label="t('layout.messages')"
            @click="open = !open"
          >
            <el-icon><Bell /></el-icon>
          </button>
        </el-tooltip>
      </el-badge>
    </template>

    <div class="msg-panel">
      <div class="msg-panel__head">
        <span>{{ t('layout.messages') }}</span>
        <el-button link type="primary" @click="goInbox">{{ t('layout.messagesViewAll') }}</el-button>
      </div>
      <el-scrollbar max-height="280px">
        <div v-if="loading" class="msg-panel__empty">{{ t('common.loading') }}</div>
        <div v-else-if="!items.length" class="msg-panel__empty">{{ t('layout.messagesEmpty') }}</div>
        <button
          v-for="row in items"
          :key="row.id"
          type="button"
          class="msg-panel__item"
          @click="openItem(row)"
        >
          <span class="msg-panel__title">{{ row.messageSubject || '-' }}</span>
          <span class="msg-panel__meta">{{ row.sentAt || '' }}</span>
        </button>
      </el-scrollbar>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Bell } from '@element-plus/icons-vue'
import {
  countUnreadSiteMessage,
  pageInboxSiteMessage,
} from '@/api/auth/siteMessageInbox'
import type { SiteMessageInboxVO } from '@/types/system/siteMessage'

const { t } = useI18n()
const router = useRouter()
const open = ref(false)
const unread = ref(0)
const loading = ref(false)
const items = ref<SiteMessageInboxVO[]>([])

async function refreshUnread() {
  try {
    const res = await countUnreadSiteMessage()
    unread.value = Number(res.data.data ?? 0)
  } catch {
    unread.value = 0
  }
}

async function loadRecent() {
  loading.value = true
  try {
    const res = await pageInboxSiteMessage({ pageNo: 1, pageSize: 8 })
    items.value = res.data.data?.list ?? []
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

function goInbox() {
  open.value = false
  router.push({ path: '/account/profile', query: { tab: 'messages' } })
}

function openItem(row: SiteMessageInboxVO) {
  open.value = false
  router.push({ path: '/account/profile', query: { tab: 'messages', id: row.id } })
}

watch(open, (v) => {
  if (v) void loadRecent()
})

onMounted(() => {
  void refreshUnread()
})

defineExpose({ refreshUnread })
</script>

<style scoped>
.msg-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
}
.msg-panel__empty {
  padding: 24px 8px;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.msg-panel__item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
  padding: 8px;
  border: none;
  border-radius: 6px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}
.msg-panel__item:hover {
  background: var(--el-fill-color-light);
}
.msg-panel__title {
  font-size: 13px;
  color: var(--el-text-color-primary);
}
.msg-panel__meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
