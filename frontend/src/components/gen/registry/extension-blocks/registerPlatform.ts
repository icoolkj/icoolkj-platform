/**
 * 平台 Gen 插件注册 — 仅内核通用件；业务插件由 domains/**\/gen-blocks.ts glob 发现。
 */
import GenDetailDialog from '@/components/gen/dialogs/GenDetailDialog.vue'
import { registerDialogWidget } from '../extension-blocks/registry'
import { registerAllDomainGenBlocks } from '../plugins/registerAll'

let registered = false

export function registerPlatformGenBlocks() {
  if (registered) return
  registered = true

  registerDialogWidget('GenDetailDialog', GenDetailDialog)
  registerAllDomainGenBlocks()
}

export function resetPlatformGenBlocksRegistrationForTests() {
  registered = false
}
