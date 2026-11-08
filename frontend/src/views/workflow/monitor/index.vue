<!-- 流程监控：本租户实例 + 当前办理人；管理员可终止 -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="workflowMonitor"
    read-only
    :api-override="apiOverride"
    :action-handlers="actionHandlers"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { GenRuntimePage } from '@/components/gen'
import type { GenCrudApi } from '@/types/gen/crudApi'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import { useGenPageShellI18n } from '@/composables/gen'
import { pageWorkflowTenant, terminateWorkflowInstance } from '@/api/workflow/instance'
import type { WorkflowInstanceVO } from '@/types/workflow/instance'

const { gp } = useGenPageShellI18n('workflowMonitor')
const { t } = useI18n()

const router = useRouter()
const pageRef = ref<InstanceType<typeof GenRuntimePage>>()

const apiOverride = computed<Partial<GenCrudApi>>(() => ({
  page: async (params) => {
    const res = await pageWorkflowTenant(params)
    const page = res.data.data
    if (page?.list?.length) {
      for (const row of page.list) {
        if (row.assigneeUnresolved) {
          row.currentAssigneeUserNames = gp('unresolvedAssignee')
        } else if (!row.currentAssigneeUserNames) {
          row.currentAssigneeUserNames = '—'
        }
        if (!row.currentTaskName) {
          row.currentTaskName = '—'
        }
      }
    }
    return res
  },
}))

function normalizeFormRoute(route: string) {
  const path = route.trim()
  if (!path) return path
  return path.replace(/\/index\/?$/i, '') || path
}

function goView(row: Record<string, unknown>) {
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
      from: 'monitor',
    },
  })
}

async function handleTerminate(row: Record<string, unknown>) {
  const inst = row as WorkflowInstanceVO
  if (!inst.instanceId) return
  await ElMessageBox.confirm(gp('terminateConfirm'), t('common.tip'))
  await terminateWorkflowInstance(inst.instanceId, { reason: gp('terminateReasonDefault') })
  ElMessage.success(t('common.success'))
  await pageRef.value?.loadPage()
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'workflowMonitor.view': async ({ row }) => goView(row),
  'workflowMonitor.terminate': async ({ row }) => handleTerminate(row),
}
</script>

