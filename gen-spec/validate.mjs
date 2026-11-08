#!/usr/bin/env node
/**
 * Validate gen page golden examples: page-spec.yaml + expected/*.json
 *
 * Usage:
 *   node gen-spec/validate.mjs [gen-spec/golden/user ...]
 *
 * First run: cd gen-spec && npm install
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import YAML from 'yaml'
import Ajv2020 from 'ajv/dist/2020.js'
import addFormats from 'ajv-formats'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const GEN_SPEC_ROOT = __dirname
const REPO_ROOT = path.resolve(GEN_SPEC_ROOT, '..')
const SCHEMA_DIR = path.join(GEN_SPEC_ROOT, 'schemas')
const DEFAULT_GOLDEN_ROOT = path.join(GEN_SPEC_ROOT, 'golden')

const ajv = new Ajv2020({ allErrors: true, strict: false })
addFormats(ajv)

function loadSchema(name) {
  const file = path.join(SCHEMA_DIR, name)
  return JSON.parse(fs.readFileSync(file, 'utf8'))
}

const validatePageSpec = ajv.compile(loadSchema('gen-page-spec.schema.json'))
const validateView = ajv.compile(loadSchema('gen-view.schema.json'))
const validateI18n = ajv.compile(loadSchema('gen-i18n.schema.json'))

const errors = []
const warnings = []

function fail(msg) {
  errors.push(msg)
}

function warn(msg) {
  warnings.push(msg)
}

function readJson(file) {
  const raw = fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, '')
  return JSON.parse(raw)
}

function readYaml(file) {
  return YAML.parse(fs.readFileSync(file, 'utf8'))
}

function formatAjvErrors(validator, label) {
  return (validator.errors ?? []).map((e) => {
    const p = e.instancePath || '/'
    return `${label}: ${p} ${e.message ?? ''}`.trim()
  })
}

/** Collect all `field` values from view.json panels */
function collectViewFields(view) {
  const fields = new Set()

  function addFromQuery(q) {
    q?.fields?.forEach((f) => fields.add(f.field))
  }
  function addFromTable(t) {
    t?.fields?.forEach((f) => fields.add(f.field))
  }
  function addFromForm(fm) {
    fm?.fields?.forEach((f) => fields.add(f.field))
  }

  addFromQuery(view.query)
  addFromTable(view.table)
  addFromForm(view.form)

  if (view.masterDetail) {
    const { master, detail } = view.masterDetail
    addFromQuery(master?.query)
    addFromTable(master?.table)
    addFromForm(master?.form)
    addFromTable(detail?.table)
    addFromForm(detail?.form)
  }

  return fields
}

function collectSpecFields(spec) {
  const fields = new Set()
  if (spec.fields) {
    spec.fields.forEach((f) => fields.add(f.javaField))
  }
  if (spec.masterDetail) {
    spec.masterDetail.master?.fields?.forEach((f) => fields.add(f.javaField))
    spec.masterDetail.detail?.fields?.forEach((f) => fields.add(f.javaField))
  }
  return fields
}

