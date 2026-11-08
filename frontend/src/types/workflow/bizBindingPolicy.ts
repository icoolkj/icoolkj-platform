/** 业务绑定策略 */
export interface WorkflowBizBindingPolicyVO {
  id?: string
  bizType?: string
  bindingMode?: string
  allowTenantApproverOverride?: string
  remark?: string
}

export interface WorkflowBizBindingPolicyForm {
  id?: string
  bizType: string
  bindingMode: string
  allowTenantApproverOverride?: string
  remark?: string
}

export interface WorkflowBizBindingPolicyUpdateForm {
  id: string
  bindingMode: string
  allowTenantApproverOverride?: string
  remark?: string
}

export interface WorkflowBizBindingPolicyPageQuery {
  pageNo?: number
  pageSize?: number
  bizType?: string
  bindingMode?: string
}
