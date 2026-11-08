<!-- AUTO-GENERATED 抄送给我的 — GenRuntimePage + view.json -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="workflowTaskCc"
    read-only
    :action-handlers="actionHandlers"
    :row-action-visible="rowActionVisible"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { GenRuntimePage } from '@/components/gen'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import type { GenViewRowAction } from '@/types/tool/genView'
import { useGenPageShellI18n } from '@/composables/gen'
import type { WorkflowTaskVO } from '@/types/workflow/task'

const { gp } = useGenPageShellI18n('workflowTaskCc')

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
const router = useRouter()

function isAcknowledged(row: Record<string, unknown>) {
  return Boolean((row as WorkflowTaskVO).endTime)
}

function normalizeFormRoute(route: string) {
  const path = route.trim()
  if (!path) return path
  return path.replace(/\/index\/?$/i, '') || path
}

function goAck(row: Record<string, unknown>) {
  const task = row as WorkflowTaskVO
  if (!task.formRoute?.trim()) {
    ElMessage.warning(gp('missingFormRoute'))
    return
  }
  router.push({
    path: normalizeFormRoute(task.formRoute),
    query: {
      taskId: task.taskId,
      bizId: task.bizId,
      from: 'cc',
    },
  })
}

function goView(row: Record<string, unknown>) {
  const task = row as WorkflowTaskVO
  if (!task.formRoute?.trim()) {
    ElMessage.warning(gp('missingFormRoute'))
    return
  }
  if (!task.bizId) {
    ElMessage.warning(gp('missingBizId'))
    return
  }
  router.push({
    path: normalizeFormRoute(task.formRoute),
    query: {
      bizId: task.bizId,
      mode: 'view',
      from: 'cc',
    },
  })
}

function rowActionVisible(action: GenViewRowAction, row: Record<string, unknown>) {
  if (action.id === 'ack') return !isAcknowledged(row)
  if (action.id === 'view') return isAcknowledged(row)
  return undefined
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'workflowTaskCc.ack': async ({ row }) => goAck(row),
  'workflowTaskCc.view': async ({ row }) => goView(row),
}
</script>

