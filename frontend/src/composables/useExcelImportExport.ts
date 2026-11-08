import type { GenCrudApi, GenCrudPerm } from '@/types/gen/crudApi'
import type { GenViewToolbarAction } from '@/types/tool/genView'

export type ExcelImportExportKitOptions = {
  businessName: string
  apiModule: string
  api: GenCrudApi
  perm: GenCrudPerm
  /** Defaults to both export + import when the corresponding perm / API surface exists */
  builtins?: Array<'export' | 'import'>
}

/**
 * Thin wiring helper for non-Gen pages that mount {@link GenExcelToolbarActions}.
 * Supplies default builtin toolbar actions; page still owns readOnly / query / gp / gv.
 */
export function useExcelImportExport(options: ExcelImportExportKitOptions) {
  const builtins = options.builtins ?? (['export', 'import'] as const)
  const actions: GenViewToolbarAction[] = []
  for (const builtin of builtins) {
    if (
      builtin === 'export' &&
      (options.perm.export || options.api.exportPath || options.api.exportFile || options.api.exportFileAsync)
    ) {
      actions.push({ id: 'export', builtin: 'export' })
    }
    if (
      builtin === 'import' &&
      options.perm.import &&
      (options.api.importFileAsync || options.api.importFile || options.api.importPath)
    ) {
      actions.push({ id: 'import', builtin: 'import' })
    }
  }
  return {
    apiModule: options.apiModule,
    businessName: options.businessName,
    api: options.api,
    perm: options.perm,
    actions,
  }
}
