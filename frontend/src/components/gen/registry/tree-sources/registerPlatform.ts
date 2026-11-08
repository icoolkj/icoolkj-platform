/**
 * @deprecated 树源已并入 domains/**\/gen-blocks.ts；请使用 registerAllDomainGenBlocks。
 */
import { registerAllDomainGenBlocks, collectDomainTreeSourceApiKeys } from '../plugins/registerAll'

export function registerPlatformTreeSources() {
  registerAllDomainGenBlocks()
}

/** 构建期校验用：已 glob 发现的 tree source apiKey */
export const PLATFORM_TREE_SOURCE_API_KEYS = collectDomainTreeSourceApiKeys()
