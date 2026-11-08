/**
 * Sync FE Git i18n baselines → BE classpath mirrors (docs/i18n-standard.md).
 * Source of truth: frontend/src/i18n/{shell,platform,gen}/
 */
const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')

function copyDir(src, dst) {
  fs.mkdirSync(dst, { recursive: true })
  for (const name of fs.readdirSync(src)) {
    const s = path.join(src, name)
    const d = path.join(dst, name)
    if (fs.statSync(s).isDirectory()) copyDir(s, d)
    else fs.copyFileSync(s, d)
  }
}

function findBeGenDirs(businessName) {
  const hits = []
  function walk(dir) {
    if (!fs.existsSync(dir)) return
    for (const ent of fs.readdirSync(dir, { withFileTypes: true })) {
      if (!ent.isDirectory()) continue
      if (ent.name === 'target' || ent.name === 'node_modules') continue
      const p = path.join(dir, ent.name)
      if (ent.name === 'gen') {
        const candidate = path.join(p, businessName)
        if (fs.existsSync(path.join(candidate, 'zh-CN.json'))) hits.push(candidate)
      } else {
        walk(p)
      }
    }
  }
  walk(path.join(root, 'backend'))
  return hits
}

const fixedPairs = [
  ['frontend/src/i18n/shell/common', 'backend/icoolkj-admin-system/src/main/resources/i18n/shell/common'],
  ['frontend/src/i18n/shell/layout', 'backend/icoolkj-admin-system/src/main/resources/i18n/shell/layout'],
  ['frontend/src/i18n/shell/login', 'backend/icoolkj-admin-system/src/main/resources/i18n/shell/login'],
  ['frontend/src/i18n/platform', 'backend/icoolkj-admin-system/src/main/resources/i18n/platform'],
]

let synced = 0
let skipped = 0

for (const [a, b] of fixedPairs) {
  const src = path.join(root, a)
  const dst = path.join(root, b)
  if (!fs.existsSync(src)) {
    console.log('skip missing', a)
    skipped++
    continue
  }
  copyDir(src, dst)
  console.log('synced', a, '->', b)
  synced++
}

const feGenRoot = path.join(root, 'frontend/src/i18n/gen')
const packs = fs.readdirSync(feGenRoot).filter((n) => fs.statSync(path.join(feGenRoot, n)).isDirectory())
const feOnly = []

for (const name of packs) {
  const src = path.join(feGenRoot, name)
  const hits = findBeGenDirs(name)
  if (!hits.length) {
    feOnly.push(name)
    continue
  }
  for (const dst of hits) {
    copyDir(src, dst)
    console.log('synced', path.relative(root, src), '->', path.relative(root, dst))
    synced++
  }
}

if (feOnly.length) {
  console.log('fe-only (no BE mirror):', feOnly.join(', '))
}
console.log(`done: synced=${synced} feOnly=${feOnly.length}`)
