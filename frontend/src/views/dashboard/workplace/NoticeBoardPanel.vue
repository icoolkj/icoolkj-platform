<template>
  <el-card shadow="never" class="notice-board-card">
    <template #header>
      <div class="notice-board-card__header">
        <span>{{ gp('boardTitle') }}</span>
        <el-button link type="primary" @click="loadNotices">{{ t('common.refresh') }}</el-button>
      </div>
    </template>

    <div v-loading="loading">
      <el-empty v-if="!notices.length" :description="gp('boardEmpty')" />
      <div v-else class="notice-board-list">
        <article
          v-for="item in notices"
          :key="String(item.id)"
          class="notice-board-item"
          @click="openDetail(item)"
        >
          <div class="notice-board-item__title">{{ item.noticeTitle }}</div>
          <div v-if="item.noticeSummary" class="notice-board-item__summary">
            {{ item.noticeSummary }}
          </div>
          <div class="notice-board-item__meta">
            <DictTag :options="getDict('sys_notice_type')" :value="item.noticeType" />
            <span>{{ formatDateTime(item.createdAt) }}</span>
          </div>
        </article>
      </div>
    </div>

    <el-dialog
      v-model="detailVisible"
      :title="detail?.noticeTitle ?? gp('boardDetailTitle')"
      width="860"
      destroy-on-close
      @closed="handleDetailClosed"
    >
      <div v-if="detail" v-loading="detailLoading">
        <p v-if="detail.noticeSummary" class="notice-board-detail__summary">
          {{ detail.noticeSummary }}
        </p>
        <RichHtmlContent
          :active="detailVisible"
          :html="detail.noticeContent"
          download-scope="notice"
        />
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import DictTag from '@/components/DictTag.vue'
import RichHtmlContent from '@/components/RichHtmlContent.vue'
import { useDict } from '@/composables/useDict'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { getPublishedNotice, listPublishedNotice } from '@/api/system/notice'
import { formatDateTime } from '@/utils/formatDateTime'
import type { NoticePublishedVO } from '@/types/system/notice'
const { t } = useI18n()
const { gp } = useGenPageShellI18n('notice')
const { getDict } = useDict(['sys_notice_type'])

const loading = ref(false)
const notices = ref<NoticePublishedVO[]>([])
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<NoticePublishedVO | null>(null)

async function loadNotices() {
  loading.value = true
  try {
    const res = await listPublishedNotice({ publishLocation: 'admin_biz', limit: 8 })
    notices.value = res.data.data ?? []
  } finally {
    loading.value = false
  }
}

async function openDetail(item: NoticePublishedVO) {
  if (!item.id) return
  detailVisible.value = true
  detailLoading.value = true
  detail.value = item
  try {
    const res = await getPublishedNotice(String(item.id))
    detail.value = res.data.data
  } finally {
    detailLoading.value = false
  }
}

function handleDetailClosed() {
  detail.value = null
}

onMounted(loadNotices)
</script>

<style scoped lang="scss">
.notice-board-card {
  margin-bottom: 16px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

.notice-board-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-board-item {
  padding: 12px 14px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;

  &:hover {
    border-color: var(--el-color-primary-light-5);
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  &__summary {
    margin-top: 6px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    line-height: 1.6;
  }

  &__meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-top: 10px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.notice-board-detail__summary {
  margin: 0 0 12px;
  color: var(--el-text-color-secondary);
}
</style>
