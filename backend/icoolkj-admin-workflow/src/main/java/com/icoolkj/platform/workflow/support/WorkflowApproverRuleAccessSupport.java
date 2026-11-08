package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.config.WorkflowTenantSupport;
import com.icoolkj.platform.workflow.domain.WfApproverRuleEntity;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;

/** 审批人规则租户边界与模型引用校验 */
@Component
public class WorkflowApproverRuleAccessSupport {

    private final WorkflowBizBindingPolicySupport bindingPolicySupport;

    public WorkflowApproverRuleAccessSupport(WorkflowBizBindingPolicySupport bindingPolicySupport) {
        this.bindingPolicySupport = bindingPolicySupport;
    }

    public String ruleOwnerTenantId() {
        return WorkflowTenantSupport.modelOwnerTenantId();
    }

    public void assertOwnedByOperator(WfApproverRuleEntity rule) {
        if (rule == null) {
            throw new BusinessException("error.workflow.approver_rule_not_found");
        }
        if (!Objects.equals(ruleOwnerTenantId(), rule.getTenantId())) {
            throw new BusinessException("error.workflow.approver_rule_access_denied");
        }
    }

    /** 本租户规则，或（非平台操作者查看）平台默认规则 */
    public void assertReadableByOperator(WfApproverRuleEntity rule) {
        if (rule == null) {
            throw new BusinessException("error.workflow.approver_rule_not_found");
        }
        String ownerTenantId = ruleOwnerTenantId();
        if (Objects.equals(ownerTenantId, rule.getTenantId())) {
            return;
        }
        if (!TenantConstants.PLATFORM_TENANT_ID.equals(ownerTenantId)
                && TenantConstants.PLATFORM_TENANT_ID.equals(rule.getTenantId())) {
            return;
        }
        throw new BusinessException("error.workflow.approver_rule_access_denied");
    }

    public void assertModelReferencable(WfModelEntity model, String ruleTenantId) {
        if (model == null) {
            throw new BusinessException("error.workflow.model_not_found");
        }
        if (TenantConstants.PLATFORM_TENANT_ID.equals(ruleTenantId)) {
            if (!TenantConstants.PLATFORM_TENANT_ID.equals(model.getTenantId())) {
                throw new BusinessException("error.workflow.approver_rule_model_invalid");
            }
            return;
        }
        if (TenantConstants.PLATFORM_TENANT_ID.equals(model.getTenantId())) {
            if (!bindingPolicySupport.allowTenantApproverOverride(model.getBizType())) {
                throw new BusinessException("error.workflow.approver_rule_override_denied");
            }
            return;
        }
        if (!Objects.equals(ruleTenantId, model.getTenantId())) {
            throw new BusinessException("error.workflow.approver_rule_model_invalid");
        }
    }
}
