<!-- P09 附件测试 — GenListLayout + 说明条 + 内嵌 GenRuntimePage（custom-form） -->
<template>
  <div class="gen-page-shell">
    <GenListLayout
      :title="gp('title')"
      :description="gp('description')"
      :show-search="false"
      :show-right-toolbar="false"
    >
      <template #table-top>
        <el-alert
          class="attachment-demo-guide"
          type="info"
          :closable="false"
          :title="gp('guideTitle')"
          :description="gp('guideBody')"
          show-icon
        />
      </template>

      <GenRuntimePage
        ref="pageRef"
        hide-header
        page-key="attachmentDemo"
        selectable
        custom-form
        :action-handlers="actionHandlers"
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
            v-hasPermi="['infra:attachmentDemo:delete']"
            @click="handleBatchDelete"
          >
            {{ gp('batchDelete') }}
          </el-button>
        </template>
      </GenRuntimePage>
    </GenListLayout>

    <AttachmentDemoFormDialog
      v-model="formVisible"
      :record-id="editingId"
      :gp="gp"
      :gf="gf"
      :gv="gv"
      :form-rules="formRules"
      :catalog-code="catalogCode"
      @success="pageRef?.loadPage()"
    />

    <AttachmentDemoDetailDialog
      v-model="detailVisible"
      :record-id="detailId"
      :gp="gp"
      :gf="gf"
      :catalog-code="catalogCode"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { GenRuntimePage, GenListLayout } from '@/components/gen'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import { loadGenPageBundle, useGenPageShellI18n } from '@/composables/gen'
import type { GenViewSchema } from '@/types/tool/genView'
import AttachmentDemoFormDialog from './AttachmentDemoFormDialog.vue'
import AttachmentDemoDetailDialog from './AttachmentDemoDetailDialog.vue'
import { useDemoStore } from '@/stores/demo'
import {
  batchDeleteAttachmentDemo,
  updateAttachmentDemoStatus,
} from '@/api/infra/attachmentDemo'

const { gp, gf, gv, formRules } = useGenPageShellI18n('attachmentDemo')

const fileCatalogView = ref<GenViewSchema>()
loadGenPageBundle('attachmentDemo').then((bundle) => {
  if (bundle) fileCatalogView.value = bundle.view
})

const catalogCode = computed(
  () =>
    fileCatalogView.value?.fileCatalogs?.find((b) => b.tab === 'form')?.code || 'attachment_demo',
)

const { t } = useI18n()
const demoStore = useDemoStore()
const pageRef = ref<InstanceType<typeof GenRuntimePage>>()

const formVisible = ref(false)
const editingId = ref<string>()
const detailVisible = ref(false)
const detailId = ref<string>()
const selectedIds = ref<string[]>([])

function handleOpenForm(row?: Record<string, unknown>) {
  editingId.value = row?.id ? String(row.id) : undefined
  formVisible.value = true
}

function handleOpenDetail(row: Record<string, unknown>) {
  if (!row?.id) return
  detailId.value = String(row.id)
  detailVisible.value = true
}

function handleSelectionChange(rows: Record<string, unknown>[]) {
  selectedIds.value = rows.map((row) => String(row.id)).filter(Boolean)
}

async function handleStatusChange(row: Record<string, unknown>, status: string) {
  if (!row.id) return
  await updateAttachmentDemoStatus({ id: String(row.id), status })
  ElMessage.success(t('common.success'))
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) return
  await ElMessageBox.confirm(
    gp('batchDeleteConfirm', { count: selectedIds.value.length }),
    t('common.tip'),
  )
  await batchDeleteAttachmentDemo({ ids: selectedIds.value })
  ElMessage.success(t('common.success'))
  selectedIds.value = []
  pageRef.value?.clearSelection()
  await pageRef.value?.loadPage()
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'attachmentDemo.detail': async ({ row }) => handleOpenDetail(row),
}
</script>

<style scoped lang="scss">
.attachment-demo-guide {
  margin-bottom: 12px;
}

.gen-page-shell :deep(.attachment-demo-host .page-container),
.gen-page-shell :deep(.page-container) {
  padding: 0;
}
</style>
