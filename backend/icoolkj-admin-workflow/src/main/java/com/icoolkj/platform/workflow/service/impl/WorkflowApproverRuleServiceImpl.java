package com.icoolkj.platform.workflow.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.workflow.convert.WorkflowApproverRuleConvert;
import com.icoolkj.platform.workflow.domain.WfApproverRuleEntity;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRuleCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRulePageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRuleUpdateRequest;
import com.icoolkj.platform.workflow.mapper.WfApproverRuleMapper;
import com.icoolkj.platform.workflow.mapper.WfModelMapper;
import com.icoolkj.platform.workflow.service.WorkflowApproverRuleService;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import com.icoolkj.platform.workflow.support.WorkflowApproverRuleAccessSupport;
import com.icoolkj.platform.workflow.support.WorkflowApproverRuleSupport;
import com.icoolkj.platform.workflow.vo.WorkflowApproverRuleVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelOptionVO;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 审批人规则管理 */
@Service
public class WorkflowApproverRuleServiceImpl implements WorkflowApproverRuleService {

    private final WfApproverRuleMapper approverRuleMapper;
    private final WfModelMapper wfModelMapper;
    private final WorkflowModelRegistry workflowModelRegistry;
    private final WorkflowApproverRuleAccessSupport accessSupport;
    private final WorkflowApproverRuleSupport approverRuleSupport;

    public WorkflowApproverRuleServiceImpl(WfApproverRuleMapper approverRuleMapper,
                                           WfModelMapper wfModelMapper,
                                           WorkflowModelRegistry workflowModelRegistry,
                                           WorkflowApproverRuleAccessSupport accessSupport,
                                           WorkflowApproverRuleSupport approverRuleSupport) {
        this.approverRuleMapper = approverRuleMapper;
        this.wfModelMapper = wfModelMapper;
        this.workflowModelRegistry = workflowModelRegistry;
        this.accessSupport = accessSupport;
        this.approverRuleSupport = approverRuleSupport;
    }

    @Override
    public PageResult<WorkflowApproverRuleVO> page(WorkflowApproverRulePageQuery query) {
        String ownerTenantId = accessSupport.ruleOwnerTenantId();
        query.setScopeTenantId(ownerTenantId);
        query.setIncludePlatform(!TenantConstants.PLATFORM_TENANT_ID.equals(ownerTenantId));
        long total = approverRuleMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<WorkflowApproverRuleVO> rows = approverRuleMapper.selectPage(query);
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public WorkflowApproverRuleVO getById(String id) {
        WfApproverRuleEntity entity = requireRule(id);
        accessSupport.assertReadableByOperator(entity);
        WorkflowApproverRuleVO vo = WorkflowApproverRuleConvert.toVo(entity);
        enrichModel(vo);
        return vo;
    }

    @Override
    public List<WorkflowModelOptionVO> listModelOptions() {
        String ownerTenantId = accessSupport.ruleOwnerTenantId();
        boolean includePlatform = !TenantConstants.PLATFORM_TENANT_ID.equals(ownerTenantId);
        return wfModelMapper.selectOptions(ownerTenantId, includePlatform);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(WorkflowApproverRuleCreateRequest request) {
        String ownerTenantId = accessSupport.ruleOwnerTenantId();
        WfModelEntity model = workflowModelRegistry.requireById(request.getModelId());
        accessSupport.assertModelReferencable(model, ownerTenantId);
        approverRuleSupport.validateStrategyConfig(request.getStrategyType(), request.getStrategyConfig());
        assertUniqueKey(request.getModelId(), request.getTaskDefinitionKey(), ownerTenantId, null);

        WfApproverRuleEntity entity = WorkflowApproverRuleConvert.toEntity(request, ownerTenantId);
        entity.setId(IdGeneratorUtils.nextIdStr());
        AuditFieldHelper.fillInsert(entity);
        approverRuleMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WorkflowApproverRuleUpdateRequest request) {
        WfApproverRuleEntity entity = requireRule(request.getId());
        accessSupport.assertOwnedByOperator(entity);
        approverRuleSupport.validateStrategyConfig(request.getStrategyType(), request.getStrategyConfig());
        WorkflowApproverRuleConvert.copy(request, entity);
        AuditFieldHelper.fillUpdate(entity);
        approverRuleMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        WfApproverRuleEntity entity = requireRule(id);
        accessSupport.assertOwnedByOperator(entity);
        if (approverRuleMapper.deleteById(id) <= 0) {
            throw new BusinessException("error.workflow.approver_rule_not_found");
        }
    }

    private WfApproverRuleEntity requireRule(String id) {
        WfApproverRuleEntity entity = approverRuleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.workflow.approver_rule_not_found");
        }
        return entity;
    }

    private void assertUniqueKey(String modelId, String taskDefinitionKey, String tenantId, String excludeId) {
        WfApproverRuleEntity existing = approverRuleMapper.selectByUniqueKey(modelId, taskDefinitionKey, tenantId);
        if (existing != null && (excludeId == null || !excludeId.equals(existing.getId()))) {
            throw new BusinessException("error.workflow.approver_rule_duplicate");
        }
    }

    private void enrichModel(WorkflowApproverRuleVO vo) {
        if (vo == null || vo.getModelId() == null) {
            return;
        }
        WfModelEntity model = wfModelMapper.selectById(vo.getModelId());
        if (model != null) {
            vo.setModelKey(model.getModelKey());
            vo.setModelName(model.getModelName());
        }
    }
}
