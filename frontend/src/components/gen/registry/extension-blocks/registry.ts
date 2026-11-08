/**
 * Gen 积木注册表：特殊交互（行动作 handler、对话框 widget）在此注册，
 * 由 view.json `extensions` 按 id 引用，避免业务页手写布局。
 */
import type { Component } from 'vue'

export type GenRowActionContext = {
  row: Record<string, unknown>
  /** page gp() */
  gp: (key: string, params?: Record<string, string | number>) => string
  /** vue-i18n t() */
  t: (key: string, params?: Record<string, unknown>) => string
  /** reload list after side-effect */
  reload: () => Promise<void>
}

export type GenRowActionHandler = (ctx: GenRowActionContext) => void | Promise<void>

const rowActionHandlers = new Map<string, GenRowActionHandler>()
const dialogWidgets = new Map<string, Component>()

export function registerRowActionHandler(id: string, handler: GenRowActionHandler) {
  rowActionHandlers.set(id, handler)
}

export function getRowActionHandler(id: string): GenRowActionHandler | undefined {
  return rowActionHandlers.get(id)
}

export function registerDialogWidget(id: string, component: Component) {
  dialogWidgets.set(id, component)
}

export function getDialogWidget(id: string): Component | undefined {
  return dialogWidgets.get(id)
}

export function listRegisteredBlockIds() {
  return {
    rowActionHandlers: [...rowActionHandlers.keys()],
    dialogWidgets: [...dialogWidgets.keys()],
  }
}
