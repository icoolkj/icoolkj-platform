/**
 * 附件组件场景选型与联调页。
 * 路径：/dev/attachment-showcase（需登录）
 * 后期可整目录删除，并去掉 router 中的 register 调用。
 */
import type { Router } from 'vue-router'

export const DEV_ATTACHMENT_SHOWCASE_PATH = '/dev/attachment-showcase'

export function registerDevAttachmentShowcaseRoutes(router: Router) {
  if (router.hasRoute('DevAttachmentShowcase')) {
    return
  }
  router.addRoute('Layout', {
    path: 'dev/attachment-showcase',
    name: 'DevAttachmentShowcase',
    component: () => import('./AttachmentShowcasePage.vue'),
    meta: { title: 'Attachment Showcase', hidden: true },
  })
}
