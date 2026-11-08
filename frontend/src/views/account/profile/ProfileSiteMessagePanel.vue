<template>
  <div class="profile-messages">
    <div class="profile-messages-layout">
      <nav class="message-category-nav" :aria-label="pp('messages')">
        <button
          v-for="item in categories"
          :key="item.value"
          type="button"
          class="message-category-item"
          :class="{ active: activeCategory === item.value }"
          @click="handleCategoryChange(item.value)"
        >
          {{ pp(item.labelKey) }}
        </button>
      </nav>

      <div class="message-list-panel">
        <el-table v-loading="loading" :data="dataList" stripe border highlight-current-row>
          <el-table-column
            :label="pp('messageSubject')"
            prop="messageSubject"
            min-width="220"
            show-overflow-tooltip
          />
          <el-table-column :label="pp('messageSentAt')" prop="sentAt" width="170" align="center">
            <template #default="{ row }">{{ formatDateTime(row.sentAt) }}</template>
          </el-table-column>
          <el-table-column :label="pp('messageReadStatus')" width="100" align="center">
            <template #default="{ row }">
              <DictTag :options="getDict('sys_site_message_read_status')" :value="row.readStatus" />
            </template>
          </el-table-column>
          <el-table-column :label="t('common.actions')" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">
                {{ pp('messageDetail') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="table-footer">
          <Pagination
            v-show="total > 0"
            v-model:page="queryParams.pageNo"
            v-model:limit="queryParams.pageSize"
            :total="total"
            @pagination="loadPage"
          />
        </div>
      </div>
    </div>

    <GenFormShell v-model="detailVisible" :title="pp('messageDetailTitle')" :width="760">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item :label="pp('messageSubject')">
          {{ detail.messageSubject }}
        </el-descriptions-item>
        <el-descriptions-item :label="pp('messageSender')">
          {{ detail.senderName }}
        </el-descriptions-item>
        <el-descriptions-item :label="pp('messageSentAt')">
          {{ formatDateTime(detail.sentAt) }}
        </el-descriptions-item>
        <el-descriptions-item :label="pp('messageReadStatus')">
          <DictTag :options="getDict('sys_site_message_read_status')" :value="detail.readStatus" />
        </el-descriptions-item>
        <el-descriptions-item :label="pp('messageContent')">
          <div class="message-content">{{ detail.messageContent }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </GenFormShell>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Pagination from '@/components/Pagination.vue'
import DictTag from '@/components/DictTag.vue'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { useDict } from '@/composables/useDict'
import { isYes } from '@/constants/flags'
import {
  getInboxSiteMessage,
  markInboxSiteMessageRead,
  pageInboxSiteMessage,
} from '@/api/auth/siteMessageInbox'
import type { SiteMessageInboxPageQuery, SiteMessageInboxVO } from '@/types/system/siteMessage'

defineProps<{
  pp: (key: string, params?: Record<string, string | number>) => string
}>()

const { t } = useI18n()
const { getDict } = useDict(['sys_site_message_read_status', 'sys_site_message_category'])

const categories = [
  { value: 'system', labelKey: 'messageCategorySystem' },
  { value: 'business', labelKey: 'messageCategoryBusiness' },
] as const

const activeCategory = ref('system')
const loading = ref(false)
const dataList = ref<SiteMessageInboxVO[]>([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref<SiteMessageInboxVO | null>(null)

const queryParams = reactive<
  Required<Pick<SiteMessageInboxPageQuery, 'pageNo' | 'pageSize'>> & SiteMessageInboxPageQuery
>({
  pageNo: 1,
  pageSize: 10,
  messageCategory: 'system',
})

onMounted(async () => {
  await loadPage()
})

function formatDateTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

async function loadPage() {
  loading.value = true
  try {
    const res = await pageInboxSiteMessage(queryParams)
    dataList.value = res.data.data.list ?? []
    total.value = res.data.data.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleCategoryChange(category: string) {
  activeCategory.value = category
  queryParams.messageCategory = category
  queryParams.pageNo = 1
  loadPage()
}

async function openDetail(row: SiteMessageInboxVO) {
  if (!row.id) return
  if (!isYes(row.readStatus)) {
    await markInboxSiteMessageRead(row.id)
  }
  detail.value = await getInboxSiteMessage(row.id)
  detailVisible.value = true
  await loadPage()
}
</script>

<style scoped>
.profile-messages-layout {
  display: flex;
  gap: 20px;
  min-height: 360px;
}

.message-category-nav {
  flex: 0 0 112px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 4px;
  border-right: 1px solid #f0f0f0;
}

.message-category-item {
  width: 100%;
  padding: 10px 12px;
  border: none;
  border-left: 3px solid transparent;
  border-radius: 0 4px 4px 0;
  background: transparent;
  color: #434343;
  font-size: 14px;
  line-height: 1.4;
  text-align: left;
  cursor: pointer;
  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    border-color 0.2s ease;
}

.message-category-item:hover {
  color: var(--el-color-primary);
  background: color-mix(in srgb, var(--el-color-primary) 6%, transparent);
}

.message-category-item.active {
  color: var(--el-color-primary);
  font-weight: 600;
  background: color-mix(in srgb, var(--el-color-primary) 10%, transparent);
  border-left-color: var(--el-color-primary);
}

.message-list-panel {
  flex: 1;
  min-width: 0;
}

.message-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