function validateGoldenDir(goldenDir) {
  const pageKey = path.basename(goldenDir)
  const rel = path.relative(REPO_ROOT, goldenDir)
  console.log(`\n▶ ${rel}`)

  const specPath = path.join(goldenDir, 'page-spec.yaml')
  const expectedDir = path.join(goldenDir, 'expected')
  const viewPath = path.join(expectedDir, 'view.json')
  const zhPath = path.join(expectedDir, 'zh-CN.json')
  const enPath = path.join(expectedDir, 'en-US.json')

  for (const f of [specPath, viewPath, zhPath, enPath]) {
    if (!fs.existsSync(f)) {
      fail(`${rel}: missing ${path.basename(f)}`)
      return
    }
  }

  let spec
  try {
    spec = readYaml(specPath)
  } catch (e) {
    fail(`${rel}: invalid YAML — ${e.message}`)
    return
  }

  if (!validatePageSpec(spec)) {
    formatAjvErrors(validatePageSpec, `${rel}/page-spec.yaml`).forEach(fail)
  }

  if (spec.pageKey !== pageKey) {
    fail(`${rel}: pageKey "${spec.pageKey}" !== directory name "${pageKey}"`)
  }

  const view = readJson(viewPath)
  const zh = readJson(zhPath)
  const en = readJson(enPath)

  if (!validateView(view)) {
    formatAjvErrors(validateView, `${rel}/expected/view.json`).forEach(fail)
  }
  if (!validateI18n(zh)) {
    formatAjvErrors(validateI18n, `${rel}/expected/zh-CN.json`).forEach(fail)
  }
  if (!validateI18n(en)) {
    formatAjvErrors(validateI18n, `${rel}/expected/en-US.json`).forEach(fail)
  }

  // pageKey ↔ view.meta.businessName
  if (view.meta?.businessName !== spec.pageKey) {
    fail(
      `${rel}: view.meta.businessName "${view.meta?.businessName}" !== pageKey "${spec.pageKey}"`,
    )
  }

  // layout alignment
  const viewLayout =
    view.page?.layout ?? (view.masterDetail ? 'master-detail' : undefined)
  if (viewLayout && viewLayout !== spec.layout) {
    fail(`${rel}: view layout "${viewLayout}" !== spec.layout "${spec.layout}"`)
  }

  // i18n meta.businessName (warn — dict legacy dictType)
  for (const [locale, i18n] of [
    ['zh-CN', zh],
    ['en-US', en],
  ]) {
    const bn = i18n.meta?.businessName
    if (bn && bn !== spec.pageKey) {
      warn(
        `${rel}/expected/${locale}.json: meta.businessName "${bn}" !== pageKey "${spec.pageKey}" (legacy; align when publishing)`,
      )
    }
  }

  // spec fields ⊆ view fields
  const specFields = collectSpecFields(spec)
  const viewFields = collectViewFields(view)
  for (const f of specFields) {
    if (!viewFields.has(f)) {
      fail(`${rel}: page-spec field "${f}" not found in expected/view.json`)
    }
  }

  // view fields should appear in i18n.fields (warn for audit-only columns)
  const i18nFieldKeys = new Set([
    ...Object.keys(zh.fields ?? {}),
    ...Object.keys(en.fields ?? {}),
  ])
  for (const f of viewFields) {
    if (!i18nFieldKeys.has(f)) {
      warn(`${rel}: view field "${f}" missing from i18n fields (zh/en)`)
    }
  }

  // master-detail foreign key: must match view.masterDetail.detailForeignKey
  // (FK is injected from master selection; often not listed as a table/form column)
  if (spec.layout === 'master-detail') {
    const fk = spec.masterDetail?.detailForeignKey
    if (fk) {
      const viewFk = view.masterDetail?.detailForeignKey
      if (viewFk !== fk) {
        fail(
          `${rel}: detailForeignKey "${fk}" !== view.masterDetail.detailForeignKey "${viewFk ?? ''}"`,
        )
      } else if (!viewFields.has(fk)) {
        warn(
          `${rel}: detailForeignKey "${fk}" not listed as a view column (ok if injected from master)`,
        )
      }
    }
  }

  // optional: compare with runtime Git copy
  const runtimeDir = path.join(REPO_ROOT, 'frontend', 'src', 'i18n', 'gen', pageKey)
  if (fs.existsSync(runtimeDir)) {
    for (const name of ['view.json', 'zh-CN.json', 'en-US.json']) {
      const expected = readJson(path.join(expectedDir, name))
      const runtime = readJson(path.join(runtimeDir, name))
      if (JSON.stringify(expected) !== JSON.stringify(runtime)) {
        warn(
          `${rel}/expected/${name} differs from frontend/src/i18n/gen/${pageKey}/${name} — refresh golden or sync runtime`,
        )
      }
    }
  }

  console.log(`  ✓ page-spec + expected JSON validated`)
}

function discoverGoldenDirs(root) {
  if (!fs.existsSync(root)) return []
  return fs
    .readdirSync(root, { withFileTypes: true })
    .filter((d) => d.isDirectory())
    .map((d) => path.join(root, d.name))
    .filter((dir) => fs.existsSync(path.join(dir, 'page-spec.yaml')))
}

function main() {
  const args = process.argv.slice(2)
  const targets =
    args.length > 0
      ? args.map((a) => path.resolve(REPO_ROOT, a))
      : discoverGoldenDirs(DEFAULT_GOLDEN_ROOT)

  if (targets.length === 0) {
    console.error('No golden examples found.')
    process.exit(1)
  }

  console.log(`Validating ${targets.length} golden page(s)...`)

  for (const dir of targets) {
    validateGoldenDir(dir)
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

  console.log(`\n✅ All ${targets.length} golden page(s) passed.`)
  if (warnings.length) {
    console.log(`   (${warnings.length} warning(s))`)
  }
}

main()
