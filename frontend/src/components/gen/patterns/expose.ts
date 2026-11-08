import type { GenPageExpose } from './types'

/** 组装统一 expose：方法恒存在，形态未实现时内部 no-op */
export function createGenPageExpose(source: Partial<GenPageExpose>): GenPageExpose {
  return {
    openForm: (row?: Record<string, unknown>) => source.openForm?.(row),
    openAddChild: (parent: Record<string, unknown>) => source.openAddChild?.(parent),
    loadPage: () => source.loadPage?.(),
    loadMasterPage: () => source.loadMasterPage?.(),
    loadDetailPage: () => source.loadDetailPage?.(),
    rebuildTree: () => source.rebuildTree?.(),
    clearSelection: () => source.clearSelection?.(),
    get form() {
      return source.form
    },
    get queryParams() {
      return source.queryParams
    },
    get selectedRows() {
      return source.selectedRows
    },
  }
}
