<!-- 租户管理员 — GenRuntimePage（readOnly 列表 + 行操作：重置密码 / 解锁） -->
<template>
  <div class="gen-page-shell">
    <GenRuntimePage page-key="tenantAdmin" :action-handlers="actionHandlers" />

    <TenantProvisionDialog v-model="provisionVisible" :info="provisionInfo" :gp="tenantGp" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { GenRuntimePage } from '@/components/gen'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import { useGenI18n } from '@/composables/useGenI18n'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import type { TenantProvisionVO } from '@/types/tenant/tenant'
import { resetTenantAdminPassword, unlockTenantAdmin } from '@/api/tenant/admin'
import TenantProvisionDialog from '../list/TenantProvisionDialog.vue'
import tenantI18nZhCN from '@/i18n/gen/tenant/zh-CN.json'
import tenantI18nEnUS from '@/i18n/gen/tenant/en-US.json'

const TENANT_I18N_FILES: Record<string, GenI18nMessages> = {
  'zh-CN': tenantI18nZhCN as GenI18nMessages,
  'en-US': tenantI18nEnUS as GenI18nMessages,
}

const { gp: tenantGp } = useGenI18n('tenant', 'tenant', TENANT_I18N_FILES)

const provisionVisible = ref(false)
const provisionInfo = ref<TenantProvisionVO | null>(null)

const actionHandlers: Record<string, GenRowActionHandler> = {
  'tenantAdmin.resetPassword': async ({ row, gp, t, reload }) => {
    const userId = row.userId ? String(row.userId) : ''
    if (!userId) return
    await ElMessageBox.confirm(gp('resetPasswordConfirm'), t('common.tip'), { type: 'warning' })
    const res = await resetTenantAdminPassword(userId)
    provisionInfo.value = res.data.data ?? null
    provisionVisible.value = true
    ElMessage.success(t('common.success'))
    await reload()
  },
  'tenantAdmin.unlock': async ({ row, gp, t, reload }) => {
    const userId = row.userId ? String(row.userId) : ''
    if (!userId) return
    await ElMessageBox.confirm(gp('unlockConfirm'), t('common.tip'))
    await unlockTenantAdmin(userId)
    ElMessage.success(t('common.success'))
    await reload()
  },
}
</script>
