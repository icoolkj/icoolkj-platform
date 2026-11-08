<!-- 业务展示：只读查看附件（可下载预览，不可上传/删除） -->
<template>
  <GenFormShell
    v-model="visibleModel"
    :title="gp('detailTitle')"
    :width="920"
    :close-on-click-modal="false"
  >
    <el-descriptions :column="1" border class="attachment-demo-detail">
      <el-descriptions-item :label="gf('title')">{{ detail.title || '—' }}</el-descriptions-item>
      <el-descriptions-item :label="gf('status')">{{ statusLabel }}</el-descriptions-item>
      <el-descriptions-item :label="gf('remark')">{{ detail.remark || '—' }}</el-descriptions-item>
      <el-descriptions-item v-if="detail.createdAt" :label="gf('createdAt')">
        {{ detail.createdAt }}
      </el-descriptions-item>
    </el-descriptions>

    <div class="attachment-demo-section-title">{{ gp('sectionAttachments') }}</div>
    <CatalogAttachmentManager
      :model-value="detail.assets || {}"
      :catalog-code="catalogCode"
      :biz-id="recordId"
      :download-scope="FILE_DOWNLOAD_SCOPE_INFRA"
      :disabled="true"
      :gp="gp"
    />

    <template #footer>
      <el-button type="primary" @click="visibleModel = false">{{ t('common.confirm') }}</el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { CatalogAttachmentManager, FILE_DOWNLOAD_SCOPE_INFRA } from '@/components/attachment'
import { useDict } from '@/composables/useDict'
import { getAttachmentDemo } from '@/api/infra/attachmentDemo'

const props = defineProps<{
  modelValue: boolean
  recordId?: string
  catalogCode: string
  gp: (key: string, params?: Record<string, string | number>) => string
  gf: (field: string, prop?: 'label' | 'placeholder') => string
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
}>()

const { t } = useI18n()
const { getDict } = useDict(['sys_common_status'])

const visibleModel = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const detail = reactive<{
  title: string
  status: string
  remark: string
  createdAt: string
  assets: Record<string, string | string[]>
}>({
  title: '',
  status: '',
  remark: '',
  createdAt: '',
  assets: {},
})

const statusLabel = computed(() => {
  const opt = getDict('sys_common_status').find((o) => String(o.value) === String(detail.status))
  return opt?.label || detail.status || '—'
})

watch(
  () => props.modelValue,
  async (open) => {
    if (!open || !props.recordId) return
    detail.title = ''
    detail.status = ''
    detail.remark = ''
    detail.createdAt = ''
    detail.assets = {}
    const res = await getAttachmentDemo(props.recordId)
    const data = res.data.data
    detail.title = data.title || ''
    detail.status = data.status || ''
    detail.remark = data.remark || ''
    detail.createdAt = data.createdAt || ''
    detail.assets = { ...(data.assets || {}) }
  },
)
</script>

<style scoped lang="scss">
.attachment-demo-detail {
  margin-bottom: 16px;
}

.attachment-demo-section-title {
  margin: 4px 0 10px;
  padding-left: 8px;
  font-weight: 600;
  font-size: 14px;
  color: var(--el-text-color-primary);
  border-left: 3px solid var(--el-color-primary);
}
</style>
