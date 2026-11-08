package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.domain.WfModelVersionEntity;
import com.icoolkj.platform.workflow.mapper.WfModelVersionMapper;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 运行态读取「发布版本」上的全局配置（behavior/notify/startAcl），避免读到模型草稿。
 * <p>优先 {@code wf_model_version.process_config_json}；旧版本缺快照时懒回填一次后使用。
 */
@Component
public class WorkflowVersionConfigLookup {

    private static final Logger log = LoggerFactory.getLogger(WorkflowVersionConfigLookup.class);

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final WfModelVersionMapper wfModelVersionMapper;
    private final WorkflowModelRegistry workflowModelRegistry;
    private final WorkflowProcessConfigSupport processConfigSupport;

    public WorkflowVersionConfigLookup(RuntimeService runtimeService,
                                       HistoryService historyService,
                                       WfModelVersionMapper wfModelVersionMapper,
                                       WorkflowModelRegistry workflowModelRegistry,
                                       WorkflowProcessConfigSupport processConfigSupport) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.wfModelVersionMapper = wfModelVersionMapper;
        this.workflowModelRegistry = workflowModelRegistry;
        this.processConfigSupport = processConfigSupport;
    }

    public WorkflowProcessConfigSupport.ProcessConfig resolveByProcessInstanceId(String processInstanceId) {
        return processConfigSupport.parse(resolveProcessConfigJson(processInstanceId, null));
    }

    public WorkflowProcessConfigSupport.ProcessConfig resolveByProcessInstanceId(String processInstanceId,
                                                                                String modelIdFallback) {
        return processConfigSupport.parse(resolveProcessConfigJson(processInstanceId, modelIdFallback));
    }

    /** 发起/目录：读模型当前已发布版本快照；无快照则回落草稿。 */
    public WorkflowProcessConfigSupport.ProcessConfig resolvePublishedByModelId(String modelId) {
        return processConfigSupport.parse(resolvePublishedConfigJson(modelId));
    }

    public String resolvePublishedConfigJson(String modelId) {
        if (!StringUtils.hasText(modelId)) {
            return null;
        }
        WfModelVersionEntity version = TenantScopeRunner.runIgnoringTenant(
                () -> wfModelVersionMapper.selectPublishedByModelId(modelId));
        if (version != null) {
            String frozen = ensureProcessConfigSnapshot(version);
            if (StringUtils.hasText(frozen)) {
                return frozen;
            }
        }
        return loadModelDraftConfig(modelId);
    }

    public String resolveProcessConfigJson(String processInstanceId, String modelIdFallback) {
        String pdId = resolveProcessDefinitionId(processInstanceId);
        if (StringUtils.hasText(pdId)) {
            WfModelVersionEntity version = TenantScopeRunner.runIgnoringTenant(
                    () -> wfModelVersionMapper.selectByProcessDefinitionId(pdId));
            if (version != null) {
                String frozen = ensureProcessConfigSnapshot(version);
                if (StringUtils.hasText(frozen)) {
                    return frozen;
                }
            }
        }
        if (StringUtils.hasText(modelIdFallback)) {
            return loadModelDraftConfig(modelIdFallback);
        }
        return null;
    }

    /**
     * 旧版本无 process_config_json 时，用当前模型草稿回填并落库（一次性自愈，避免运行态持续读草稿）。
     */
    private String ensureProcessConfigSnapshot(WfModelVersionEntity version) {
        if (version == null) {
            return null;
        }
        if (StringUtils.hasText(version.getProcessConfigJson())) {
            return version.getProcessConfigJson();
        }
        if (!StringUtils.hasText(version.getModelId())) {
            return null;
        }
        String draft = loadModelDraftConfig(version.getModelId());
        if (!StringUtils.hasText(draft)) {
            return null;
        }
        version.setProcessConfigJson(draft);
        AuditFieldHelper.fillUpdate(version);
        try {
            TenantScopeRunner.runIgnoringTenant(() -> wfModelVersionMapper.updateById(version));
            log.info("Backfilled process_config_json for wf_model_version id={} modelId={} v{}",
                    version.getId(), version.getModelId(), version.getVersionNo());
        } catch (Exception ex) {
            log.warn("Failed to backfill process_config_json for version {}: {}",
                    version.getId(), ex.getMessage());
        }
        return draft;
    }

    private String resolveProcessDefinitionId(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return null;
        }
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (pi != null && StringUtils.hasText(pi.getProcessDefinitionId())) {
            return pi.getProcessDefinitionId();
        }
        HistoricProcessInstance historic = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        return historic == null ? null : historic.getProcessDefinitionId();
    }

    private String loadModelDraftConfig(String modelId) {
        try {
            WfModelEntity model = workflowModelRegistry.requireById(modelId);
            return model == null ? null : model.getProcessConfig();
        } catch (Exception ex) {
            return null;
        }
    }
}
