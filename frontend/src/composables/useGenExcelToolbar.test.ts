import { describe, expect, it } from 'vitest'
import { createApp, ref } from 'vue'
import { createI18n } from 'vue-i18n'
import { toExcelExportQuery, useGenExcelToolbar } from '@/composables/useGenExcelToolbar'
import type { GenExcelToolbarOptions } from '@/composables/useGenExcelToolbar'

function withSetup<T>(factory: () => T): T {
  let result!: T
  const i18n = createI18n({ legacy: false, locale: 'zh-CN', messages: { 'zh-CN': {} } })
  const app = createApp({
    setup() {
      result = factory()
      return () => null
    },
  })
  app.use(i18n)
  app.mount(document.createElement('div'))
  app.unmount()
  return result
}

describe('toExcelExportQuery', () => {
  it('strips pagination', () => {
    expect(toExcelExportQuery({ pageNo: 1, pageSize: 10, name: 'a' })).toEqual({ name: 'a' })
  })
})

describe('useGenExcelToolbar visibility', () => {
  it('hides export/import when perm codes are missing', () => {
    const options: GenExcelToolbarOptions = {
      apiModule: 'system',
      businessName: 'user',
      api: { page: async () => ({}) },
      perm: {},
      readOnly: () => false,
      exportQuery: () => ({}),
      toolbarActions: () => [
        { id: 'export', builtin: 'export' },
        { id: 'import', builtin: 'import' },
      ],
    }
    const { declaredActions, toolbarActionVisible } = withSetup(() => useGenExcelToolbar(options))
    expect(declaredActions.value).toHaveLength(2)
    expect(toolbarActionVisible(declaredActions.value[0]!)).toBe(false)
    expect(toolbarActionVisible(declaredActions.value[1]!)).toBe(false)
  })

  it('hides import on readOnly unless allowOnReadOnly', () => {
    const options: GenExcelToolbarOptions = {
      apiModule: 'system',
      businessName: 'user',
      api: { page: async () => ({}) },
      perm: { export: 'system:user:export', import: 'system:user:import' },
      readOnly: () => true,
      exportQuery: ref({}),
      toolbarActions: () => [
        { id: 'export', builtin: 'export' },
        { id: 'import', builtin: 'import' },
        { id: 'import2', builtin: 'import', allowOnReadOnly: true },
      ],
    }
    const { declaredActions, toolbarActionVisible } = withSetup(() => useGenExcelToolbar(options))
    expect(toolbarActionVisible(declaredActions.value[0]!)).toBe(true)
    expect(toolbarActionVisible(declaredActions.value[1]!)).toBe(false)
    expect(toolbarActionVisible(declaredActions.value[2]!)).toBe(true)
  })
})
