import type { Router } from 'vue-router'

/** 审批页返回：与 OA / 公告共用，优先办公入口路由名，再回退工作流菜单名 */
function pushNamed(router: Router, primary: string, fallback?: string) {
  return router.push({ name: primary }).catch(() => {
    if (fallback) {
      return router.push({ name: fallback }).catch(() => router.back())
    }
    return router.back()
  })
}

/**
 * @param from route.query.from：todo / done / cc / submitted / draft / notice / mine / monitor
 * @param viewMode 只读详情
 * @param viewFallback 只读时无 from 的默认去向（公告用 done，OA 用 submitted）
 */
export function navigateAfterWorkflowApprove(
  router: Router,
  options: {
    from?: string | null
    viewMode?: boolean
    viewFallback?: 'submitted' | 'done'
  } = {},
) {
  const from = String(options.from ?? '').trim()
  if (from === 'monitor') {
    return pushNamed(router, 'WorkflowMonitor')
  }
  if (from === 'cc') {
    return pushNamed(router, 'OfficeCc', 'WorkflowTaskCc')
  }
  if (from === 'draft') {
    return pushNamed(router, 'OfficeDraft')
  }
  if (from === 'notice') {
    return pushNamed(router, 'SystemNotice')
  }
  if (from === 'submitted' || from === 'mine') {
    return pushNamed(router, 'OfficeSubmitted')
  }
  if (from === 'done') {
    return pushNamed(router, 'OfficeDone', 'WorkflowTaskDone')
  }
  if (options.viewMode) {
    if (options.viewFallback === 'done') {
      return pushNamed(router, 'OfficeDone', 'WorkflowTaskDone')
    }
    return pushNamed(router, 'OfficeSubmitted')
  }
  return pushNamed(router, 'OfficeTodo', 'WorkflowTaskTodo')
}
