/**
 * 流程设计器消费的表单字段摘要（与业务侧 OaFieldDefVO 结构兼容，避免设计器依赖 OA 类型）。
 */
export interface WorkflowFormFieldDef {
  fieldCode: string
  fieldName: string
  fieldType?: string
  componentType?: string
  dictType?: string
}
