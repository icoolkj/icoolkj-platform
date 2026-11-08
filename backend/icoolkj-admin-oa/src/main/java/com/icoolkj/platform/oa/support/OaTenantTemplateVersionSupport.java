package com.icoolkj.platform.oa.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.oa.constant.OaEnableStatus;
import com.icoolkj.platform.oa.constant.OaFieldOriginType;
import com.icoolkj.platform.oa.constant.OaFieldSyncChangeType;
import com.icoolkj.platform.oa.constant.OaVersionStatus;
import com.icoolkj.platform.oa.convert.OaConvert;
import com.icoolkj.platform.oa.convert.OaTenantTemplateConvert;
import com.icoolkj.platform.oa.domain.OaPlatformFieldDefEntity;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateEntity;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateVersionEntity;
import com.icoolkj.platform.oa.domain.OaTenantFieldDefEntity;
import com.icoolkj.platform.oa.domain.OaTenantTemplateEntity;
import com.icoolkj.platform.oa.domain.OaTenantTemplateVersionEntity;
import com.icoolkj.platform.oa.dto.OaFieldDefBatchSaveRequest;
import com.icoolkj.platform.oa.dto.OaFieldDefItemRequest;
import com.icoolkj.platform.oa.dto.OaVersionCreateRequest;
import com.icoolkj.platform.oa.dto.OaVersionPublishRequest;
import com.icoolkj.platform.oa.mapper.OaPlatformFieldDefMapper;
import com.icoolkj.platform.oa.mapper.OaPlatformTemplateMapper;
import com.icoolkj.platform.oa.mapper.OaPlatformTemplateVersionMapper;
import com.icoolkj.platform.oa.mapper.OaTenantFieldDefMapper;
import com.icoolkj.platform.oa.mapper.OaTenantTemplateMapper;
import com.icoolkj.platform.oa.mapper.OaTenantTemplateVersionMapper;
import com.icoolkj.platform.oa.vo.OaFieldDefVO;
import com.icoolkj.platform.oa.vo.OaTenantFieldSyncDiffVO;
import com.icoolkj.platform.oa.vo.OaTenantSyncPreviewVO;
import com.icoolkj.platform.oa.vo.OaTenantTemplateDetailVO;
import com.icoolkj.platform.oa.vo.OaTenantTemplateVO;
import com.icoolkj.platform.oa.vo.OaTenantPublishReadinessVO;
import com.icoolkj.platform.oa.vo.OaTemplateVersionVO;
import com.icoolkj.platform.workflow.domain.WfApproverRuleEntity;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.domain.WfModelVersionEntity;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import com.icoolkj.platform.workflow.support.WorkflowBizBindingPolicySupport;
import com.icoolkj.platform.workflow.support.WorkflowDesignSoTSupport;
import com.icoolkj.platform.workflow.support.WorkflowModelAdminSupport;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** OA 租户模板版本与字段扩展（手写，不随 Velocity 覆盖） */
@Component
public class OaTenantTemplateVersionSupport {

    private final OaTenantTemplateMapper tenantTemplateMapper;
    private final OaTenantTemplateVersionMapper tenantVersionMapper;
    private final OaTenantFieldDefMapper tenantFieldDefMapper;
    private final OaPlatformTemplateMapper platformTemplateMapper;
    private final OaPlatformTemplateVersionMapper platformVersionMapper;
    private final OaPlatformFieldDefMapper platformFieldDefMapper;
    private final WorkflowModelAdminSupport workflowModelAdminSupport;
    private final WorkflowModelRegistry workflowModelRegistry;
    private final WorkflowBizBindingPolicySupport bindingPolicySupport;
    private final WorkflowDesignSoTSupport designSoTSupport;
    private final ObjectMapper objectMapper;
    private final OaTenantTemplateAccessSupport accessSupport;

    public OaTenantTemplateVersionSupport(OaTenantTemplateMapper tenantTemplateMapper,
                                          OaTenantTemplateVersionMapper tenantVersionMapper,
                                          OaTenantFieldDefMapper tenantFieldDefMapper,
                                          OaPlatformTemplateMapper platformTemplateMapper,
                                          OaPlatformTemplateVersionMapper platformVersionMapper,
                                          OaPlatformFieldDefMapper platformFieldDefMapper,
                                          WorkflowModelAdminSupport workflowModelAdminSupport,
                                          WorkflowModelRegistry workflowModelRegistry,
                                          WorkflowBizBindingPolicySupport bindingPolicySupport,
                                          WorkflowDesignSoTSupport designSoTSupport,
                                          ObjectMapper objectMapper,
                                          OaTenantTemplateAccessSupport accessSupport) {
        this.tenantTemplateMapper = tenantTemplateMapper;
        this.tenantVersionMapper = tenantVersionMapper;
        this.tenantFieldDefMapper = tenantFieldDefMapper;
        this.platformTemplateMapper = platformTemplateMapper;
        this.platformVersionMapper = platformVersionMapper;
        this.platformFieldDefMapper = platformFieldDefMapper;
        this.workflowModelAdminSupport = workflowModelAdminSupport;
        this.workflowModelRegistry = workflowModelRegistry;
        this.bindingPolicySupport = bindingPolicySupport;
        this.designSoTSupport = designSoTSupport;
        this.objectMapper = objectMapper;
        this.accessSupport = accessSupport;
    }

