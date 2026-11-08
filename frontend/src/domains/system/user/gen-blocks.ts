/**
 * 用户域 Gen 插件 — view.extensions 通过 id 引用。
 */
import type { Component } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import GenUserRoleDialog from './dialogs/GenUserRoleDialog.vue'
import { resetUserPassword, unlockUser } from '@/api/system/user'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'

export const GEN_DIALOG_WIDGETS: Record<string, Component> = {
  GenUserRoleDialog,
}

export const GEN_ROW_ACTION_HANDLERS: Record<string, GenRowActionHandler> = {
  'user.resetPassword': async ({ row, gp, t }) => {
    if (!row.id) return
    await ElMessageBox.confirm(gp('resetPasswordConfirm'), t('common.tip'), { type: 'warning' })
    await resetUserPassword(String(row.id))
    ElMessage.success(gp('resetPasswordSuccess'))
  },
  'user.unlock': async ({ row, gp, t }) => {
    if (!row.id) return
    await ElMessageBox.confirm(gp('unlockConfirm'), t('common.tip'), { type: 'warning' })
    await unlockUser(String(row.id))
    ElMessage.success(gp('unlockSuccess'))
  },
}
