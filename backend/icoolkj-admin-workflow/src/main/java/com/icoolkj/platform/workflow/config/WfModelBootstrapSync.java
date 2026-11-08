package com.icoolkj.platform.workflow.config;

import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.workflow.design.WorkflowDesignCompiler;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.domain.WfModelVersionEntity;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.engine.FlowableProcessDeployService;
import com.icoolkj.platform.workflow.mapper.WfModelMapper;
import com.icoolkj.platform.workflow.mapper.WfModelVersionMapper;
import com.icoolkj.platform.workflow.support.WorkflowDesignSoTSupport;
import com.icoolkj.platform.workflow.vo.WorkflowModelDeployResultVO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.flowable.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 启动后同步种子模型：有 {@code design_json} 时优先编译再部署；否则用 DB {@code bpmn_xml}。
 * <p>classpath 仅在 fallback 开启时用于补齐空版本。须在 {@link WorkflowProcessBootstrap} 之后执行。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 110)
@EnableConfigurationProperties(WorkflowBootstrapProperties.class)
@ConditionalOnProperty(prefix = "icoolkj.workflow.bootstrap", name = "model-sync-enabled", havingValue = "true", matchIfMissing = true)
public class WfModelBootstrapSync implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(WfModelBootstrapSync.class);

    private final WfModelMapper wfModelMapper;
    private final WfModelVersionMapper wfModelVersionMapper;
    private final FlowableProcessDeployService processDeployService;
    private final WorkflowBootstrapProperties bootstrapProperties;
    private final WorkflowDesignSoTSupport designSoTSupport;
    private final WorkflowDesignCompiler designCompiler;

    public WfModelBootstrapSync(WfModelMapper wfModelMapper,
                                WfModelVersionMapper wfModelVersionMapper,
                                FlowableProcessDeployService processDeployService,
                                WorkflowBootstrapProperties bootstrapProperties,
                                WorkflowDesignSoTSupport designSoTSupport,
                                WorkflowDesignCompiler designCompiler) {
        this.wfModelMapper = wfModelMapper;
        this.wfModelVersionMapper = wfModelVersionMapper;
        this.processDeployService = processDeployService;
        this.bootstrapProperties = bootstrapProperties;
        this.designSoTSupport = designSoTSupport;
        this.designCompiler = designCompiler;
    }

    @Override
    public void run(ApplicationArguments args) {
        TenantScopeRunner.runIgnoringTenant(() -> {
            List<WfModelEntity> models = wfModelMapper.selectPage(pageAll());
            log.info("WfModelBootstrapSync: inspecting {} workflow model(s)", models.size());
            for (WfModelEntity model : models) {
                syncModelIfNeeded(model);
            }
        });
    }

    private void syncModelIfNeeded(WfModelEntity model) {
        List<WfModelVersionEntity> versions = wfModelVersionMapper.selectByModelId(model.getId());
        if (model.getLatestVersion() != null && model.getLatestVersion() > 0 && !CollectionUtils.isEmpty(versions)) {
            ensureFlowableDeployed(model);
            return;
        }
        WfModelVersionEntity published = wfModelVersionMapper.selectPublishedByModelId(model.getId());
        if (published != null) {
            repairLatestVersion(model, published.getVersionNo());
            ensureFlowableDeployed(model);
            return;
        }
        ProcessDefinition definition = processDeployService.getLatestByKey(model.getModelKey());
        if (definition == null) {
            return;
        }
        if (!CollectionUtils.isEmpty(versions)) {
            WfModelVersionEntity latest = versions.get(0);
            repairLatestVersion(model, latest.getVersionNo());
            return;
        }
        String bpmnXml = readBootstrapBpmn(model.getModelKey());
        if (!StringUtils.hasText(bpmnXml)) {
            log.warn("Skip wf_model_version sync for {}: BPMN not found on classpath", model.getModelKey());
            return;
        }

        WfModelVersionEntity version = new WfModelVersionEntity();
        version.setId(IdGeneratorUtils.nextIdStr());
        version.setTenantId(model.getTenantId());
        version.setModelId(model.getId());
        version.setVersionNo(1);
        version.setBpmnXml(bpmnXml);
        version.setDeploymentId(definition.getDeploymentId());
        version.setProcessDefinitionId(definition.getId());
        version.setProcessDefinitionKey(definition.getKey());
        version.setPublished(1);
        version.setRemark("bootstrap sync");
        AuditFieldHelper.fillInsert(version);
        wfModelVersionMapper.insert(version);

        model.setLatestVersion(1);
        AuditFieldHelper.fillUpdate(model);
        wfModelMapper.updateById(model);

        log.info("Synced wf_model_version for key={}, version=1, deploymentId={}",
                model.getModelKey(), definition.getDeploymentId());
    }

    private void repairLatestVersion(WfModelEntity model, int versionNo) {
        if (model.getLatestVersion() != null && model.getLatestVersion() >= versionNo) {
            return;
        }
        model.setLatestVersion(versionNo);
        AuditFieldHelper.fillUpdate(model);
        wfModelMapper.updateById(model);
        log.info("Repaired wf_model.latest_version for key={}, version={}", model.getModelKey(), versionNo);
    }

    /** 种子模型：优先 design_json 编译，再部署 Flowable */
    private void ensureFlowableDeployed(WfModelEntity model) {
        WfModelVersionEntity published = wfModelVersionMapper.selectPublishedByModelId(model.getId());
        if (published == null) {
            return;
        }
        String bpmnXml = resolveDeployBpmn(model, published);
        if (!StringUtils.hasText(bpmnXml)) {
            return;
        }
        String flowableTenantId = WorkflowTenantSupport.flowableTenantIdForModel(model.getTenantId());
        boolean alreadyDeployed = processDeployService.isDeployed(model.getModelKey(), flowableTenantId);
        boolean missingIds = !StringUtils.hasText(published.getDeploymentId())
                || !StringUtils.hasText(published.getProcessDefinitionId());
        // 种子 bpmn_xml 常为占位 stub；design 编译结果与库中不同时需重新部署
        boolean bpmnNeedsUpdate = StringUtils.hasText(bpmnXml)
                && !bpmnXml.equals(published.getBpmnXml());
        if (alreadyDeployed && !bpmnNeedsUpdate) {
            if (missingIds) {
                backfillDeployMetaIfMissing(model, published, bpmnXml, flowableTenantId);
            }
            return;
        }
        WorkflowModelDeployResultVO deployResult = processDeployService.deployBpmnXml(
                model.getModelKey() + ".bpmn20.xml",
                bpmnXml,
                "bootstrap:seed:" + model.getId(),
                model.getTenantId());
        published.setBpmnXml(bpmnXml);
        published.setDeploymentId(deployResult.getDeploymentId());
        published.setProcessDefinitionId(deployResult.getProcessDefinitionId());
        published.setProcessDefinitionKey(deployResult.getProcessDefinitionKey());
        AuditFieldHelper.fillUpdate(published);
        wfModelVersionMapper.updateById(published);
        log.info("Deployed seed/tenant BPMN for key={}, tenantId={}, deploymentId={}",
                model.getModelKey(), model.getTenantId(), deployResult.getDeploymentId());
    }

    private void backfillDeployMetaIfMissing(WfModelEntity model,
                                             WfModelVersionEntity published,
                                             String bpmnXml,
                                             String flowableTenantId) {
        ProcessDefinition definition = processDeployService.getLatestByKey(model.getModelKey(), flowableTenantId);
        if (definition == null) {
            return;
        }
        if (StringUtils.hasText(bpmnXml) && !StringUtils.hasText(published.getBpmnXml())) {
            published.setBpmnXml(bpmnXml);
        }
        published.setDeploymentId(definition.getDeploymentId());
        published.setProcessDefinitionId(definition.getId());
        published.setProcessDefinitionKey(definition.getKey());
        AuditFieldHelper.fillUpdate(published);
        wfModelVersionMapper.updateById(published);
        log.info("Backfilled deploy meta for key={}, tenantId={}, deploymentId={}",
                model.getModelKey(), model.getTenantId(), definition.getDeploymentId());
    }

    private String resolveDeployBpmn(WfModelEntity model, WfModelVersionEntity published) {
        if (StringUtils.hasText(published.getDesignJson())) {
            List<WorkflowModelDesignNodeRequest> nodes =
                    designSoTSupport.parseDesignNodes(published.getDesignJson());
            if (!CollectionUtils.isEmpty(nodes)) {
                try {
                    return designCompiler.compile(model.getModelKey(), model.getModelName(), nodes).bpmnXml();
                } catch (Exception ex) {
                    log.warn("design_json compile failed for {}, fallback bpmn_xml: {}",
                            model.getModelKey(), ex.getMessage());
                }
            }
        }
        return published.getBpmnXml();
    }

    private String readBootstrapBpmn(String modelKey) {
        if (!bootstrapProperties.isClasspathFallbackEnabled()) {
            return null;
        }
        if (bootstrapProperties.getClasspathResources() == null) {
            return null;
        }
        for (String resourcePath : bootstrapProperties.getClasspathResources()) {
            if (!resourcePath.contains(modelKey)) {
                continue;
            }
            try {
                Resource resource = new PathMatchingResourcePatternResolver()
                        .getResource("classpath:" + resourcePath);
                if (resource.exists()) {
                    return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (IOException ex) {
                log.warn("Failed to read BPMN resource {}: {}", resourcePath, ex.getMessage());
            }
        }
        return null;
    }

    private static com.icoolkj.platform.workflow.dto.WorkflowModelPageQuery pageAll() {
        com.icoolkj.platform.workflow.dto.WorkflowModelPageQuery query =
                new com.icoolkj.platform.workflow.dto.WorkflowModelPageQuery();
        query.setPageNo(1L);
        query.setPageSize(1000L);
        return query;
    }
}