    public void enrichPlatformSyncInfo(OaTenantTemplateVO vo, OaTenantTemplateEntity template) {
        OaTenantSyncPreviewVO preview = buildSyncPreview(template);
        OaTenantTemplateConvert.fillPlatformSyncInfo(
                vo, preview.getPlatformLatestVersionNo(), Boolean.TRUE.equals(preview.getHasChanges()));
    }

    public OaTenantTemplateDetailVO buildDetail(OaTenantTemplateEntity template) {
        OaTenantTemplateDetailVO detail = OaTenantTemplateConvert.toDetailVo(
                OaTenantTemplateConvert.toVo(template));
        OaTenantTemplateVersionEntity draft = tenantVersionMapper.selectDraftByTemplateId(template.getId());
        OaTenantTemplateVersionEntity published = tenantVersionMapper.selectPublishedByTemplateId(template.getId());
        if (draft != null) {
            detail.setDraftVersion(toVersionVo(draft));
        }
        if (published != null) {
            detail.setPublishedVersion(toVersionVo(published));
        }
        detail.setVersions(listVersions(template.getId()));
        OaTenantSyncPreviewVO preview = buildSyncPreview(template);
        detail.setPlatformTemplateName(TenantScopeRunner.runIgnoringTenant(() -> {
            OaPlatformTemplateEntity platform = platformTemplateMapper.selectById(template.getPlatformTemplateId());
            return platform == null ? null : platform.getTemplateName();
        }));
        detail.setPlatformLatestVersionNo(preview.getPlatformLatestVersionNo());
        detail.setPlatformOutdated(Boolean.TRUE.equals(preview.getHasChanges()));
        return detail;
    }

    public OaTenantSyncPreviewVO previewSyncFromPlatform(String templateId) {
        return buildSyncPreview(accessSupport.requireById(templateId));
    }

    public List<OaTemplateVersionVO> listVersions(String templateId) {
        accessSupport.requireById(templateId);
        return tenantVersionMapper.selectByTemplateId(templateId).stream()
                .map(this::toVersionVo).toList();
    }

    public OaTemplateVersionVO getVersionDetail(String versionId) {
        OaTenantTemplateVersionEntity version = requireReadableVersion(versionId);
        return toVersionVoWithFields(version);
    }

    @Transactional(rollbackFor = Exception.class)
    public String createDraftVersion(OaVersionCreateRequest request) {
        OaTenantTemplateEntity template = accessSupport.requireById(request.getTemplateId());
        if (tenantVersionMapper.selectDraftByTemplateId(template.getId()) != null) {
            throw new BusinessException("error.oa.draft_version_exists");
        }
        OaPlatformTemplateVersionEntity platformPublished = requirePlatformPublishedForTenant(template);
        Integer max = tenantVersionMapper.selectMaxVersionNo(template.getId());
        int next = max == null ? 1 : max + 1;

        OaTenantTemplateVersionEntity published = tenantVersionMapper.selectPublishedByTemplateId(template.getId());
        // 从本租户已发布版本衍生草稿时，platform 绑定沿用已发布字段来源，避免“版本号已新、字段仍旧”
        OaPlatformTemplateVersionEntity bindPlatform = platformPublished;
        if (published != null && StringUtils.hasText(published.getPlatformTplVerId())) {
            OaPlatformTemplateVersionEntity fromPublished = TenantScopeRunner.runIgnoringTenant(
                    () -> platformVersionMapper.selectById(published.getPlatformTplVerId()));
            if (fromPublished != null) {
                bindPlatform = fromPublished;
            }
        }
        OaTenantTemplateVersionEntity version = newTenantVersion(template, next, bindPlatform, request.getRemark());
        if (published != null) {
            version.setLayoutJson(published.getLayoutJson());
        } else {
            version.setLayoutJson(platformPublished.getLayoutJson());
        }
        tenantVersionMapper.insert(version);

        if (published != null) {
            copyTenantFields(published.getId(), version.getId(), template.getTenantId());
        } else {
            copyPlatformFieldsToTenant(platformPublished.getId(), version.getId(), template.getTenantId());
            template.setSourcePlatformTplVerId(platformPublished.getId());
            AuditFieldHelper.fillUpdate(template);
            tenantTemplateMapper.updateById(template);
        }
        return version.getId();
    }

