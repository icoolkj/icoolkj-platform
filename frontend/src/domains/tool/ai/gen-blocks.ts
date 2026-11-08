import type { Component } from 'vue'
import GenAiInvokeLogDetailDialog from './dialogs/GenAiInvokeLogDetailDialog.vue'
import GenAiDdlLogDetailDialog from './dialogs/GenAiDdlLogDetailDialog.vue'
import GenAiChatLogDetailDialog from './dialogs/GenAiChatLogDetailDialog.vue'

export const GEN_DIALOG_WIDGETS: Record<string, Component> = {
  GenAiInvokeLogDetailDialog,
  GenAiDdlLogDetailDialog,
  GenAiChatLogDetailDialog,
}
