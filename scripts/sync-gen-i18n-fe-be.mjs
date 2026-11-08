const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '../..')

function copyDir(src, dst) {
  fs.mkdirSync(dst, { recursive: true })
  for (const f of fs.readdirSync(src)) {
    const s = path.join(src, f)
    const d = path.join(dst, f)
    if (fs.statSync(s).isDirectory()) copyDir(s, d)
    else fs.copyFileSync(s, d)
  }
}

const pairs = [
  ['frontend/src/i18n/gen/file', 'backend/icoolkj-admin-infra/src/main/resources/i18n/gen/file'],
  ['frontend/src/i18n/gen/noticeApprove', 'backend/icoolkj-admin-system/src/main/resources/i18n/gen/noticeApprove'],
  ['frontend/src/i18n/gen/profile', 'backend/icoolkj-admin-system/src/main/resources/i18n/gen/profile'],
  ['frontend/src/i18n/gen/siteMessage', 'backend/icoolkj-admin-system/src/main/resources/i18n/gen/siteMessage'],
  ['frontend/src/i18n/gen/menu', 'backend/icoolkj-admin-system/src/main/resources/i18n/gen/menu'],
  ['frontend/src/i18n/gen/notice', 'backend/icoolkj-admin-system/src/main/resources/i18n/gen/notice'],
  ['frontend/src/i18n/gen/oaTenantTemplate', 'backend/icoolkj-admin-system/src/main/resources/i18n/gen/oaTenantTemplate'],
  ['frontend/src/i18n/gen/workflowFlowView', 'backend/icoolkj-admin-workflow/src/main/resources/i18n/gen/workflowFlowView'],
  ['frontend/src/i18n/gen/oaWidget', 'backend/icoolkj-admin-system/src/main/resources/i18n/gen/oaWidget'],
  ['frontend/src/i18n/gen/officeApply', 'backend/icoolkj-admin-system/src/main/resources/i18n/gen/officeApply'],
  ['frontend/src/i18n/gen/officeApprove', 'backend/icoolkj-admin-system/src/main/resources/i18n/gen/officeApprove'],
  ['frontend/src/i18n/shell/common', 'backend/icoolkj-admin-system/src/main/resources/i18n/shell/common'],
  ['frontend/src/i18n/platform', 'backend/icoolkj-admin-system/src/main/resources/i18n/platform'],
]

for (const [a, b] of pairs) {
  const src = path.join(root, a)
  const dst = path.join(root, b)
  if (!fs.existsSync(src)) {
    console.log('skip missing', a)
    continue
  }
  copyDir(src, dst)
  console.log('synced', a)
}
