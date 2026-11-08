<!-- 站内信息 — GenRuntimePage；详情/删除由 extensions，发送走工具栏 -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="siteMessage"
    selectable
    read-only
    @selection-change="handleSelectionChange"
  >
    <template #toolbar-extra>
      <el-button
        v-if="!demoStore.isDemo"
        type="primary"
        :icon="Promotion"
        v-hasPermi="['system:siteMessage:send']"
        @click="sendVisible = true"
      >
        {{ gp('sendAction') }}
      </el-button>
      <el-button
        v-if="!demoStore.isDemo"
        type="danger"
        plain
        :disabled="!selectedIds.length"
        v-hasPermi="['system:siteMessage:delete']"
        @click="handleBatchDelete"
      >
        {{ gp('batchDelete') }}
      </el-button>
    </template>
  </GenRuntimePage>

  <SiteMessageSendDialog
    v-model="sendVisible"
    :gp="gp"
    :gf="(field, key) => gf(field, (key as 'label' | 'placeholder') ?? 'label')"
    @success="handleSendSuccess"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import { GenRuntimePage } from '@/components/gen'
import SiteMessageSendDialog from '@/views/system/siteMessage/SiteMessageSendDialog.vue'
import { useGenPageShellI18n } from '@/composables/gen'
import { useDemoStore } from '@/stores/demo'
import { batchDeleteSiteMessage } from '@/api/system/siteMessage'

const { gp, gf } = useGenPageShellI18n('siteMessage')

const { t } = useI18n()
const demoStore = useDemoStore()
const pageRef = ref<InstanceType<typeof GenRuntimePage>>()

const sendVisible = ref(false)
const selectedIds = ref<string[]>([])

function handleSelectionChange(rows: Record<string, unknown>[]) {
  selectedIds.value = rows.map((row) => String(row.id)).filter(Boolean)
}

async function handleSendSuccess() {
  await pageRef.value?.loadPage()
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) return
  await ElMessageBox.confirm(
    gp('batchDeleteConfirm', { count: selectedIds.value.length }),
    t('common.tip'),
  )
  await batchDeleteSiteMessage({ ids: selectedIds.value })
  ElMessage.success(t('common.success'))
  selectedIds.value = []
  pageRef.value?.clearSelection()
  await pageRef.value?.loadPage()
}
</script>

