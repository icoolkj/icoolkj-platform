<!-- 在线办公 · 我的草稿 — OA 业务单据：修改/提交/关闭（关闭写 oa_instance） -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="officeDraft"
    custom-form
    read-only
    :action-handlers="actionHandlers"
    :row-action-visible="rowActionVisible"
    @open-form="handleOpenForm"
  />

  <OaInstanceFormDialog
    v-model="formVisible"
    :instance-id="editingId"
    :category-code="editingCategory"
    @success="pageRef?.loadPage()"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { GenRuntimePage } from '@/components/gen'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import type { GenViewRowAction } from '@/types/tool/genView'
import OaInstanceFormDialog from '@/views/oa/components/OaInstanceFormDialog.vue'
import {
  canCloseWorkflowBizRow,
  canDeleteWorkflowBizRow,
} from '@/composables/useWorkflowBizRowActions'
import { useGenPageShellI18n } from '@/composables/gen'
import { closeOaInstance, submitOaInstanceApprove } from '@/api/oa/oaInstance'

const { gp } = useGenPageShellI18n('officeDraft')
const { t } = useI18n()
const router = useRouter()

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
const formVisible = ref(false)
const editingId = ref<string>()
const editingCategory = ref<string>()

function handleOpenForm(row?: Record<string, unknown>) {
  if (!row?.id) return
  openEdit(row)
}

function openEdit(row: Record<string, unknown>) {
  if (!row.id) return
  editingId.value = String(row.id)
  editingCategory.value = row.categoryCode ? String(row.categoryCode) : undefined
  formVisible.value = true
}

function openView(row: Record<string, unknown>) {
  if (!row.id) return
  router.push({
    path: '/office/approve',
    query: {
      bizId: String(row.id),
      mode: 'view',
      from: 'draft',
    },
  })
}

async function handleSubmit(row: Record<string, unknown>) {
  if (!row.id) return
  await submitOaInstanceApprove(String(row.id))
  ElMessage.success(gp('submitSuccess'))
  pageRef.value?.loadPage()
}

async function handleClose(row: Record<string, unknown>) {
  if (!row.id) return
  await ElMessageBox.confirm(gp('closeConfirm'), t('common.tip'), { type: 'warning' })
  await closeOaInstance(String(row.id))
  ElMessage.success(gp('closeSuccess'))
  pageRef.value?.loadPage()
}

function rowActionVisible(action: GenViewRowAction, row: Record<string, unknown>) {
  if (action.id === 'delete') return canDeleteWorkflowBizRow(row)
  if (action.id === 'close') return canCloseWorkflowBizRow(row)
  return undefined
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'officeDraft.view': async ({ row }) => openView(row),
  'officeDraft.submit': async ({ row }) => handleSubmit(row),
  'officeDraft.close': async ({ row }) => handleClose(row),
}
</script>

