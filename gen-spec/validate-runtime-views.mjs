#!/usr/bin/env node
/**
 * Validate all runtime Gen view.json under frontend/src/i18n/gen/
 * against gen-view.schema.json + runtime registries:
 *   - dialog widgets / row action handlers / tree sources discovered from
 *     domains/**\/gen-blocks.ts (same source the runtime uses)
 *   - field widgets discovered from field-widgets/registerCore.ts
 *   - GEN_RESOURCE_KEY discovered from api/**\/*.ts
 *
 * Usage (from repo root):
 *   node gen-spec/validate-runtime-views.mjs
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import Ajv2020 from 'ajv/dist/2020.js'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const REPO_ROOT = path.resolve(__dirname, '..')
const GEN_I18N_ROOT = path.join(REPO_ROOT, 'frontend', 'src', 'i18n', 'gen')
const SCHEMA_PATH = path.join(__dirname, 'schemas', 'gen-view.schema.json')

const API_GEN_ROOT = path.join(REPO_ROOT, 'frontend', 'src', 'api')
const DOMAINS_ROOT = path.join(REPO_ROOT, 'frontend', 'src', 'domains')
const VIEWS_ROOT = path.join(REPO_ROOT, 'frontend', 'src', 'views')
const FIELD_WIDGETS_FILE = path.join(
  REPO_ROOT,
  'frontend',
  'src',
  'components',
  'gen',
  'registry',
  'field-widgets',
  'registerCore.ts',
)

function walkFiles(root, predicate) {
  const results = []
  if (!fs.existsSync(root)) return results
  const stack = [root]
  while (stack.length) {
    const dir = stack.pop()
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name)
      if (entry.isDirectory()) {
        stack.push(full)
      } else if (predicate(full)) {
        results.push(full)
      }
    }
  }
  return results.sort()
}

/**
 * Extract object keys from `export const NAME = { ... }` in a TS module.
 * Supports: quoted keys ('a.b': ...), identifier keys (foo: async ...),
 * and shorthand properties (Foo,).
 */
function extractObjectLiteralKeys(text, exportName) {
  const keys = new Set()
  const re = new RegExp(`export const ${exportName}\\b[^=]*=\\s*\\{`)
  const m = re.exec(text)
  if (!m) return keys
  let depth = 1
  let i = m.index + m[0].length
  for (; i < text.length && depth > 0; i++) {
    if (text[i] === '{') depth++
    else if (text[i] === '}') depth--
  }
  const body = text.slice(m.index + m[0].length, i - 1)
  for (const line of body.split('\n')) {
    const t = line.trim()
    let km = t.match(/^['"]([^'"]+)['"]\s*:/)
    if (km) {
      keys.add(km[1])
      continue
    }
    km = t.match(/^([A-Za-z_$][\w$]*)\s*:\s*async\b/)
    if (km) {
      keys.add(km[1])
      continue
    }
    km = t.match(/^([A-Z][A-Za-z0-9_]*)\s*,?\s*(?:\/\/.*)?$/)
    if (km) keys.add(km[1])
  }
  return keys
}

/** Dialog widgets / row action handlers / tree sources the runtime will have. */
function discoverDomainRegistrations() {
  const dialogWidgets = new Set(['GenDetailDialog'])
  const rowHandlers = new Set()
  const treeSources = new Set()
  for (const file of walkFiles(DOMAINS_ROOT, (f) => f.endsWith('gen-blocks.ts'))) {
    const text = fs.readFileSync(file, 'utf8')
    for (const id of extractObjectLiteralKeys(text, 'GEN_DIALOG_WIDGETS')) dialogWidgets.add(id)
    for (const id of extractObjectLiteralKeys(text, 'GEN_ROW_ACTION_HANDLERS')) rowHandlers.add(id)
    for (const id of extractObjectLiteralKeys(text, 'GEN_TREE_SOURCE_APIS')) treeSources.add(id)
  }
  return { dialogWidgets, rowHandlers, treeSources }
}

/** Field widgets the runtime will have (legacy builtins + ctx widgets). */
function discoverFieldWidgets() {
  const keys = new Set()
  if (!fs.existsSync(FIELD_WIDGETS_FILE)) return keys
  const text = fs.readFileSync(FIELD_WIDGETS_FILE, 'utf8')
  const arr = text.match(/LEGACY_BUILTIN_IDS\s*=\s*\[([\s\S]*?)\]/)
  if (arr) {
    for (const m of arr[1].matchAll(/['"]([^'"]+)['"]/g)) keys.add(m[1])
  }
  for (const m of text.matchAll(/registerCtxWidget\(\s*['"]([^'"]+)['"]/g)) {
    keys.add(m[1])
  }
  return keys
}

/**
 * Page-local row action handler ids declared in views via `actionHandlers`
 * (L4 extension). The runtime resolves `options.actionHandlers[id]` before the
 * global registry, so these ids are valid without a gen-blocks registration.
 */
function discoverLocalActionHandlerIds() {
  const ids = new Set()
  for (const file of walkFiles(VIEWS_ROOT, (f) => /\.(vue|ts)$/.test(f))) {
    const text = fs.readFileSync(file, 'utf8')
    for (const m of text.matchAll(/['"]([\w.]+)['"]\s*:/g)) {
      if (m[1].includes('.')) ids.add(m[1])
    }
  }
  return ids
}

// 从 api 目录扫描 export const GEN_RESOURCE_KEY
function discoverPlatformCrudResourceKeys(root) {
  const keys = new Set()
  if (!fs.existsSync(root)) return keys

  function walk(dir) {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name)
      if (entry.isDirectory()) {
        walk(full)
        continue
      }
      if (!entry.name.endsWith('.ts') || entry.name.endsWith('.gen.ts')) continue
      const text = fs.readFileSync(full, 'utf8')
      const match = text.match(/export const GEN_RESOURCE_KEY = ['"]([^'"]+)['"]/)
      if (match) keys.add(match[1])
      const multi = text.match(/export const GEN_CRUD_RESOURCES[\s\S]*?=\s*\{([\s\S]*?)\n\}/)
      if (multi) {
        for (const km of multi[1].matchAll(/['"]([^'"]+)['"]\s*:/g)) {
          keys.add(km[1])
        }
      }
    }
  }

  walk(root)
  return keys
}

const PLATFORM_CRUD_RESOURCE_KEYS = discoverPlatformCrudResourceKeys(API_GEN_ROOT)

const ajv = new Ajv2020({ allErrors: true, strict: false })
const schema = JSON.parse(fs.readFileSync(SCHEMA_PATH, 'utf8'))
const validateView = ajv.compile(schema)

const registry = discoverDomainRegistrations()
const fieldWidgets = discoverFieldWidgets()
const localHandlerIds = discoverLocalActionHandlerIds()

const errors = []
const warnings = []

function fail(msg) {
  errors.push(msg)
}

function warn(msg) {
  warnings.push(msg)
}

function readJson(file) {
  return JSON.parse(fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''))
}

