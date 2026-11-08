package com.icoolkj.platform.workflow.convert;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.workflow.domain.WfApproverRuleEntity;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRuleCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRuleUpdateRequest;
import com.icoolkj.platform.workflow.vo.WorkflowApproverRuleVO;

/** 审批人规则对象转换 */
public final class WorkflowApproverRuleConvert {

    private WorkflowApproverRuleConvert() {
    }

    public static WorkflowApproverRuleVO toVo(WfApproverRuleEntity entity) {
        if (entity == null) {
            return null;
        }
        WorkflowApproverRuleVO vo = new WorkflowApproverRuleVO();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setModelId(entity.getModelId());
        vo.setTaskDefinitionKey(entity.getTaskDefinitionKey());
        vo.setVariableName(entity.getVariableName());
        vo.setStrategyType(entity.getStrategyType());
        vo.setStrategyConfig(entity.getStrategyConfig());
        vo.setResolveScope(entity.getResolveScope());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    public static WfApproverRuleEntity toEntity(WorkflowApproverRuleCreateRequest request, String tenantId) {
        WfApproverRuleEntity entity = new WfApproverRuleEntity();
        entity.setTenantId(tenantId);
        entity.setModelId(request.getModelId().trim());
        entity.setTaskDefinitionKey(request.getTaskDefinitionKey().trim());
        entity.setVariableName(request.getVariableName().trim());
        entity.setStrategyType(request.getStrategyType().trim());
        entity.setStrategyConfig(request.getStrategyConfig().trim());
        entity.setResolveScope(request.getResolveScope().trim());
        entity.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        entity.setStatus(defaultStatus(request.getStatus()));
        entity.setRemark(trimToNull(request.getRemark()));
        return entity;
    }

    public static void copy(WorkflowApproverRuleUpdateRequest request, WfApproverRuleEntity entity) {
        entity.setVariableName(request.getVariableName().trim());
        entity.setStrategyType(request.getStrategyType().trim());
        entity.setStrategyConfig(request.getStrategyConfig().trim());
        entity.setResolveScope(request.getResolveScope().trim());
        entity.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        entity.setStatus(defaultStatus(request.getStatus()));
        entity.setRemark(trimToNull(request.getRemark()));
    }

    private static String defaultStatus(String status) {
        return status == null || status.isBlank() ? CommonConstants.STATUS_NORMAL : status.trim();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
