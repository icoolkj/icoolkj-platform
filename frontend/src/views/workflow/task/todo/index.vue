<!-- AUTO-GENERATED 我的待办 — GenRuntimePage + view.json -->
<template>
  <GenRuntimePage
    page-key="workflowTaskTodo"
    read-only
    :action-handlers="actionHandlers"
  />
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { GenRuntimePage } from '@/components/gen'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import { useGenPageShellI18n } from '@/composables/gen'
import type { WorkflowTaskVO } from '@/types/workflow/task'

const { gp } = useGenPageShellI18n('workflowTaskTodo')

const router = useRouter()

function normalizeFormRoute(route: string) {
  const path = route.trim()
  if (!path) return path
  return path.replace(/\/index\/?$/i, '') || path
}

function goApprove(row: Record<string, unknown>) {
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
      from: 'todo',
    },
  })
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'workflowTaskTodo.approve': async ({ row }) => goApprove(row),
}
</script>

