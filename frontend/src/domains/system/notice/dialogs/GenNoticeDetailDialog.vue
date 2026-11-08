<template>
  <GenFormShell v-model="visible" :title="title || gp('detailTitle')" :width="960">
    <div v-loading="loading">
      <el-descriptions v-if="detail" :column="2" border size="small" class="detail-meta">
        <el-descriptions-item :label="gf('noticeTitle', 'label')" :span="2">
          {{ detail.noticeTitle }}
        </el-descriptions-item>
        <el-descriptions-item :label="gf('noticeType', 'label')">
          <DictTag :options="getDict('sys_notice_type')" :value="detail.noticeType" />
        </el-descriptions-item>
        <el-descriptions-item :label="gf('status', 'label')">
          <DictTag :options="getDict('sys_notice_status')" :value="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item :label="gf('sortNo', 'label')">
          {{ detail.sortNo ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="gf('createdAt', 'label')">
          {{ formatDateTime(detail.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item :label="gf('publishLocations', 'label')" :span="2">
          <DictTag
            v-for="item in publishLocationValues"
            :key="item"
            class="dict-tags-item"
            :options="getDict('sys_notice_publish_location')"
            :value="item"
          />
        </el-descriptions-item>
        <el-descriptions-item :label="gf('noticeSummary', 'label')" :span="2">
          {{ detail.noticeSummary }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="detail.coverPreviewUrl"
          :label="gf('coverFileId', 'label')"
          :span="2"
        >
          <AuthImage
            :src="detail.coverPreviewUrl"
            download-scope="notice"
            fit="cover"
            image-class="detail-cover"
            previewable
          />
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.remark" :label="gf('remark', 'label')" :span="2">
          {{ detail.remark }}
        </el-descriptions-item>
      </el-descriptions>

      <el-tabs v-if="detail" type="border-card" class="detail-tabs">
        <el-tab-pane :label="gf('noticeContent', 'label')">
          <RichHtmlContent :active="visible" :html="detail.noticeContent" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <el-button type="primary" @click="visible = false">{{ t('common.confirm') }}</el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import DictTag from '@/components/DictTag.vue'
import AuthImage from '@/components/AuthImage.vue'
import RichHtmlContent from '@/components/RichHtmlContent.vue'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { useDict } from '@/composables/useDict'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { getNotice } from '@/api/system/notice'
import type { NoticeVO } from '@/types/system/notice'
import { formatDateTime } from '@/utils/formatDateTime'
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue: boolean
  noticeId?: string | null
  title?: string
}>()

const emit = defineEmits<{ 'update:modelValue': [boolean] }>()

const { gp, gf } = useGenPageShellI18n('notice')
const { t } = useI18n()
const { getDict } = useDict(['sys_notice_type', 'sys_notice_status', 'sys_notice_publish_location'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const loading = ref(false)
const detail = ref<NoticeVO | null>(null)

const publishLocationValues = computed(() => {
  if (!detail.value?.publishLocations) return []
  return detail.value.publishLocations
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
})

watch(
  () => [props.modelValue, props.noticeId] as const,
  async ([open, noticeId]) => {
    if (!open || !noticeId) {
      detail.value = null
      return
    }
    loading.value = true
    try {
      const res = await getNotice(noticeId)
      detail.value = res.data.data
    } finally {
      loading.value = false
    }
  },
)
</script>

<style scoped lang="scss">
.detail-meta {
  margin-bottom: 12px;
}

.detail-tabs {
  margin-top: 8px;

  :deep(.el-tabs__content) {
    padding: 10px 12px;
  }
}

.detail-cover {
  width: 160px;
  height: 90px;
  border-radius: 4px;
}
</style>
