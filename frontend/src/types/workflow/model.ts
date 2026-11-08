/** 流程模型类型 */

export interface WorkflowModelVO {
  id?: string
  modelKey?: string
  modelName?: string
  categoryId?: string
  categoryName?: string
  bizType?: string
  formType?: string
  formRoute?: string
  latestVersion?: number
  status?: string
  remark?: string
  processConfig?: string
}

export interface WorkflowModelVersionVO {
  id?: string
  modelId?: string
  versionNo?: number
  deploymentId?: string
  processDefinitionId?: string
  processDefinitionKey?: string
  published?: boolean
  hasDesignJson?: boolean
  remark?: string
}

export interface WorkflowModelDetailVO extends WorkflowModelVO {
  bpmnXml?: string
  /** 已发布版本设计快照（version.design_json 优先） */
  designJson?: string
  versions?: WorkflowModelVersionVO[]
}

export interface WorkflowModelPageQuery {
  pageNo?: number
  pageSize?: number
  modelKey?: string
  modelName?: string
  status?: string
}

export interface WorkflowModelForm {
  id?: string
  modelKey?: string
  modelName?: string
  bizType?: string
  categoryId?: string
  categoryName?: string
  formType?: string
  formRoute?: string
  status?: string
  remark?: string
}

export interface WorkflowModelDeployPayload {
  modelId: string
  bpmnXml: string
  remark?: string
}

export interface WorkflowModelDeployResultVO {
  modelId?: string
  versionId?: string
  versionNo?: number
  deploymentId?: string
  processDefinitionId?: string
  processDefinitionKey?: string
}

export interface WorkflowModelSyncCatalogVO {
  platformModelId?: string
  modelKey?: string
  modelName?: string
  bizType?: string
  categoryName?: string
  bindingMode?: string
  formRoute?: string
  synced?: boolean
  syncedModelId?: string
}

export interface WorkflowModelSyncPayload {
  platformModelId: string
}
