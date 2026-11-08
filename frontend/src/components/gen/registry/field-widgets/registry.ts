/**
 * Gen 表单/查询字段 Widget 注册表 — view.json field.component 映射到 Vue 组件。
 */
import type { Component } from 'vue'

const fieldWidgets = new Map<string, Component>()

export function registerFieldWidget(id: string, component: Component) {
  fieldWidgets.set(id, component)
}

export function getFieldWidget(id: string): Component | undefined {
  return fieldWidgets.get(id)
}

export function listRegisteredFieldWidgetIds(): string[] {
  return [...fieldWidgets.keys()].sort()
}

export function hasFieldWidget(id: string): boolean {
  return fieldWidgets.has(id)
}
