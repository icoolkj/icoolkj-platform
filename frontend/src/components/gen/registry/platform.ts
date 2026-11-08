/**
 * Gen 平台插件一次注册 — 业务启动 / 运行时唯一入口。
 *
 * 合并原 crud-resources + extension-blocks + tree-sources 的 platform 注册，
 * 避免 GenRuntimePage / useGenCrudPage 多处重复调用。
 */
import { registerPlatformCrudResources } from './crud-resources/registerAll'
import { registerPlatformGenBlocks } from './extension-blocks/registerPlatform'
import { registerCoreFieldWidgets } from './field-widgets/registerCore'

let registered = false

/** 注册全部平台级 Gen 插件（幂等） */
export function registerGenPlatform(): void {
  if (registered) return
  registered = true
  registerCoreFieldWidgets()
  registerPlatformCrudResources()
  registerPlatformGenBlocks()
}

/** 测试 / HMR 重置（一般业务勿调） */
export function resetGenPlatformRegistrationForTests(): void {
  registered = false
}
