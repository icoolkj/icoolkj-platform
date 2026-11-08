import type { ExcelImportResult } from '@/types/common'

/** Permission codes keyed for Gen list pages (add/update/delete/export/import/…). */
export interface GenCrudPerm {
  [key: string]: string | undefined
  add?: string
  update?: string
  delete?: string
  export?: string
  import?: string
}

/** CRUD API surface consumed by Gen list layouts + Excel toolbar. */
export type GenCrudApi = {
  // loosely typed: Gen pages bind domain-specific API functions
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  page: (params: any) => Promise<unknown>
  get?: (id: string) => Promise<unknown>
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  create?: (data: any) => Promise<unknown>
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  update?: (data: any) => Promise<unknown>
  delete?: (id: string) => Promise<unknown>
  /** Batch delete when extensions.toolbarActions includes builtin batchDelete */
  batchDelete?: (data: { ids: string[] }) => Promise<unknown>
  /** Optional absolute export path; default `/{apiModule}/{businessName}/export` */
  exportPath?: string
  /** Optional import upload path (used when importFile is omitted) */
  importPath?: string
  /** Optional import-template download path */
  importTemplatePath?: string
  /**
   * Optional async import: submit file, return task id for polling {@code /infra/asyncTask/mine/{id}}.
   * When set, {@link GenExcelImportDialog} prefers this over sync {@link importFile}.
   * On {@code error.async.disabled}, Dialog falls back to {@link importFile} / {@link importPath}.
   */
  importFileAsync?: (file: File) => Promise<{ taskId: string }>
  /**
   * Sync import fallback ({@code POST /import}). Prefer {@link importFileAsync}; retained for scripts
   * and when async is disabled.
   */
  importFile?: (file: File) => Promise<ExcelImportResult | { data: ExcelImportResult } | unknown>
  /**
   * Optional injectable sync export. Used when neither action.exportPath nor api.exportPath is set
   * and {@link exportFileAsync} is absent.
   */
  exportFile?: (query: Record<string, unknown>) => Promise<void>
  /**
   * Preferred async export: submit query → poll {@code /infra/asyncTask/mine/{id}} → download fileId.
   */
  exportFileAsync?: (query: Record<string, unknown>) => Promise<{ taskId: string }>
}

/** Master/detail API pair for {@link GenMasterDetailPage}. */
export type GenMasterDetailApi = {
  master: GenCrudApi
  detail: GenCrudApi
}
