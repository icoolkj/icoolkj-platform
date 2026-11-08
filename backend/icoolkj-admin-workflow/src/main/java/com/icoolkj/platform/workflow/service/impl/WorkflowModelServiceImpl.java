package com.icoolkj.platform.workflow.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.workflow.config.WorkflowBootstrapProperties;
import com.icoolkj.platform.workflow.config.WorkflowTenantSupport;
import com.icoolkj.platform.workflow.convert.WorkflowModelConvert;
import com.icoolkj.platform.workflow.deploy.WorkflowVersionPublishCommand;
import com.icoolkj.platform.workflow.design.DesignNodeHandlerRegistry;
import com.icoolkj.platform.workflow.design.WorkflowDesignCompiler;
import com.icoolkj.platform.workflow.domain.WfApproverRuleEntity;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.domain.WfModelVersionEntity;
import com.icoolkj.platform.workflow.dto.WorkflowModelCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDeployRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignDeployRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelPageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowModelSyncRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelUpdateRequest;
import com.icoolkj.platform.workflow.mapper.WfApproverRuleMapper;
import com.icoolkj.platform.workflow.mapper.WfModelMapper;
import com.icoolkj.platform.workflow.mapper.WfModelVersionMapper;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import com.icoolkj.platform.workflow.service.WorkflowModelService;
import com.icoolkj.platform.workflow.support.WorkflowBizBindingPolicySupport;
import com.icoolkj.platform.workflow.support.WorkflowDesignSoTSupport;
import com.icoolkj.platform.workflow.support.WorkflowModelAccessSupport;
import com.icoolkj.platform.workflow.vo.WorkflowModelDeployResultVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelDetailVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelSyncCatalogVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelVersionVO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 流程模型服务 */
@Service
public class WorkflowModelServiceImpl implements WorkflowModelService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowModelServiceImpl.class);

    private final WfModelMapper wfModelMapper;
    private final WfModelVersionMapper wfModelVersionMapper;
    private final WfApproverRuleMapper approverRuleMapper;
    private final WorkflowModelRegistry workflowModelRegistry;
    private final WorkflowBizBindingPolicySupport bindingPolicySupport;
    private final WorkflowModelAccessSupport modelAccessSupport;
    private final WorkflowBootstrapProperties bootstrapProperties;
    private final WorkflowDesignSoTSupport designSoTSupport;
    private final WorkflowDesignCompiler designCompiler;
    private final DesignNodeHandlerRegistry designNodeHandlerRegistry;

    public WorkflowModelServiceImpl(WfModelMapper wfModelMapper,
                                    WfModelVersionMapper wfModelVersionMapper,
                                    WfApproverRuleMapper approverRuleMapper,
                                    WorkflowModelRegistry workflowModelRegistry,
                                    WorkflowBizBindingPolicySupport bindingPolicySupport,
                                    WorkflowModelAccessSupport modelAccessSupport,
                                    WorkflowBootstrapProperties bootstrapProperties,
                                    WorkflowDesignSoTSupport designSoTSupport,
                                    WorkflowDesignCompiler designCompiler,
                                    DesignNodeHandlerRegistry designNodeHandlerRegistry) {
        this.wfModelMapper = wfModelMapper;
        this.wfModelVersionMapper = wfModelVersionMapper;
        this.approverRuleMapper = approverRuleMapper;
        this.workflowModelRegistry = workflowModelRegistry;
        this.bindingPolicySupport = bindingPolicySupport;
        this.modelAccessSupport = modelAccessSupport;
        this.bootstrapProperties = bootstrapProperties;
        this.designSoTSupport = designSoTSupport;
        this.designCompiler = designCompiler;
        this.designNodeHandlerRegistry = designNodeHandlerRegistry;
    }

    @Override
    public PageResult<WorkflowModelVO> page(WorkflowModelPageQuery query) {
        String ownerTenantId = WorkflowTenantSupport.modelOwnerTenantId();
        query.setScopeTenantId(ownerTenantId);
        // 租户列表仅本租户；平台目录走同步接口
        query.setIncludePlatform(false);
        long total = wfModelMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<WorkflowModelVO> rows = wfModelMapper.selectPage(query).stream()
                .map(WorkflowModelConvert::toVo)
                .toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public WorkflowModelDetailVO getById(String id) {
        WfModelEntity model = workflowModelRegistry.requireById(id);
        modelAccessSupport.assertReadableByOperator(model);
        WfModelVersionEntity published = wfModelVersionMapper.selectPublishedByModelId(id);
        if (published == null) {
            published = wfModelVersionMapper.selectLatestByModelId(id);
        }
        WorkflowModelDetailVO detail = WorkflowModelConvert.toDetailVo(model, published);
        detail.setVersions(listVersions(id));
        String designPayload = designSoTSupport.resolveDesignPayload(
                published == null ? null : published.getDesignJson(),
                model.getProcessConfig());
        detail.setDesignJson(designPayload);
        return detail;
    }

    @Override
    public List<WorkflowModelVersionVO> listVersions(String modelId) {
        WfModelEntity model = workflowModelRegistry.requireById(modelId);
        modelAccessSupport.assertReadableByOperator(model);
        return wfModelVersionMapper.selectByModelId(modelId).stream()
                .map(WorkflowModelConvert::toVersionVo)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(WorkflowModelCreateRequest request) {
        String ownerTenantId = WorkflowTenantSupport.modelOwnerTenantId();
        bindingPolicySupport.assertMayCreateModel(ownerTenantId, request.getBizType());
        if (wfModelMapper.selectByTenantAndKey(ownerTenantId, request.getModelKey().trim()) != null) {
            throw new BusinessException("error.workflow.model_key_duplicate");
        }
        WfModelEntity entity = WorkflowModelConvert.toEntity(request, ownerTenantId);
        entity.setId(IdGeneratorUtils.nextIdStr());
        AuditFieldHelper.fillInsert(entity);
        wfModelMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WorkflowModelUpdateRequest request) {
        WfModelEntity entity = workflowModelRegistry.requireById(request.getId());
        modelAccessSupport.assertOwnedByOperator(entity);
        WorkflowModelConvert.copy(request, entity);
        AuditFieldHelper.fillUpdate(entity);
        wfModelMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowModelDeployResultVO deploy(WorkflowModelDeployRequest request) {
        WfModelEntity model = workflowModelRegistry.requireById(request.getModelId());
        modelAccessSupport.assertOwnedByOperator(model);
        return workflowModelRegistry.publishVersion(request.getModelId(), request.getBpmnXml(), request.getRemark());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowModelDeployResultVO designDeploy(WorkflowModelDesignDeployRequest request) {
        WfModelEntity model = workflowModelRegistry.requireById(request.getModelId());
        modelAccessSupport.assertOwnedByOperator(model);
        String ownerTenantId = WorkflowTenantSupport.modelOwnerTenantId();
        if (TenantConstants.PLATFORM_TENANT_ID.equals(ownerTenantId)) {
            throw new BusinessException("error.workflow.model_sync_tenant_only");
        }

        List<WorkflowModelDesignNodeRequest> nodes = request.getNodes();
        WorkflowDesignCompiler.CompiledDesign compiled =
                designCompiler.compile(model.getModelKey(), model.getModelName(), nodes);

        // 先合并 design + 全局属性，再发布冻结，避免版本快照落后于草稿
        String baseConfig = StringUtils.hasText(request.getProcessConfig())
                ? request.getProcessConfig().trim()
                : model.getProcessConfig();
        String mergedConfig = designSoTSupport.writeDesignAuthority(baseConfig, nodes);
        WfModelEntity fresh = workflowModelRegistry.requireById(model.getId());
        fresh.setProcessConfig(mergedConfig);
        AuditFieldHelper.fillUpdate(fresh);
        wfModelMapper.updateById(fresh);

        String remark = StringUtils.hasText(request.getRemark()) ? request.getRemark() : "design deploy";
        WorkflowModelDeployResultVO result = workflowModelRegistry.publishVersion(
                new WorkflowVersionPublishCommand(
                        model.getId(), compiled.bpmnXml(), compiled.designJson(), remark, mergedConfig));

        syncDesignApproverRules(model.getId(), ownerTenantId, nodes);
        return result;
    }

    private void syncDesignApproverRules(String modelId, String ownerTenantId,
                                         List<WorkflowModelDesignNodeRequest> nodes) {
        List<WfApproverRuleEntity> existing = approverRuleMapper.selectByModelAndTenants(
                modelId, List.of(ownerTenantId));
        Set<String> keepKeys = new HashSet<>();
        AtomicInteger index = new AtomicInteger(0);
        designNodeHandlerRegistry.walkApproverRuleNodes(nodes, node -> {
            String key = node.getTaskDefinitionKey().trim();
            keepKeys.add(key);
            WfApproverRuleEntity row = approverRuleMapper.selectByUniqueKey(modelId, key, ownerTenantId);
            int sortNo = node.getSortNo() != null ? node.getSortNo() : index.incrementAndGet();
            String resolveScope = StringUtils.hasText(node.getResolveScope())
                    ? node.getResolveScope().trim() : "biz_tenant";
            if (row == null) {
                row = new WfApproverRuleEntity();
                row.setId(IdGeneratorUtils.nextIdStr());
                row.setTenantId(ownerTenantId);
                row.setModelId(modelId);
                row.setTaskDefinitionKey(key);
                row.setVariableName(node.getVariableName().trim());
                row.setStrategyType(node.getStrategyType());
                row.setStrategyConfig(node.getStrategyConfig().trim());
                row.setResolveScope(resolveScope);
                row.setSortNo(sortNo);
                row.setStatus(CommonConstants.STATUS_NORMAL);
                AuditFieldHelper.fillInsert(row);
                approverRuleMapper.insert(row);
            } else {
                row.setVariableName(node.getVariableName().trim());
                row.setStrategyType(node.getStrategyType());
                row.setStrategyConfig(node.getStrategyConfig().trim());
                row.setResolveScope(resolveScope);
                row.setSortNo(sortNo);
                row.setStatus(CommonConstants.STATUS_NORMAL);
                AuditFieldHelper.fillUpdate(row);
                approverRuleMapper.updateById(row);
            }
        });
        if (!CollectionUtils.isEmpty(existing)) {
            for (WfApproverRuleEntity old : existing) {
                if (!keepKeys.contains(old.getTaskDefinitionKey())) {
                    approverRuleMapper.deleteById(old.getId());
                }
            }
        }
    }

    @Override
    public List<WorkflowModelSyncCatalogVO> listSyncCatalog() {
        assertTenantOperatorForSync();
        String ownerTenantId = WorkflowTenantSupport.modelOwnerTenantId();
        List<WfModelEntity> platformModels = wfModelMapper.selectPlatformSyncCatalog();
        if (CollectionUtils.isEmpty(platformModels)) {
            return List.of();
        }
        List<WorkflowModelSyncCatalogVO> rows = new ArrayList<>();
        for (WfModelEntity platform : platformModels) {
            if (!bindingPolicySupport.isSyncCatalogCandidate(platform.getBizType())) {
                continue;
            }
            WorkflowModelSyncCatalogVO vo = new WorkflowModelSyncCatalogVO();
            vo.setPlatformModelId(platform.getId());
            vo.setModelKey(platform.getModelKey());
            vo.setModelName(platform.getModelName());
            vo.setBizType(platform.getBizType());
            vo.setCategoryName(platform.getCategoryName());
            vo.setFormRoute(platform.getFormRoute());
            vo.setBindingMode(bindingPolicySupport.resolveBindingMode(platform.getBizType()));
            WfModelEntity synced = wfModelMapper.selectTenantByBizType(ownerTenantId, platform.getBizType());
            if (synced != null) {
                vo.setSynced(true);
                vo.setSyncedModelId(synced.getId());
            }
            rows.add(vo);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String syncFromPlatform(WorkflowModelSyncRequest request) {
        assertTenantOperatorForSync();
        String ownerTenantId = WorkflowTenantSupport.modelOwnerTenantId();
        WfModelEntity platform = workflowModelRegistry.requireById(request.getPlatformModelId());
        if (!TenantConstants.PLATFORM_TENANT_ID.equals(platform.getTenantId())) {
            throw new BusinessException("error.workflow.model_sync_platform_only");
        }
        if (!bindingPolicySupport.isSyncCatalogCandidate(platform.getBizType())) {
            throw new BusinessException("error.workflow.model_sync_not_allowed");
        }
        if (wfModelMapper.selectTenantByBizType(ownerTenantId, platform.getBizType()) != null) {
            throw new BusinessException("error.workflow.model_already_synced");
        }
        if (wfModelMapper.selectByTenantAndKey(ownerTenantId, platform.getModelKey()) != null) {
            throw new BusinessException("error.workflow.model_key_duplicate");
        }

        WfModelEntity tenantModel = new WfModelEntity();
        tenantModel.setId(IdGeneratorUtils.nextIdStr());
        tenantModel.setTenantId(ownerTenantId);
        tenantModel.setModelKey(platform.getModelKey());
        tenantModel.setModelName(platform.getModelName());
        tenantModel.setCategoryName(platform.getCategoryName());
        tenantModel.setBizType(platform.getBizType());
        tenantModel.setFormType(platform.getFormType());
        tenantModel.setFormRoute(platform.getFormRoute());
        tenantModel.setLatestVersion(0);
        tenantModel.setStatus(platform.getStatus());
        tenantModel.setRemark("synced from platform:" + platform.getId());
        tenantModel.setProcessConfig(platform.getProcessConfig());
        tenantModel.setSourceModelId(platform.getId());
        AuditFieldHelper.fillInsert(tenantModel);
        wfModelMapper.insert(tenantModel);

        String bpmnXml;
        String designJson = null;
        WfModelVersionEntity platformVersion = resolvePlatformVersion(platform);
        if (platformVersion != null && StringUtils.hasText(platformVersion.getBpmnXml())) {
            bpmnXml = platformVersion.getBpmnXml();
            designJson = platformVersion.getDesignJson();
        } else {
            bpmnXml = readClasspathBpmn(platform.getModelKey());
        }
        if (!StringUtils.hasText(bpmnXml)) {
            throw new BusinessException("error.workflow.model_sync_bpmn_missing");
        }
        if (!StringUtils.hasText(designJson)) {
            log.warn("syncFromPlatform: platform model {} has no design_json; publishing BPMN only",
                    platform.getModelKey());
        }
        String processConfigJson = platformVersion != null
                && StringUtils.hasText(platformVersion.getProcessConfigJson())
                ? platformVersion.getProcessConfigJson()
                : tenantModel.getProcessConfig();
        workflowModelRegistry.publishVersion(new WorkflowVersionPublishCommand(
                tenantModel.getId(), bpmnXml, designJson, "sync from platform", processConfigJson));
        copyPlatformApproverRules(platform.getId(), tenantModel.getId(), ownerTenantId);
        return tenantModel.getId();
    }

    private WfModelVersionEntity resolvePlatformVersion(WfModelEntity platform) {
        WfModelVersionEntity published = wfModelVersionMapper.selectPublishedByModelId(platform.getId());
        if (published != null && StringUtils.hasText(published.getBpmnXml())) {
            return published;
        }
        return wfModelVersionMapper.selectLatestByModelId(platform.getId());
    }

    private void copyPlatformApproverRules(String platformModelId, String tenantModelId, String ownerTenantId) {
        List<WfApproverRuleEntity> platformRules = approverRuleMapper.selectByModelAndTenants(
                platformModelId, List.of(TenantConstants.PLATFORM_TENANT_ID));
        if (CollectionUtils.isEmpty(platformRules)) {
            return;
        }
        for (WfApproverRuleEntity source : platformRules) {
            WfApproverRuleEntity copy = new WfApproverRuleEntity();
            copy.setId(IdGeneratorUtils.nextIdStr());
            copy.setTenantId(ownerTenantId);
            copy.setModelId(tenantModelId);
            copy.setTaskDefinitionKey(source.getTaskDefinitionKey());
            copy.setVariableName(source.getVariableName());
            copy.setStrategyType(source.getStrategyType());
            copy.setStrategyConfig(source.getStrategyConfig());
            copy.setResolveScope(source.getResolveScope());
            copy.setSortNo(source.getSortNo());
            copy.setStatus(source.getStatus());
            copy.setRemark(source.getRemark());
            AuditFieldHelper.fillInsert(copy);
            approverRuleMapper.insert(copy);
        }
    }

    private String readClasspathBpmn(String modelKey) {
        if (!StringUtils.hasText(modelKey) || bootstrapProperties.getClasspathResources() == null) {
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
            } catch (IOException ignored) {
                // try next
            }
        }
        return null;
    }

    private void assertTenantOperatorForSync() {
        if (WorkflowTenantSupport.isPlatformOperator()) {
            throw new BusinessException("error.workflow.model_sync_tenant_only");
        }
    }
}