function discoverViewFiles(root) {
  const results = []
  if (!fs.existsSync(root)) return results
  for (const entry of fs.readdirSync(root, { withFileTypes: true })) {
    if (!entry.isDirectory()) continue
    const viewPath = path.join(root, entry.name, 'view.json')
    if (fs.existsSync(viewPath)) results.push(viewPath)
  }
  return results.sort()
}

function validateViewFile(viewPath) {
  const rel = path.relative(REPO_ROOT, viewPath)
  let view
  try {
    view = readJson(viewPath)
  } catch (e) {
    fail(`${rel}: invalid JSON — ${e.message}`)
    return
  }

  if (!validateView(view)) {
    for (const e of validateView.errors ?? []) {
      const p = e.instancePath || '/'
      fail(`${rel}: ${p} ${e.message ?? ''}`.trim())
    }
  }

  for (const id of view.extensions?.dialogs ?? []) {
    if (!registry.dialogWidgets.has(id)) {
      fail(`${rel}: unregistered dialog widget "${id}"`)
    }
  }

  for (const action of view.extensions?.rowActions ?? []) {
    if (action.dialog && !registry.dialogWidgets.has(action.dialog)) {
      fail(`${rel}: unregistered rowAction.dialog "${action.dialog}"`)
    }
    if (
      action.handler &&
      !registry.rowHandlers.has(action.handler) &&
      !localHandlerIds.has(action.handler)
    ) {
      fail(`${rel}: unregistered rowAction.handler "${action.handler}"`)
    }
  }

  for (const [key, config] of Object.entries(view.extensions?.treeSources ?? {})) {
    if (!config?.apiKey) {
      fail(`${rel}: extensions.treeSources.${key} missing apiKey`)
      continue
    }
    if (!registry.treeSources.has(config.apiKey)) {
      fail(`${rel}: unregistered treeSources apiKey "${config.apiKey}" (key=${key})`)
    }
  }

  for (const [section, fields] of [
    ['query', view.query?.fields],
    ['form', view.form?.fields],
    ['masterDetail.master.query', view.masterDetail?.master?.query?.fields],
    ['masterDetail.master.form', view.masterDetail?.master?.form?.fields],
    ['masterDetail.detail.form', view.masterDetail?.detail?.form?.fields],
  ]) {
    for (const field of fields ?? []) {
      if (field?.component && !fieldWidgets.has(field.component)) {
        fail(
          `${rel}: ${section} field "${field.field}" uses unregistered component "${field.component}"`,
        )
      }
    }
  }

  for (const action of view.extensions?.toolbarActions ?? []) {
    if (action.builtin === 'batchDelete') {
      // informational only — api.batchDelete wired via crudResourceRegistry
    }
  }

  const resourceKey = view.meta?.resourceKey ?? `${view.meta?.apiModule}.${view.meta?.businessName}`
  if (view.meta?.apiModule && view.meta?.businessName && !PLATFORM_CRUD_RESOURCE_KEYS.has(resourceKey)) {
    warn(
      `${rel}: unregistered GEN_RESOURCE_KEY "${resourceKey}" (add genCrudResource to api/{module}/${resourceKey.split('.').pop()}.ts)`,
    )
  }
}

function main() {
  const viewFiles = discoverViewFiles(GEN_I18N_ROOT)
  if (viewFiles.length === 0) {
    console.error('No view.json files found under frontend/src/i18n/gen/')
    process.exit(1)
  }

  console.log(`Validating ${viewFiles.length} runtime Gen view.json file(s)...`)
  console.log(
    `  registry: dialogs=${registry.dialogWidgets.size} handlers=${registry.rowHandlers.size} ` +
      `treeSources=${registry.treeSources.size} fieldWidgets=${fieldWidgets.size} ` +
      `localHandlers=${localHandlerIds.size}`,
  )
  for (const file of viewFiles) {
    validateViewFile(file)
  }

  if (warnings.length) {
    console.log('\nWarnings:')
    warnings.forEach((w) => console.log(`  ⚠ ${w}`))
  }

  if (errors.length) {
    console.error('\nErrors:')
    errors.forEach((e) => console.error(`  ✗ ${e}`))
    process.exit(1)
  }

  console.log(`\n✅ All ${viewFiles.length} runtime view.json file(s) passed.`)
}

main()
