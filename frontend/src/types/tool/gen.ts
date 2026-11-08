export interface GenTableColumn {
  id?: string
  tableId?: string
  columnName: string
  columnComment?: string
  columnType?: string
  javaType?: string
  javaField?: string
  isPk?: string
  isInsert?: string
  isEdit?: string
  isList?: string
  isExport?: string
  isImport?: string
  isQuery?: string
  queryType?: string
  htmlType?: string
  dictType?: string
  isRequired?: string
  columnLength?: number
  sort?: number
}

export interface GenTable {
  id?: string
  tableName: string
  tableComment?: string
  className?: string
  tplCategory?: string
  tplWebType?: string
  backendModule?: string
  apiModule?: string
  baseType?: string
  businessName?: string
  functionName?: string
  functionAuthor?: string
  routePrefix?: string
  permissionPrefix?: string
  genType?: string
  genPath?: string
  options?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
  columns?: GenTableColumn[]
}

export interface GenTablePageQuery {
  pageNo?: number
  pageSize?: number
  tableName?: string
  tableComment?: string
}
