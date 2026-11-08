package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.domain.WfApproverRuleEntity;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.domain.WfModelVersionEntity;
import com.icoolkj.platform.workflow.mapper.WfApproverRuleMapper;
import com.icoolkj.platform.workflow.mapper.WfModelMapper;
import com.icoolkj.platform.workflow.mapper.WfModelVersionMapper;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import com.icoolkj.platform.workflow.vo.WorkflowModelOptionVO;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Admin-facing workflow model queries for OA/system modules.
 * Keep MyBatis mappers package-private to workflow callers outside this facade.
 */
@Component
public class WorkflowModelAdminSupport {

    private final WfModelMapper wfModelMapper;
    private final WfModelVersionMapper wfModelVersionMapper;
    private final WfApproverRuleMapper approverRuleMapper;
    private final WorkflowModelRegistry workflowModelRegistry;

    public WorkflowModelAdminSupport(WfModelMapper wfModelMapper,
                                     WfModelVersionMapper wfModelVersionMapper,
                                     WfApproverRuleMapper approverRuleMapper,
                                     WorkflowModelRegistry workflowModelRegistry) {
        this.wfModelMapper = wfModelMapper;
        this.wfModelVersionMapper = wfModelVersionMapper;
        this.approverRuleMapper = approverRuleMapper;
        this.workflowModelRegistry = workflowModelRegistry;
    }

    public List<WorkflowModelOptionVO> listOptions(String scopeTenantId, boolean includePlatform) {
        return wfModelMapper.selectOptions(scopeTenantId, includePlatform);
    }

    public WfModelEntity requireDeployedById(String modelId) {
        WfModelEntity model = workflowModelRegistry.requireById(modelId);
        if (model.getLatestVersion() == null || model.getLatestVersion() <= 0) {
            throw new BusinessException("error.workflow.model_not_deployed");
        }
        return model;
    }

    public WfModelEntity findTenantByBizType(String tenantId, String bizType) {
        return wfModelMapper.selectTenantByBizType(tenantId, bizType);
    }

    public WfModelEntity findActiveByBizType(String tenantId, String bizType) {
        return wfModelMapper.selectActiveByBizType(tenantId, bizType);
    }

    public WfModelVersionEntity findPublishedByModelId(String modelId) {
        if (!StringUtils.hasText(modelId)) {
            return null;
        }
        return wfModelVersionMapper.selectPublishedByModelId(modelId);
    }

    public List<WfApproverRuleEntity> listApproverRules(String modelId, List<String> tenantIds) {
        return approverRuleMapper.selectByModelAndTenants(modelId, tenantIds);
    }
}
