import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import type { Component } from 'vue'

export const iconMap = ElementPlusIconsVue as Record<string, Component>

/** Resolve Element Plus icon component by PascalCase name stored in menu.icon. */
export function resolveIconComponent(name?: string | null): Component | null {
  if (!name) return null
  return iconMap[name] ?? iconMap.Document ?? null
}

/** All registered @element-plus/icons-vue export names, sorted. */
export function listIconNames(): string[] {
  return Object.keys(iconMap).sort((a, b) => a.localeCompare(b))
}
