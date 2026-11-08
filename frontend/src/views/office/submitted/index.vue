<!-- 在线办公 · 已发申请 — 工作流实例视角：每业务最新一轮；仅查看 / 撤回 -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="officeSubmitted"
    read-only
    :action-handlers="actionHandlers"
    :row-action-visible="rowActionVisible"
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
import { canWithdrawSubmittedInstanceRow } from '@/composables/useWorkflowBizRowActions'
import { useGenPageShellI18n } from '@/composables/gen'
import { cancelWorkflowInstance } from '@/api/workflow/instance'
import type { WorkflowInstanceVO } from '@/types/workflow/instance'

const { gp } = useGenPageShellI18n('officeSubmitted')
const { t } = useI18n()
const router = useRouter()

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()

function normalizeFormRoute(route: string) {
  const path = route.trim()
  if (!path) return path
  return path.replace(/\/index\/?$/i, '') || path
}

function openView(row: Record<string, unknown>) {
  const inst = row as WorkflowInstanceVO
  if (!inst.formRoute?.trim()) {
    ElMessage.warning(gp('missingFormRoute'))
    return
  }
  if (!inst.bizId) {
    ElMessage.warning(gp('missingBizId'))
    return
  }
  router.push({
    path: normalizeFormRoute(inst.formRoute),
    query: {
      bizId: inst.bizId,
      mode: 'view',
      from: 'submitted',
    },
  })
}

async function handleWithdraw(row: Record<string, unknown>) {
  const instanceId = row.instanceId ? String(row.instanceId) : ''
  if (!instanceId) {
    ElMessage.warning(gp('missingInstanceId'))
    return
  }
  await ElMessageBox.confirm(gp('withdrawConfirm'), t('common.tip'), { type: 'warning' })
  await cancelWorkflowInstance(instanceId)
  ElMessage.success(gp('withdrawSuccess'))
  pageRef.value?.loadPage()
}

function rowActionVisible(action: GenViewRowAction, row: Record<string, unknown>) {
  if (action.id === 'withdraw') return canWithdrawSubmittedInstanceRow(row)
  return undefined
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'officeSubmitted.view': async ({ row }) => openView(row),
  'officeSubmitted.withdraw': async ({ row }) => handleWithdraw(row),
}
</script>

