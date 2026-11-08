/**
 * 域插件 glob 聚合 — dialogs / row handlers / tree sources。
 * 约定：domains/{domain}/gen-blocks.ts 导出 GEN_DIALOG_WIDGETS 等。
 */
import type { Component } from 'vue'
import {
  registerDialogWidget,
  registerRowActionHandler,
  type GenRowActionHandler,
} from '../extension-blocks/registry'
import { registerTreeSourceApi, type GenTreeSourceApiLoader } from '../tree-sources/registry'

export type DomainGenBlocksModule = {
  GEN_DIALOG_WIDGETS?: Record<string, Component>
  GEN_ROW_ACTION_HANDLERS?: Record<string, GenRowActionHandler>
  GEN_TREE_SOURCE_APIS?: Record<string, GenTreeSourceApiLoader>
}

const domainBlockModules = import.meta.glob<DomainGenBlocksModule>(
  '@/domains/**/gen-blocks.ts',
  { eager: true },
)

function registerFromModule(mod: DomainGenBlocksModule) {
  if (mod.GEN_DIALOG_WIDGETS) {
    for (const [id, component] of Object.entries(mod.GEN_DIALOG_WIDGETS)) {
      registerDialogWidget(id, component)
    }
  }
  if (mod.GEN_ROW_ACTION_HANDLERS) {
    for (const [id, handler] of Object.entries(mod.GEN_ROW_ACTION_HANDLERS)) {
      registerRowActionHandler(id, handler)
    }
  }
  if (mod.GEN_TREE_SOURCE_APIS) {
    for (const [id, loader] of Object.entries(mod.GEN_TREE_SOURCE_APIS)) {
      registerTreeSourceApi(id, loader)
    }
  }
}

let registered = false

/** glob 发现并注册全部 domains/**\/gen-blocks.ts */
export function registerAllDomainGenBlocks() {
  if (registered) return
  registered = true
  for (const mod of Object.values(domainBlockModules)) {
    registerFromModule(mod)
  }
}

export function collectDomainTreeSourceApiKeys(): string[] {
  const keys = new Set<string>()
  for (const mod of Object.values(domainBlockModules)) {
    if (mod.GEN_TREE_SOURCE_APIS) {
      for (const key of Object.keys(mod.GEN_TREE_SOURCE_APIS)) {
        keys.add(key)
      }
    }
  }
  return [...keys].sort()
}

export function resetDomainGenBlocksRegistrationForTests() {
  registered = false
}
