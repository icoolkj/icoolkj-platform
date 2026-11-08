package com.icoolkj.platform.workflow.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.workflow.config.WorkflowTenantSupport;
import com.icoolkj.platform.workflow.deploy.WorkflowVersionPublishCommand;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.domain.WfModelVersionEntity;
import com.icoolkj.platform.workflow.engine.FlowableProcessDeployService;
import com.icoolkj.platform.workflow.mapper.WfModelMapper;
import com.icoolkj.platform.workflow.mapper.WfModelVersionMapper;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import com.icoolkj.platform.workflow.support.WorkflowBizBindingPolicySupport;
import com.icoolkj.platform.workflow.vo.WorkflowModelDeployResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 流程模型注册与发布 */
@Service
public class WorkflowModelRegistryImpl implements WorkflowModelRegistry {

    private final WfModelMapper wfModelMapper;
    private final WfModelVersionMapper wfModelVersionMapper;
    private final FlowableProcessDeployService processDeployService;
    private final WorkflowBizBindingPolicySupport bindingPolicySupport;

    public WorkflowModelRegistryImpl(WfModelMapper wfModelMapper,
                                     WfModelVersionMapper wfModelVersionMapper,
                                     FlowableProcessDeployService processDeployService,
                                     WorkflowBizBindingPolicySupport bindingPolicySupport) {
        this.wfModelMapper = wfModelMapper;
        this.wfModelVersionMapper = wfModelVersionMapper;
        this.processDeployService = processDeployService;
        this.bindingPolicySupport = bindingPolicySupport;
    }

    @Override
    public WfModelEntity requireById(String modelId) {
        WfModelEntity entity = wfModelMapper.selectById(modelId);
        if (entity == null) {
            throw new BusinessException("error.workflow.model_not_found");
        }
        return entity;
    }

    @Override
    public WfModelEntity requireByModelKey(String modelKey) {
        WfModelEntity entity = resolveByModelKey(modelKey);
        if (entity == null) {
            throw new BusinessException("error.workflow.model_not_found");
        }
        return entity;
    }

    @Override
    public WfModelEntity requireActiveByBizType(String tenantId, String bizType) {
        String normalizedTenantId = StringUtils.hasText(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID;
        WfModelEntity entity;
        if (bindingPolicySupport.isPlatformMandatory(bizType)) {
            entity = wfModelMapper.selectPlatformByBizType(bizType);
            if (entity == null) {
                throw new BusinessException("error.workflow.model_not_found");
            }
        } else if (bindingPolicySupport.isTenantRequired(bizType)) {
            entity = wfModelMapper.selectTenantByBizType(normalizedTenantId, bizType);
            if (entity == null) {
                throw new BusinessException("error.workflow.model_not_synced");
            }
        } else {
            entity = wfModelMapper.selectActiveByBizType(normalizedTenantId, bizType);
            if (entity == null) {
                throw new BusinessException("error.workflow.model_not_found");
            }
        }
        if (entity.getLatestVersion() == null || entity.getLatestVersion() <= 0) {
            throw new BusinessException("error.workflow.model_not_deployed");
        }
        return entity;
    }

    @Override
    public String resolveFormRoute(String processDefinitionKey) {
        WfModelEntity model = resolveByModelKey(processDefinitionKey);
        return model == null ? null : model.getFormRoute();
    }

    @Override
    public String resolveModelName(String processDefinitionKey) {
        WfModelEntity model = resolveByModelKey(processDefinitionKey);
        return model == null ? processDefinitionKey : model.getModelName();
    }

    private WfModelEntity resolveByModelKey(String modelKey) {
        if (!StringUtils.hasText(modelKey)) {
            return null;
        }
        String preferTenantId = WorkflowTenantSupport.currentOperatorTenantId();
        if (TenantConstants.PLATFORM_TENANT_ID.equals(preferTenantId)) {
            return wfModelMapper.selectByModelKey(modelKey.trim());
        }
        WfModelEntity preferred = wfModelMapper.selectByModelKeyPreferTenant(modelKey.trim(), preferTenantId);
        if (preferred != null) {
            return preferred;
        }
        return wfModelMapper.selectByModelKey(modelKey.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowModelDeployResultVO publishVersion(WorkflowVersionPublishCommand command) {
        WfModelEntity model = requireById(command.modelId());
        validateBpmnKey(model.getModelKey(), command.bpmnXml());

        Integer maxVersion = wfModelVersionMapper.selectMaxVersionNo(command.modelId());
        int nextVersion = maxVersion == null ? 1 : maxVersion + 1;

        WfModelVersionEntity versionEntity = new WfModelVersionEntity();
        versionEntity.setId(IdGeneratorUtils.nextIdStr());
        versionEntity.setTenantId(model.getTenantId());
        versionEntity.setModelId(command.modelId());
        versionEntity.setVersionNo(nextVersion);
        versionEntity.setBpmnXml(command.bpmnXml());
        versionEntity.setDesignJson(command.designJson());
        // 冻结本版全局配置；显式传入优先（design-deploy 合并后），否则回落模型草稿
        String frozenConfig = StringUtils.hasText(command.processConfigJson())
                ? command.processConfigJson()
                : model.getProcessConfig();
        versionEntity.setProcessConfigJson(frozenConfig);
        versionEntity.setPublished(0);
        versionEntity.setRemark(command.remark());
        AuditFieldHelper.fillInsert(versionEntity);
        wfModelVersionMapper.insert(versionEntity);

        String resourceName = model.getModelKey() + ".bpmn20.xml";
        WorkflowModelDeployResultVO deployResult = processDeployService.deployBpmnXml(
                resourceName,
                command.bpmnXml(),
                "wf_model:" + command.modelId() + ":v" + nextVersion,
                model.getTenantId());

        wfModelVersionMapper.unpublishByModelId(command.modelId());
        versionEntity.setDeploymentId(deployResult.getDeploymentId());
        versionEntity.setProcessDefinitionId(deployResult.getProcessDefinitionId());
        versionEntity.setProcessDefinitionKey(deployResult.getProcessDefinitionKey());
        versionEntity.setPublished(1);
        AuditFieldHelper.fillUpdate(versionEntity);
        wfModelVersionMapper.updateById(versionEntity);

        model.setLatestVersion(nextVersion);
        AuditFieldHelper.fillUpdate(model);
        wfModelMapper.updateById(model);

        deployResult.setModelId(command.modelId());
        deployResult.setVersionId(versionEntity.getId());
        deployResult.setVersionNo(nextVersion);
        return deployResult;
    }

    private void validateBpmnKey(String modelKey, String bpmnXml) {
        if (!StringUtils.hasText(bpmnXml)) {
            throw new BusinessException("error.workflow.bpmn_invalid");
        }
        String marker = "process id=\"" + modelKey + "\"";
        String marker2 = "process id='" + modelKey + "'";
        if (!bpmnXml.contains(marker) && !bpmnXml.contains(marker2)) {
            throw new BusinessException("error.workflow.bpmn_key_mismatch", modelKey);
        }
    }
}
