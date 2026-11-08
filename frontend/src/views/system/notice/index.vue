<!-- 通知公告 — GenRuntimePage + view.extensions；行 handler 为页内注册 -->
<template>
  <GenRuntimePage page-key="notice" :action-handlers="actionHandlers" />
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { GenRuntimePage } from '@/components/gen'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import {
  closeNotice,
  publishNotice,
  submitNoticeApprove,
  unpublishNotice,
  withdrawNoticeApprove,
} from '@/api/system/notice'
import { useGenPageShellI18n } from '@/composables/gen'

const { gp } = useGenPageShellI18n('notice')

const { t } = useI18n()
const router = useRouter()

function openView(row: Record<string, unknown>) {
  if (!row.id) return
  router.push({
    path: '/system/notice/approve',
    query: {
      bizId: String(row.id),
      mode: 'view',
      from: 'notice',
    },
  })
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'notice.view': async ({ row }) => openView(row),
  'notice.submitApprove': async ({ row, reload }) => {
    if (!row.id) return
    await ElMessageBox.confirm(gp('submitApproveConfirm'), t('common.tip'))
    await submitNoticeApprove(String(row.id))
    ElMessage.success(t('common.success'))
    await reload()
  },
  'notice.withdrawApprove': async ({ row, reload }) => {
    if (!row.id) return
    await ElMessageBox.confirm(gp('withdrawApproveConfirm'), t('common.tip'))
    await withdrawNoticeApprove(String(row.id))
    ElMessage.success(t('common.success'))
    await reload()
  },
  'notice.publish': async ({ row, reload }) => {
    if (!row.id) return
    await ElMessageBox.confirm(gp('publishConfirm'), t('common.tip'))
    await publishNotice(String(row.id))
    ElMessage.success(gp('publishSuccess'))
    await reload()
  },
  'notice.unpublish': async ({ row, reload }) => {
    if (!row.id) return
    await ElMessageBox.confirm(gp('unpublishConfirm'), t('common.tip'), { type: 'warning' })
    await unpublishNotice(String(row.id))
    ElMessage.success(gp('unpublishSuccess'))
    await reload()
  },
  'notice.close': async ({ row, reload }) => {
    if (!row.id) return
    await ElMessageBox.confirm(gp('closeConfirm'), t('common.tip'), { type: 'warning' })
    await closeNotice(String(row.id))
    ElMessage.success(gp('closeSuccess'))
    await reload()
  },
}
</script>

