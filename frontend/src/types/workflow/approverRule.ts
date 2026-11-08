export interface WorkflowApproverRuleVO {
  id?: string
  tenantId?: string
  modelId?: string
  modelKey?: string
  modelName?: string
  taskDefinitionKey?: string
  variableName?: string
  strategyType?: string
  strategyConfig?: string
  resolveScope?: string
  sortNo?: number
  status?: string
  remark?: string
}

export interface WorkflowApproverRuleForm {
  id?: string
  modelId: string
  taskDefinitionKey: string
  variableName: string
  strategyType: string
  strategyConfig: string
  resolveScope: string
  sortNo: number
  status?: string
  remark?: string
}

export interface WorkflowApproverRuleUpdateForm {
  id: string
  variableName: string
  strategyType: string
  strategyConfig: string
  resolveScope: string
  sortNo: number
  status?: string
  remark?: string
}

export interface WorkflowApproverRulePageQuery {
  pageNo?: number
  pageSize?: number
  modelId?: string
  modelKey?: string
  taskDefinitionKey?: string
  strategyType?: string
  status?: string
}

export interface WorkflowModelOptionVO {
  value: string
  label: string
  modelKey?: string
  bizType?: string
}
