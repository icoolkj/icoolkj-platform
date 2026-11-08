import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type { GenTable, GenTablePageQuery } from '@/types/tool/gen'

const BASE = '/tool/page-spec'

export interface GenPageSpecGenerateRequest {
  tableId?: string
  pageKey: string
  layout: string
  userPrompt?: string
  columnsText?: string
}

export interface GenPageSpecFieldDraft {
  javaField: string
  columnComment?: string
  isQuery?: boolean
  queryType?: string
  isList?: boolean
  isExport?: boolean
  isImport?: boolean
  isInsert?: boolean
  isEdit?: boolean
  htmlType?: string
  dictType?: string
}

export interface GenPageSpecDraft {
  specVersion?: string
  pageKey: string
  layout: string
  summary?: string
  meta?: {
    apiModule?: string
    backendModule?: string
    permissionPrefix?: string
    className?: string
  }
  entity?: {
    tableName?: string
    businessName?: string
    functionName?: string
  }
  fields: GenPageSpecFieldDraft[]
  /** Compiled into view.json.extensions (rowActions / dialogs) */
  extensions?: {
    rowActions?: Array<string | Record<string, unknown>>
    dialogs?: string[]
    treeSources?: Record<string, unknown>
    [key: string]: unknown
  }
}

export interface GenPageSpecCompileResult {
  tableName: string
  pageKey: string
  tableId: string
  warnings?: string[]
}

export function getPageSpecDraft(tableId: string, pageKey?: string, layout?: string) {
  return request.get<ApiResponse<GenPageSpecDraft>>(`${BASE}/draft`, {
    params: { tableId, pageKey, layout },
  })
}

export function analyzePageSpec(data: GenPageSpecGenerateRequest) {
  return request.post<ApiResponse<GenPageSpecDraft>>(`${BASE}/analyze`, data)
}

export function publishPageSpec(data: { tableId?: string; draft: GenPageSpecDraft }) {
  return request.post<ApiResponse<GenPageSpecCompileResult>>(`${BASE}/publish`, data)
}

// legacy YAML APIs (advanced)
export interface GenPageSpecValidateRequest {
  yamlContent: string
  tableId?: string
}

export interface GenPageSpecValidateResult {
  valid: boolean
  errors: string[]
  warnings: string[]
  normalizedYaml?: string
}

export function generatePageSpec(data: GenPageSpecGenerateRequest) {
  return request.post<ApiResponse<string>>(`${BASE}/generate`, data)
}

export function validatePageSpec(data: GenPageSpecValidateRequest) {
  return request.post<ApiResponse<GenPageSpecValidateResult>>(`${BASE}/validate`, data)
}

export function compilePageSpec(data: GenPageSpecValidateRequest) {
  return request.post<ApiResponse<GenPageSpecCompileResult>>(`${BASE}/compile`, data)
}

export interface GenTableDesignColumn {
  columnName: string
  columnComment?: string
  columnType?: string
  required?: boolean
  dictType?: string
}

export interface GenTableDesign {
  tableName: string
  tableComment?: string
  apiModule?: string
  backendModule?: string
  baseType?: string
  routePrefix?: string
  summary?: string
  ddlPreview?: string
  columns: GenTableDesignColumn[]
}

export interface GenModuleOption {
  apiModule: string
  labelKey: string
  tablePrefix: string
  backendModule: string
  baseType: string
  routePrefix: string
  includeTenantColumn: boolean
}

export interface GenTablePrepareResult {
  tableId: string
  tableName: string
  pageKey: string
  action: 'imported' | 'existed' | 'created' | 'altered'
}

export interface GenTableDdlDesign {
  summary?: string
  ddl: string
  ddlRecordId?: string
  invokeId?: string
}

export interface GenTableDdlExecuteResult {
  tableNames: string[]
  tables: GenTablePrepareResult[]
}

export interface GenTableSchemaColumn {
  columnName: string
  columnComment?: string
  columnType?: string
  required?: boolean
  systemColumn?: boolean
}

export interface GenTableSchema {
  tableName: string
  tableComment?: string
  columns: GenTableSchemaColumn[]
}

export interface GenTableAlterModifyColumn {
  columnName: string
  columnComment?: string
  columnType?: string
  required?: boolean
}

export interface GenTableAlterDesign {
  tableName: string
  summary?: string
  ddlPreview?: string
  addColumns: GenTableDesignColumn[]
  modifyColumns: GenTableAlterModifyColumn[]
}

export function getPageSpecTableSchema(tableName: string) {
  return request.get<ApiResponse<GenTableSchema>>(`${BASE}/table-schema`, { params: { tableName } })
}

export function designAlterPageSpecTable(data: { tableName: string; userPrompt: string }) {
  return request.post<ApiResponse<GenTableAlterDesign>>(`${BASE}/design-alter-table`, data)
}

export function applyAlterPageSpecTable(design: GenTableAlterDesign) {
  return request.post<ApiResponse<GenTablePrepareResult>>(`${BASE}/apply-alter-table`, { design })
}

export function pageSpecDbTables(params: GenTablePageQuery) {
  return request.get<ApiResponse<PageResult<GenTable>>>(`${BASE}/db-tables`, { params })
}

export function listPageSpecModules() {
  return request.get<ApiResponse<GenModuleOption[]>>(`${BASE}/modules`)
}

export function preparePageSpecTable(tableName: string) {
  return request.post<ApiResponse<GenTablePrepareResult>>(`${BASE}/prepare-table`, { tableName })
}

export function designCreateDdlPageSpecTable(data: { userPrompt: string; apiModule?: string }) {
  return request.post<ApiResponse<GenTableDdlDesign>>(`${BASE}/design-create-ddl`, data)
}

export function designAlterDdlPageSpecTable(data: { tableName: string; userPrompt: string }) {
  return request.post<ApiResponse<GenTableDdlDesign>>(`${BASE}/design-alter-ddl`, data)
}

export function executePageSpecDdl(ddl: string) {
  return request.post<ApiResponse<GenTableDdlExecuteResult>>(`${BASE}/execute-ddl`, { ddl })
}

export function designPageSpecTable(data: { userPrompt: string; apiModule?: string }) {
  return request.post<ApiResponse<GenTableDesign>>(`${BASE}/design-table`, data)
}

export function createPageSpecTable(design: GenTableDesign) {
  return request.post<ApiResponse<GenTablePrepareResult>>(`${BASE}/create-table`, { design })
}

export function previewCreatePageSpecTable(design: GenTableDesign) {
  return request.post<ApiResponse<string>>(`${BASE}/preview-create-table`, { design })
}

export function previewAlterPageSpecTable(design: GenTableAlterDesign) {
  return request.post<ApiResponse<string>>(`${BASE}/preview-alter-table`, { design })
}