    /**
     * 将平台最新已发布字段同步到租户草稿：平台字段覆写/新增，保留本租户自定义字段，覆盖项保留显示侧自定义。
     */
    @Transactional(rollbackFor = Exception.class)
    public String syncFromPlatform(String templateId) {
        OaTenantTemplateEntity template = accessSupport.requireById(templateId);
        OaTenantSyncPreviewVO preview = buildSyncPreview(template);
        if (!Boolean.TRUE.equals(preview.getHasChanges())) {
            throw new BusinessException("error.oa.platform_sync_no_changes");
        }
        OaPlatformTemplateVersionEntity platformPublished = requirePlatformPublishedForTenant(template);
        List<OaPlatformFieldDefEntity> platformFields = TenantScopeRunner.runIgnoringTenant(
                () -> platformFieldDefMapper.selectByVersionId(platformPublished.getId()));
        if (CollectionUtils.isEmpty(platformFields)) {
            throw new BusinessException("error.oa.platform_fields_empty");
        }

        OaTenantTemplateVersionEntity draft = tenantVersionMapper.selectDraftByTemplateId(template.getId());
        List<OaTenantFieldDefEntity> previousFields;
        if (draft == null) {
            Integer max = tenantVersionMapper.selectMaxVersionNo(template.getId());
            int next = max == null ? 1 : max + 1;
            draft = newTenantVersion(template, next, platformPublished, "同步平台最新版本");
            draft.setLayoutJson(platformPublished.getLayoutJson());
            tenantVersionMapper.insert(draft);
            OaTenantTemplateVersionEntity published = tenantVersionMapper.selectPublishedByTemplateId(template.getId());
            previousFields = published == null
                    ? List.of()
                    : tenantFieldDefMapper.selectByVersionId(published.getId());
        } else {
            previousFields = tenantFieldDefMapper.selectByVersionId(draft.getId());
            draft.setPlatformTplVerId(platformPublished.getId());
            draft.setWfModelId(platformPublished.getWfModelId());
            draft.setLayoutJson(platformPublished.getLayoutJson());
            draft.setRemark("同步平台最新版本");
            AuditFieldHelper.fillUpdate(draft);
            tenantVersionMapper.updateById(draft);
            tenantFieldDefMapper.deleteByVersionId(draft.getId());
        }

        List<OaTenantFieldDefEntity> merged = mergePlatformIntoTenantFields(
                platformFields, previousFields, template.getTenantId(), draft.getId());
        if (!CollectionUtils.isEmpty(merged)) {
            tenantFieldDefMapper.insertBatch(merged);
        }

        template.setSourcePlatformTplVerId(platformPublished.getId());
        AuditFieldHelper.fillUpdate(template);
        tenantTemplateMapper.updateById(template);
        return draft.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveFieldDefs(OaFieldDefBatchSaveRequest request) {
        OaTenantTemplateVersionEntity version = requireDraftVersion(request.getVersionId());
        OaTenantTemplateEntity template = accessSupport.requireById(version.getTenantTemplateId());
        OaFieldDefValidator.validateBatch(request.getFields());
        Map<String, OaPlatformFieldDefEntity> platformLocked = loadPlatformLockedFields(version.getPlatformTplVerId());
        validateTenantFieldChanges(request.getFields(), platformLocked);

        version.setLayoutJson(
                OaLayoutJsonValidator.validateAndNormalize(request.getLayoutJson(), request.getFields()));
        AuditFieldHelper.fillUpdate(version);
        tenantVersionMapper.updateById(version);

        tenantFieldDefMapper.deleteByVersionId(version.getId());
        if (CollectionUtils.isEmpty(request.getFields())) {
            return;
        }
        List<OaTenantFieldDefEntity> entities = new ArrayList<>();
        for (OaFieldDefItemRequest item : request.getFields()) {
            OaPlatformFieldDefEntity platformField = platformLocked.get(item.getFieldCode().trim());
            String originType = platformField == null
                    ? OaFieldOriginType.TENANT_CUSTOM
                    : (isOverride(platformField, item) ? OaFieldOriginType.TENANT_OVERRIDE : OaFieldOriginType.PLATFORM_COPY);
            if (platformField != null && YesNoEnum.isYes(platformField.getPlatformLocked())) {
                item.setFieldType(platformField.getFieldType());
                item.setPlatformLocked(CommonConstants.YES);
                originType = OaFieldOriginType.PLATFORM_COPY;
            }
            OaTenantFieldDefEntity entity = OaConvert.toTenantFieldEntity(
                    item, template.getTenantId(), version.getId(), originType,
                    platformField == null ? null : platformField.getId());
            entity.setId(IdGeneratorUtils.nextIdStr());
            AuditFieldHelper.fillInsert(entity);
            entities.add(entity);
        }
        tenantFieldDefMapper.insertBatch(entities);
    }

    public OaTenantPublishReadinessVO getPublishReadiness(String versionId) {
        OaTenantTemplateVersionEntity version = requireReadableVersion(versionId);
        OaTenantTemplateEntity template = accessSupport.requireById(version.getTenantTemplateId());
        return buildPublishReadiness(template, version, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publishVersion(OaVersionPublishRequest request) {
        OaTenantTemplateVersionEntity version = requireDraftVersion(request.getVersionId());
        OaTenantTemplateEntity template = accessSupport.requireById(version.getTenantTemplateId());
        // 发布沿用草稿自身绑定的平台字段版本；流程须本租户已同步并部署，勿隐式改绑平台模型

        OaTenantPublishReadinessVO readiness = buildPublishReadiness(template, version, true);
        if (!readiness.isReady()) {
            if (!readiness.isModelSynced()) {
                throw new BusinessException("error.workflow.model_not_synced");
            }
            if (!readiness.isModelDeployed()) {
                throw new BusinessException("error.workflow.model_not_deployed");
            }
            if (!readiness.isFieldsReady()) {
                throw new BusinessException("error.oa.publish_fields_empty");
            }
            if (!readiness.isApproversReady()) {
                throw new BusinessException("error.oa.publish_approver_incomplete");
            }
            throw new BusinessException("error.oa.publish_not_ready");
        }

        List<OaFieldDefVO> snapshot = tenantFieldDefMapper.selectByVersionId(version.getId()).stream()
                .map(OaConvert::toTenantFieldVo).toList();
        version.setFieldSnapshotJson(writeSnapshot(snapshot));
        // Snapshot must be reference-shape; re-normalize against published field codes
        List<OaFieldDefItemRequest> fieldItems = snapshot.stream().map(vo -> {
            OaFieldDefItemRequest item = new OaFieldDefItemRequest();
            item.setFieldCode(vo.getFieldCode());
            return item;
        }).toList();
        version.setLayoutSnapshotJson(
                OaLayoutJsonValidator.validateAndNormalize(version.getLayoutJson(), fieldItems));
        version.setWfModelId(readiness.getModelId());
        version.setWfModelVersionNo(readiness.getModelVersionNo());

        tenantVersionMapper.archivePublishedByTemplateId(template.getId());
        version.setVersionStatus(OaVersionStatus.PUBLISHED);
        version.setPublishedAt(LocalDateTime.now());
        AuditFieldHelper.fillUpdate(version);
        tenantVersionMapper.updateById(version);

        template.setLatestVersionNo(version.getVersionNo());
        template.setPublishedTplVerId(version.getId());
        template.setSourcePlatformTplVerId(version.getPlatformTplVerId());
        template.setEnableStatus(OaEnableStatus.ENABLED);
        AuditFieldHelper.fillUpdate(template);
        tenantTemplateMapper.updateById(template);
    }

    private OaTenantPublishReadinessVO buildPublishReadiness(OaTenantTemplateEntity template,
                                                              OaTenantTemplateVersionEntity version,
                                                              boolean throwOnResolve) {
        OaTenantPublishReadinessVO vo = new OaTenantPublishReadinessVO();
        List<OaFieldDefVO> fields = tenantFieldDefMapper.selectByVersionId(version.getId()).stream()
                .map(OaConvert::toTenantFieldVo).toList();
        vo.setFieldsReady(!CollectionUtils.isEmpty(fields));
        if (!vo.isFieldsReady()) {
            vo.getMessages().add("fields_empty");
        }

        String categoryCode = template.getCategoryCode();
        WfModelEntity model = null;
        try {
            model = workflowModelRegistry.requireActiveByBizType(template.getTenantId(), categoryCode);
        } catch (BusinessException ex) {
            String key = ex.getMessage();
            if ("error.workflow.model_not_synced".equals(key) || "error.workflow.model_not_found".equals(key)) {
                vo.setModelSynced(false);
                vo.getMessages().add("model_not_synced");
            } else if ("error.workflow.model_not_deployed".equals(key)) {
                // latestVersion 未设置，但仍可能有草稿模型行
                WfModelEntity partial = bindingPolicySupport.isTenantRequired(categoryCode)
                        ? workflowModelAdminSupport.findTenantByBizType(template.getTenantId(), categoryCode)
                        : workflowModelAdminSupport.findActiveByBizType(template.getTenantId(), categoryCode);
                if (partial != null) {
                    vo.setModelSynced(true);
                    vo.setModelId(partial.getId());
                    vo.setModelKey(partial.getModelKey());
                    vo.setModelVersionNo(partial.getLatestVersion());
                }
                vo.setModelDeployed(false);
                vo.getMessages().add("model_not_deployed");
            } else if (throwOnResolve) {
                throw ex;
            }
        }

        if (model != null) {
            vo.setModelSynced(true);
            vo.setModelId(model.getId());
            vo.setModelKey(model.getModelKey());
            vo.setModelVersionNo(model.getLatestVersion());
            WfModelVersionEntity published = workflowModelAdminSupport.findPublishedByModelId(model.getId());
            boolean deployed = published != null
                    && StringUtils.hasText(published.getDeploymentId())
                    && StringUtils.hasText(published.getProcessDefinitionId())
                    && StringUtils.hasText(published.getBpmnXml());
            vo.setModelDeployed(deployed);
            if (!deployed) {
                vo.getMessages().add("model_not_deployed");
            }
            // 审批人覆盖与是否已部署解耦：未部署时仍按 design 叶子校验，避免「未部署」连带假失败
            if (published != null) {
                List<String> taskKeys = extractApproverTaskKeys(published);
                vo.setUserTaskKeys(taskKeys);
                Set<String> covered = resolveCoveredTaskKeys(model.getId(), template.getTenantId());
                List<String> missing = new ArrayList<>();
                for (String key : taskKeys) {
                    if (!covered.contains(key)) {
                        missing.add(key);
                    }
                }
                vo.setMissingApproverTaskKeys(missing);
                vo.setApproversReady(missing.isEmpty());
                if (!missing.isEmpty()) {
                    vo.getMessages().add("approver_incomplete");
                }
            }
        }

        vo.setReady(vo.isFieldsReady() && vo.isModelSynced() && vo.isModelDeployed() && vo.isApproversReady());
        return vo;
    }

    private Set<String> resolveCoveredTaskKeys(String modelId, String bizTenantId) {
        List<String> tenantIds = new ArrayList<>();
        tenantIds.add(TenantConstants.PLATFORM_TENANT_ID);
        if (StringUtils.hasText(bizTenantId) && !TenantConstants.PLATFORM_TENANT_ID.equals(bizTenantId)) {
            tenantIds.add(bizTenantId);
        }
        List<WfApproverRuleEntity> rules = workflowModelAdminSupport.listApproverRules(modelId, tenantIds);
        Map<String, WfApproverRuleEntity> merged = new HashMap<>();
        if (!CollectionUtils.isEmpty(rules)) {
            for (WfApproverRuleEntity rule : rules) {
                if (TenantConstants.PLATFORM_TENANT_ID.equals(rule.getTenantId())) {
                    merged.putIfAbsent(rule.getTaskDefinitionKey(), rule);
                }
            }
            for (WfApproverRuleEntity rule : rules) {
                if (bizTenantId != null && bizTenantId.equals(rule.getTenantId())) {
                    merged.put(rule.getTaskDefinitionKey(), rule);
                }
            }
        }
        return new HashSet<>(merged.keySet());
    }

    /** 优先 design_json 叶子节点；无则退回 BPMN userTask 扫描（逃生舱） */
    private List<String> extractApproverTaskKeys(WfModelVersionEntity published) {
        if (published == null) {
            return List.of();
        }
        List<WorkflowDesignSoTSupport.DesignLeafTask> leaves =
                designSoTSupport.collectApproverLeafTasks(published.getDesignJson());
        if (!CollectionUtils.isEmpty(leaves)) {
            LinkedHashSet<String> keys = new LinkedHashSet<>();
            for (WorkflowDesignSoTSupport.DesignLeafTask leaf : leaves) {
                keys.add(leaf.taskDefinitionKey());
            }
            return new ArrayList<>(keys);
        }
        return extractUserTaskKeysFromBpmn(published.getBpmnXml());
    }

    private List<String> extractUserTaskKeysFromBpmn(String bpmnXml) {
        if (!StringUtils.hasText(bpmnXml)) {
            return List.of();
        }
        Set<String> keys = new LinkedHashSet<>();
        int from = 0;
        String lower = bpmnXml.toLowerCase();
        while (true) {
            int tag = lower.indexOf("<usertask", from);
            if (tag < 0) {
                break;
            }
            int idAttr = lower.indexOf("id=", tag);
            int tagEnd = bpmnXml.indexOf('>', tag);
            if (idAttr < 0 || tagEnd < 0 || idAttr > tagEnd) {
                from = tag + 8;
                continue;
            }
            int q = idAttr + 3;
            while (q < tagEnd && (bpmnXml.charAt(q) == ' ' || bpmnXml.charAt(q) == '\t')) {
                q++;
            }
            if (q >= tagEnd) {
                from = tag + 8;
                continue;
            }
            char quote = bpmnXml.charAt(q);
            if (quote != '"' && quote != '\'') {
                from = tag + 8;
                continue;
            }
            int end = bpmnXml.indexOf(quote, q + 1);
            if (end > q) {
                String id = bpmnXml.substring(q + 1, end).trim();
                if (StringUtils.hasText(id)) {
                    keys.add(id);
                }
            }
            from = tag + 8;
        }
        return new ArrayList<>(keys);
    }

    private OaTenantTemplateVersionEntity requireDraftVersion(String versionId) {
        OaTenantTemplateVersionEntity version = tenantVersionMapper.selectById(versionId);
        if (version == null || !OaVersionStatus.DRAFT.equals(version.getVersionStatus())) {
            throw new BusinessException("error.oa.version_not_draft");
        }
        accessSupport.requireById(version.getTenantTemplateId());
        return version;
    }

    private OaTenantTemplateVersionEntity requireReadableVersion(String versionId) {
        OaTenantTemplateVersionEntity version = tenantVersionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException("error.oa.version_not_found");
        }
        accessSupport.requireById(version.getTenantTemplateId());
        return version;
    }

    private OaPlatformTemplateVersionEntity requirePlatformPublishedForTenant(OaTenantTemplateEntity template) {
        OaPlatformTemplateVersionEntity platformPublished = TenantScopeRunner.runIgnoringTenant(
                () -> platformVersionMapper.selectPublishedByTemplateId(template.getPlatformTemplateId()));
        if (platformPublished == null) {
            throw new BusinessException("error.oa.platform_not_published");
        }
        return platformPublished;
    }

    private OaTenantSyncPreviewVO buildSyncPreview(OaTenantTemplateEntity template) {
        OaTenantSyncPreviewVO preview = new OaTenantSyncPreviewVO();
        preview.setTemplateId(template.getId());
        preview.setTemplateName(template.getTemplateName());
        preview.setCurrentSourcePlatformTplVerId(template.getSourcePlatformTplVerId());

        OaPlatformTemplateEntity platform = TenantScopeRunner.runIgnoringTenant(
                () -> platformTemplateMapper.selectById(template.getPlatformTemplateId()));
        OaPlatformTemplateVersionEntity platformPublished = TenantScopeRunner.runIgnoringTenant(
                () -> platformVersionMapper.selectPublishedByTemplateId(template.getPlatformTemplateId()));
        if (platform != null) {
            preview.setPlatformLatestVersionNo(platform.getLatestVersionNo());
        }
        if (platformPublished == null) {
            preview.setHasChanges(false);
            preview.setAddedCount(0);
            preview.setRemovedCount(0);
            preview.setChangedCount(0);
            preview.setKeptCustomCount(0);
            return preview;
        }
        preview.setPlatformPublishedVersionId(platformPublished.getId());

        OaTenantTemplateVersionEntity draft = tenantVersionMapper.selectDraftByTemplateId(template.getId());
        OaTenantTemplateVersionEntity published = tenantVersionMapper.selectPublishedByTemplateId(template.getId());
        OaTenantTemplateVersionEntity baseVersion = draft != null ? draft : published;
        List<OaTenantFieldDefEntity> currentFields = baseVersion == null
                ? List.of()
                : tenantFieldDefMapper.selectByVersionId(baseVersion.getId());
        if (baseVersion != null) {
            preview.setCurrentTenantVersionNo(baseVersion.getVersionNo());
            preview.setCurrentTenantVersionStatus(baseVersion.getVersionStatus());
        }

        List<OaPlatformFieldDefEntity> platformFields = TenantScopeRunner.runIgnoringTenant(
                () -> platformFieldDefMapper.selectByVersionId(platformPublished.getId()));
        if (platformFields == null) {
            platformFields = List.of();
        }

        Map<String, OaTenantFieldDefEntity> currentByCode = new HashMap<>();
        for (OaTenantFieldDefEntity field : currentFields) {
            currentByCode.put(field.getFieldCode(), field);
        }

        int added = 0;
        int removed = 0;
        int changed = 0;
        int keptCustom = 0;
        List<OaTenantFieldSyncDiffVO> diffs = new ArrayList<>();

        for (OaPlatformFieldDefEntity platformField : platformFields) {
            OaTenantFieldDefEntity current = currentByCode.remove(platformField.getFieldCode());
            if (current == null) {
                OaTenantFieldSyncDiffVO diff = new OaTenantFieldSyncDiffVO();
                diff.setChangeType(OaFieldSyncChangeType.ADDED);
                diff.setFieldCode(platformField.getFieldCode());
                diff.setFieldName(platformField.getFieldName());
                diff.setCurrentSummary(null);
                diff.setAfterSummary(summarizePlatformField(platformField));
                diffs.add(diff);
                added++;
                continue;
            }
            if (isPlatformContractChanged(platformField, current)) {
                OaTenantFieldSyncDiffVO diff = new OaTenantFieldSyncDiffVO();
                diff.setChangeType(OaFieldSyncChangeType.CHANGED);
                diff.setFieldCode(platformField.getFieldCode());
                diff.setFieldName(platformField.getFieldName());
                diff.setCurrentSummary(summarizeTenantField(current));
                diff.setAfterSummary(summarizeMergedAfter(platformField, current));
                diffs.add(diff);
                changed++;
            }
        }

        for (OaTenantFieldDefEntity leftover : currentByCode.values()) {
            OaTenantFieldSyncDiffVO diff = new OaTenantFieldSyncDiffVO();
            diff.setFieldCode(leftover.getFieldCode());
            diff.setFieldName(leftover.getFieldName());
            diff.setCurrentSummary(summarizeTenantField(leftover));
            if (OaFieldOriginType.TENANT_CUSTOM.equals(leftover.getOriginType())) {
                diff.setChangeType(OaFieldSyncChangeType.KEPT_CUSTOM);
                diff.setAfterSummary(diff.getCurrentSummary());
                diffs.add(diff);
                keptCustom++;
            } else {
                diff.setChangeType(OaFieldSyncChangeType.REMOVED);
                diff.setAfterSummary(null);
                diffs.add(diff);
                removed++;
            }
        }

        preview.setDiffs(diffs);
        preview.setAddedCount(added);
        preview.setRemovedCount(removed);
        preview.setChangedCount(changed);
        preview.setKeptCustomCount(keptCustom);
        preview.setHasChanges(added + removed + changed > 0);
        return preview;
    }

    private boolean isPlatformContractChanged(OaPlatformFieldDefEntity platform, OaTenantFieldDefEntity tenant) {
        // 类型/组件/字典/锁定/槽位/用途：始终按平台契约比对
        if (!eqIgnoreCase(platform.getFieldType(), tenant.getFieldType())
                || !eq(platform.getComponentType(), tenant.getComponentType())
                || !eq(platform.getDictType(), tenant.getDictType())
                || !eq(platform.getPlatformLocked(), tenant.getPlatformLocked())
                || !eq(platform.getUsedInCondition(), tenant.getUsedInCondition())
                || !eq(platform.getUsedInList(), tenant.getUsedInList())
                || !eq(platform.getPromoteSlot(), tenant.getPromoteSlot())
                || !eq(platform.getListSlotSync(), tenant.getListSlotSync())) {
            return true;
        }
        // 名称/必填/默认值：仅在平台锁定或租户未覆盖时要求对齐
        boolean alignDisplay = YesNoEnum.isYes(platform.getPlatformLocked())
                || !OaFieldOriginType.TENANT_OVERRIDE.equals(tenant.getOriginType());
        if (!alignDisplay) {
            return false;
        }
        return !eq(platform.getFieldName(), tenant.getFieldName())
                || !eq(platform.getFieldRequired(), tenant.getFieldRequired())
                || !eq(platform.getDefaultValue(), tenant.getDefaultValue())
                || !eq(platform.getMaxLength() == null ? null : String.valueOf(platform.getMaxLength()),
                tenant.getMaxLength() == null ? null : String.valueOf(tenant.getMaxLength()));
    }

    private String summarizeMergedAfter(OaPlatformFieldDefEntity platform, OaTenantFieldDefEntity current) {
        // 与 merge 规则一致：锁定字段用平台；未锁定且租户有展示侧覆盖时展示保留后的结果
        if (YesNoEnum.isYes(platform.getPlatformLocked())) {
            return summarizePlatformField(platform);
        }
        if (OaFieldOriginType.TENANT_OVERRIDE.equals(current.getOriginType())
                || isTenantDisplayOverride(platform, current)) {
            return "name=" + current.getFieldName()
                    + ", type=" + platform.getFieldType()
                    + ", component=" + nullToDash(platform.getComponentType())
                    + ", dict=" + nullToDash(platform.getDictType())
                    + ", required=" + nullToDash(current.getFieldRequired())
                    + ", slot=" + nullToDash(current.getPromoteSlot())
                    + ", locked=" + nullToDash(platform.getPlatformLocked())
                    + " (保留租户覆盖)";
        }
        return summarizePlatformField(platform);
    }

    private String summarizePlatformField(OaPlatformFieldDefEntity field) {
        return "name=" + field.getFieldName()
                + ", type=" + field.getFieldType()
                + ", component=" + nullToDash(field.getComponentType())
                + ", dict=" + nullToDash(field.getDictType())
                + ", required=" + nullToDash(field.getFieldRequired())
                + ", slot=" + nullToDash(field.getPromoteSlot())
                + ", locked=" + nullToDash(field.getPlatformLocked());
    }

    private String summarizeTenantField(OaTenantFieldDefEntity field) {
        return "name=" + field.getFieldName()
                + ", type=" + field.getFieldType()
                + ", component=" + nullToDash(field.getComponentType())
                + ", dict=" + nullToDash(field.getDictType())
                + ", required=" + nullToDash(field.getFieldRequired())
                + ", slot=" + nullToDash(field.getPromoteSlot())
                + ", locked=" + nullToDash(field.getPlatformLocked())
                + ", origin=" + nullToDash(field.getOriginType());
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private boolean eqIgnoreCase(String a, String b) {
        if (a == null) {
            return b == null || b.isBlank();
        }
        if (b == null) {
            return a.isBlank();
        }
        return a.equalsIgnoreCase(b);
    }

    private OaTenantTemplateVersionEntity newTenantVersion(
            OaTenantTemplateEntity template, int versionNo,
            OaPlatformTemplateVersionEntity platformPublished, String remark) {
        OaTenantTemplateVersionEntity version = new OaTenantTemplateVersionEntity();
        version.setId(IdGeneratorUtils.nextIdStr());
        version.setTenantId(template.getTenantId());
        version.setTenantTemplateId(template.getId());
        version.setVersionNo(versionNo);
        version.setVersionStatus(OaVersionStatus.DRAFT);
        version.setPlatformTplVerId(platformPublished.getId());
        version.setWfModelId(platformPublished.getWfModelId());
        version.setRemark(remark);
        AuditFieldHelper.fillInsert(version);
        return version;
    }

    private void copyPlatformFieldsToTenant(String platformVersionId, String tenantVersionId, String tenantId) {
        List<OaPlatformFieldDefEntity> fields = TenantScopeRunner.runIgnoringTenant(
                () -> platformFieldDefMapper.selectByVersionId(platformVersionId));
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        List<OaTenantFieldDefEntity> copies = new ArrayList<>();
        for (OaPlatformFieldDefEntity field : fields) {
            OaTenantFieldDefEntity copy = OaConvert.copyFromPlatform(field, tenantId, tenantVersionId);
            copy.setId(IdGeneratorUtils.nextIdStr());
            AuditFieldHelper.fillInsert(copy);
            copies.add(copy);
        }
        tenantFieldDefMapper.insertBatch(copies);
    }

    private List<OaTenantFieldDefEntity> mergePlatformIntoTenantFields(
            List<OaPlatformFieldDefEntity> platformFields,
            List<OaTenantFieldDefEntity> previousFields,
            String tenantId,
            String tenantVersionId) {
        Map<String, OaTenantFieldDefEntity> previousByCode = new HashMap<>();
        if (previousFields != null) {
            for (OaTenantFieldDefEntity prev : previousFields) {
                previousByCode.put(prev.getFieldCode(), prev);
            }
        }
        List<OaTenantFieldDefEntity> merged = new ArrayList<>();
        for (OaPlatformFieldDefEntity platform : platformFields) {
            OaTenantFieldDefEntity copy = OaConvert.copyFromPlatform(platform, tenantId, tenantVersionId);
            OaTenantFieldDefEntity prev = previousByCode.remove(platform.getFieldCode());
            if (prev != null && !YesNoEnum.isYes(platform.getPlatformLocked())
                    && (OaFieldOriginType.TENANT_OVERRIDE.equals(prev.getOriginType())
                    || isTenantDisplayOverride(platform, prev))) {
                // 保留租户对展示侧的自定义（名称/必填/默认值/槽位等）
                copy.setFieldName(prev.getFieldName());
                copy.setFieldAlias(prev.getFieldAlias());
                copy.setFieldRequired(prev.getFieldRequired());
                copy.setDefaultValue(prev.getDefaultValue());
                copy.setMaxLength(prev.getMaxLength());
                copy.setPromoteSlot(prev.getPromoteSlot());
                copy.setListSlotSync(prev.getListSlotSync());
                copy.setUsedInCondition(prev.getUsedInCondition());
                copy.setUsedInList(prev.getUsedInList());
                copy.setSortNo(prev.getSortNo());
                copy.setRemark(prev.getRemark());
                copy.setOriginType(OaFieldOriginType.TENANT_OVERRIDE);
            }
            copy.setId(IdGeneratorUtils.nextIdStr());
            AuditFieldHelper.fillInsert(copy);
            merged.add(copy);
        }
        // 平台已删除的字段不再保留；仅保留纯租户自定义字段
        for (OaTenantFieldDefEntity prev : previousByCode.values()) {
            if (!OaFieldOriginType.TENANT_CUSTOM.equals(prev.getOriginType())) {
                continue;
            }
            OaTenantFieldDefEntity custom = OaConvert.toTenantFieldEntity(
                    fieldToItem(prev), tenantId, tenantVersionId,
                    OaFieldOriginType.TENANT_CUSTOM, null);
            custom.setId(IdGeneratorUtils.nextIdStr());
            AuditFieldHelper.fillInsert(custom);
            merged.add(custom);
        }
        return merged;
    }

    private boolean isTenantDisplayOverride(OaPlatformFieldDefEntity platform, OaTenantFieldDefEntity prev) {
        return !platform.getFieldName().equals(prev.getFieldName())
                || !eq(platform.getFieldAlias(), prev.getFieldAlias())
                || !eq(platform.getFieldRequired(), prev.getFieldRequired())
                || !eq(platform.getDefaultValue(), prev.getDefaultValue())
                || !eq(platform.getPromoteSlot(), prev.getPromoteSlot())
                || !eq(platform.getListSlotSync(), prev.getListSlotSync());
    }

    private void copyTenantFields(String fromVersionId, String toVersionId, String tenantId) {
        List<OaTenantFieldDefEntity> fields = tenantFieldDefMapper.selectByVersionId(fromVersionId);
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        List<OaTenantFieldDefEntity> copies = new ArrayList<>();
        for (OaTenantFieldDefEntity field : fields) {
            OaTenantFieldDefEntity copy = OaConvert.toTenantFieldEntity(
                    fieldToItem(field), tenantId, toVersionId, field.getOriginType(), field.getSourcePlatformFieldId());
            copy.setId(IdGeneratorUtils.nextIdStr());
            AuditFieldHelper.fillInsert(copy);
            copies.add(copy);
        }
        tenantFieldDefMapper.insertBatch(copies);
    }

    private Map<String, OaPlatformFieldDefEntity> loadPlatformLockedFields(String platformVersionId) {
        List<OaPlatformFieldDefEntity> fields = TenantScopeRunner.runIgnoringTenant(
                () -> platformFieldDefMapper.selectByVersionId(platformVersionId));
        Map<String, OaPlatformFieldDefEntity> map = new HashMap<>();
        if (fields != null) {
            for (OaPlatformFieldDefEntity field : fields) {
                map.put(field.getFieldCode(), field);
            }
        }
        return map;
    }

    private void validateTenantFieldChanges(List<OaFieldDefItemRequest> fields, Map<String, OaPlatformFieldDefEntity> platformFields) {
        for (OaFieldDefItemRequest item : fields) {
            OaPlatformFieldDefEntity platform = platformFields.get(item.getFieldCode().trim());
            if (platform != null && YesNoEnum.isYes(platform.getPlatformLocked())) {
                if (!platform.getFieldType().equalsIgnoreCase(item.getFieldType())) {
                    throw new BusinessException("error.oa.platform_locked_field_type", item.getFieldCode());
                }
            }
        }
    }

    private boolean isOverride(OaPlatformFieldDefEntity platform, OaFieldDefItemRequest item) {
        return !platform.getFieldName().equals(item.getFieldName())
                || !eq(platform.getFieldAlias(), item.getFieldAlias())
                || !eq(platform.getFieldRequired(), item.getFieldRequired())
                || !eq(platform.getFieldGroup(), item.getFieldGroup())
                || !java.util.Objects.equals(platform.getColSpan(), item.getColSpan())
                || !eq(platform.getDefaultValue(), item.getDefaultValue())
                || !eq(platform.getPromoteSlot(), item.getPromoteSlot())
                || !eq(platform.getListSlotSync(), item.getListSlotSync());
    }

    private boolean eq(String a, String b) {
        if (a == null) {
            return b == null || b.isBlank();
        }
        return a.equals(b);
    }

    private OaFieldDefItemRequest fieldToItem(OaTenantFieldDefEntity field) {
        var item = new OaFieldDefItemRequest();
        item.setFieldCode(field.getFieldCode());
        item.setFieldName(field.getFieldName());
        item.setFieldAlias(field.getFieldAlias());
        item.setFieldType(field.getFieldType());
        item.setComponentType(field.getComponentType());
        item.setDictType(field.getDictType());
        item.setFieldRequired(field.getFieldRequired());
        item.setSortNo(field.getSortNo());
        item.setFieldGroup(field.getFieldGroup());
        item.setColSpan(field.getColSpan());
        item.setDefaultValue(field.getDefaultValue());
        item.setMaxLength(field.getMaxLength());
        item.setPlatformLocked(field.getPlatformLocked());
        item.setUsedInCondition(field.getUsedInCondition());
        item.setUsedInList(field.getUsedInList());
        item.setPromoteSlot(field.getPromoteSlot());
        item.setListSlotSync(field.getListSlotSync());
        item.setStatus(field.getStatus());
        item.setRemark(field.getRemark());
        return item;
    }

    private String writeSnapshot(List<OaFieldDefVO> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("error.oa.snapshot_failed");
        }
    }

    private OaTemplateVersionVO toVersionVo(OaTenantTemplateVersionEntity entity) {
        return OaConvert.toTenantVersionVo(entity, null);
    }

    private OaTemplateVersionVO toVersionVoWithFields(OaTenantTemplateVersionEntity entity) {
        List<OaFieldDefVO> fields = tenantFieldDefMapper.selectByVersionId(entity.getId()).stream()
                .map(OaConvert::toTenantFieldVo).toList();
        return OaConvert.toTenantVersionVo(entity, fields);
    }
}
