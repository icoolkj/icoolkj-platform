<!-- 轮播图 — GenRuntimePage + view.extensions；自定义表单 BannerFormDialog -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="banner"
    selectable
    custom-form
    :status-change="handleStatusChange"
    @open-form="handleOpenForm"
    @selection-change="handleSelectionChange"
  >
    <template #toolbar-extra>
      <el-button
        v-if="!demoStore.isDemo"
        type="danger"
        plain
        :disabled="!selectedIds.length"
        v-hasPermi="['system:banner:delete']"
        @click="handleBatchDelete"
      >
        {{ gp('batchDelete') }}
      </el-button>
    </template>
  </GenRuntimePage>

  <BannerFormDialog
    v-model="formVisible"
    :banner-id="editingId"
    :gp="gp"
    :gf="gf"
    :gv="gv"
    @success="pageRef?.loadPage()"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { GenRuntimePage } from '@/components/gen'
import BannerFormDialog from '@/views/system/banner/BannerFormDialog.vue'
import { useGenPageShellI18n } from '@/composables/gen'
import { useDemoStore } from '@/stores/demo'
import { batchDeleteBanner, updateBannerStatus } from '@/api/system/banner'

const { gp, gf, gv } = useGenPageShellI18n('banner')

const { t } = useI18n()
const demoStore = useDemoStore()
const pageRef = ref<InstanceType<typeof GenRuntimePage>>()

const formVisible = ref(false)
const editingId = ref<string>()
const selectedIds = ref<string[]>([])

function handleOpenForm(row?: Record<string, unknown>) {
  editingId.value = row?.id ? String(row.id) : undefined
  formVisible.value = true
}

function handleSelectionChange(rows: Record<string, unknown>[]) {
  selectedIds.value = rows.map((row) => String(row.id)).filter(Boolean)
}

async function handleStatusChange(row: Record<string, unknown>, status: string) {
  if (!row.id) return
  await updateBannerStatus({ id: String(row.id), status })
  ElMessage.success(t('common.success'))
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) return
  await ElMessageBox.confirm(
    gp('batchDeleteConfirm', { count: selectedIds.value.length }),
    t('common.tip'),
  )
  await batchDeleteBanner({ ids: selectedIds.value })
  ElMessage.success(t('common.success'))
  selectedIds.value = []
  pageRef.value?.clearSelection()
  await pageRef.value?.loadPage()
}
</script>

