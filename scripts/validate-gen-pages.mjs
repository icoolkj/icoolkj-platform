#!/usr/bin/env node
/** @deprecated Use `node gen-spec/validate.mjs` — thin wrapper for CI / old docs. */
import { spawnSync } from 'node:child_process'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const validate = path.join(__dirname, '..', 'gen-spec', 'validate.mjs')
const result = spawnSync(process.execPath, [validate, ...process.argv.slice(2)], {
  stdio: 'inherit',
})
process.exit(result.status ?? 1)
