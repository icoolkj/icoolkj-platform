<!-- 在线用户 — GenRuntimePage（readOnly 列表 + 强制下线行操作） -->
<template>
  <div class="gen-page-shell">
    <GenRuntimePage page-key="online" :action-handlers="actionHandlers" />
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { GenRuntimePage } from '@/components/gen'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import { useDemoStore } from '@/stores/demo'
import { useUserStore } from '@/stores/user'
import { forceLogoutOnlineUser } from '@/api/system/online'

const demoStore = useDemoStore()
const userStore = useUserStore()

const actionHandlers: Record<string, GenRowActionHandler> = {
  'monitor.online.forceLogout': async ({ row, gp, t, reload }) => {
    if (demoStore.isDemo) return
    const sessionId = row.sessionId ? String(row.sessionId) : ''
    if (!sessionId) return
    const isSelfUser = row.userId === userStore.user?.userId
    await ElMessageBox.confirm(
      isSelfUser ? gp('forceLogoutSelf') : gp('forceLogoutConfirm'),
      t('common.tip'),
      { type: 'warning' },
    )
    await forceLogoutOnlineUser(sessionId)
    ElMessage.success(gp('forceLogoutSuccess'))
    await reload()
  },
}
</script>
