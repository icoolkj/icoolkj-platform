package com.icoolkj.platform.oa.support;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.oa.constant.OaVersionStatus;
import com.icoolkj.platform.oa.convert.OaConvert;
import com.icoolkj.platform.oa.convert.OaPlatformTemplateConvert;
import com.icoolkj.platform.oa.domain.OaPlatformFieldDefEntity;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateEntity;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateVersionEntity;
import com.icoolkj.platform.oa.dto.OaFieldDefBatchSaveRequest;
import com.icoolkj.platform.oa.dto.OaFieldDefItemRequest;
import com.icoolkj.platform.oa.dto.OaVersionCreateRequest;
import com.icoolkj.platform.oa.dto.OaVersionPublishRequest;
import com.icoolkj.platform.oa.mapper.OaPlatformFieldDefMapper;
import com.icoolkj.platform.oa.mapper.OaPlatformTemplateMapper;
import com.icoolkj.platform.oa.mapper.OaPlatformTemplateVersionMapper;
import com.icoolkj.platform.oa.vo.OaFieldDefVO;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateDetailVO;
import com.icoolkj.platform.oa.vo.OaTemplateVersionVO;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.support.WorkflowModelAdminSupport;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** OA 平台模板版本与字段扩展（手写，不随 Velocity 覆盖） */
@Component
public class OaPlatformTemplateVersionSupport {

    private final OaPlatformTemplateMapper templateMapper;
    private final OaPlatformTemplateVersionMapper versionMapper;
    private final OaPlatformFieldDefMapper fieldDefMapper;
    private final WorkflowModelAdminSupport workflowModelAdminSupport;
    private final OaPlatformTemplateAccessSupport accessSupport;

    public OaPlatformTemplateVersionSupport(OaPlatformTemplateMapper templateMapper,
                                              OaPlatformTemplateVersionMapper versionMapper,
                                              OaPlatformFieldDefMapper fieldDefMapper,
                                              WorkflowModelAdminSupport workflowModelAdminSupport,
                                              OaPlatformTemplateAccessSupport accessSupport) {
        this.templateMapper = templateMapper;
        this.versionMapper = versionMapper;
        this.fieldDefMapper = fieldDefMapper;
        this.workflowModelAdminSupport = workflowModelAdminSupport;
        this.accessSupport = accessSupport;
    }

    public OaPlatformTemplateDetailVO buildDetail(OaPlatformTemplateEntity template) {
        OaPlatformTemplateDetailVO detail = OaPlatformTemplateConvert.toDetailVo(
                OaPlatformTemplateConvert.toVo(template));
        OaPlatformTemplateVersionEntity draft = versionMapper.selectDraftByTemplateId(template.getId());
        OaPlatformTemplateVersionEntity published = versionMapper.selectPublishedByTemplateId(template.getId());
        if (draft != null) {
            detail.setDraftVersion(toVersionVo(draft));
        }
        if (published != null) {
            detail.setPublishedVersion(toVersionVo(published));
        }
        detail.setVersions(listVersions(template.getId()));
        return detail;
    }

    public List<OaTemplateVersionVO> listVersions(String templateId) {
        accessSupport.requireById(templateId);
        return versionMapper.selectByTemplateId(templateId).stream()
                .map(this::toVersionVo).toList();
    }

    public OaTemplateVersionVO getVersionDetail(String versionId) {
        OaPlatformTemplateVersionEntity version = requireReadableVersion(versionId);
        return toVersionVoWithFields(version);
    }

    @Transactional(rollbackFor = Exception.class)
    public String createDraftVersion(OaVersionCreateRequest request) {
        OaPlatformTemplateEntity template = accessSupport.requireById(request.getTemplateId());
        if (versionMapper.selectDraftByTemplateId(template.getId()) != null) {
            throw new BusinessException("error.oa.draft_version_exists");
        }
        Integer max = versionMapper.selectMaxVersionNo(template.getId());
        int next = max == null ? 1 : max + 1;
        OaPlatformTemplateVersionEntity version = newVersion(template, next, request.getWfModelId(), request.getRemark());
        OaPlatformTemplateVersionEntity published = versionMapper.selectPublishedByTemplateId(template.getId());
        if (published != null) {
            version.setLayoutJson(published.getLayoutJson());
        }
        versionMapper.insert(version);

        if (published != null) {
            copyFields(published.getId(), version.getId());
        }
        return version.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveFieldDefs(OaFieldDefBatchSaveRequest request) {
        OaPlatformTemplateVersionEntity version = requireDraftVersion(request.getVersionId());
        OaFieldDefValidator.validateBatch(request.getFields());
        version.setLayoutJson(
                OaLayoutJsonValidator.validateAndNormalize(request.getLayoutJson(), request.getFields()));
        AuditFieldHelper.fillUpdate(version);
        versionMapper.updateById(version);

        fieldDefMapper.deleteByVersionId(version.getId());
        if (CollectionUtils.isEmpty(request.getFields())) {
            return;
        }
        String tenantId = OaTenantSupport.platformTenantId();
        List<OaPlatformFieldDefEntity> entities = new ArrayList<>();
        for (var item : request.getFields()) {
            OaPlatformFieldDefEntity entity = OaConvert.toPlatformFieldEntity(item, tenantId, version.getId());
            entity.setId(IdGeneratorUtils.nextIdStr());
            AuditFieldHelper.fillInsert(entity);
            entities.add(entity);
        }
        fieldDefMapper.insertBatch(entities);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publishVersion(OaVersionPublishRequest request) {
        OaPlatformTemplateVersionEntity version = requireDraftVersion(request.getVersionId());
        OaPlatformTemplateEntity template = accessSupport.requireById(version.getTemplateId());
        String wfModelId = StringUtils.hasText(version.getWfModelId()) ? version.getWfModelId() : template.getWfModelId();
        Integer wfModelVersionNo = null;
        if (StringUtils.hasText(wfModelId)) {
            WfModelEntity wfModel = workflowModelAdminSupport.requireDeployedById(wfModelId);
            wfModelVersionNo = wfModel.getLatestVersion();
            version.setWfModelId(wfModelId);
        }
        versionMapper.archivePublishedByTemplateId(template.getId());
        version.setVersionStatus(OaVersionStatus.PUBLISHED);
        version.setWfModelVersionNo(wfModelVersionNo);
        version.setPublishedAt(LocalDateTime.now());
        AuditFieldHelper.fillUpdate(version);
        versionMapper.updateById(version);

        template.setLatestVersionNo(version.getVersionNo());
        if (StringUtils.hasText(wfModelId)) {
            template.setWfModelId(wfModelId);
        }
        AuditFieldHelper.fillUpdate(template);
        templateMapper.updateById(template);
    }

    private OaPlatformTemplateVersionEntity requireDraftVersion(String versionId) {
        OaPlatformTemplateVersionEntity version = versionMapper.selectById(versionId);
        if (version == null || !OaVersionStatus.DRAFT.equals(version.getVersionStatus())) {
            throw new BusinessException("error.oa.version_not_draft");
        }
        accessSupport.requireById(version.getTemplateId());
        return version;
    }

    private OaPlatformTemplateVersionEntity requireReadableVersion(String versionId) {
        OaPlatformTemplateVersionEntity version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException("error.oa.version_not_found");
        }
        accessSupport.requireById(version.getTemplateId());
        return version;
    }

    private OaPlatformTemplateVersionEntity newVersion(
            OaPlatformTemplateEntity template, int versionNo, String wfModelId, String remark) {
        OaPlatformTemplateVersionEntity version = new OaPlatformTemplateVersionEntity();
        version.setId(IdGeneratorUtils.nextIdStr());
        version.setTenantId(OaTenantSupport.platformTenantId());
        version.setTemplateId(template.getId());
        version.setVersionNo(versionNo);
        version.setVersionStatus(OaVersionStatus.DRAFT);
        version.setWfModelId(StringUtils.hasText(wfModelId) ? wfModelId : template.getWfModelId());
        version.setRemark(remark);
        AuditFieldHelper.fillInsert(version);
        return version;
    }

    private void copyFields(String fromVersionId, String toVersionId) {
        List<OaPlatformFieldDefEntity> fields = fieldDefMapper.selectByVersionId(fromVersionId);
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        String tenantId = OaTenantSupport.platformTenantId();
        List<OaPlatformFieldDefEntity> copies = new ArrayList<>();
        for (OaPlatformFieldDefEntity field : fields) {
            OaPlatformFieldDefEntity copy = OaConvert.toPlatformFieldEntity(
                    fieldToItem(field), tenantId, toVersionId);
            copy.setId(IdGeneratorUtils.nextIdStr());
            AuditFieldHelper.fillInsert(copy);
            copies.add(copy);
        }
        fieldDefMapper.insertBatch(copies);
    }

    private OaFieldDefItemRequest fieldToItem(OaPlatformFieldDefEntity field) {
        var item = new OaFieldDefItemRequest();
        item.setFieldCode(field.getFieldCode());
        item.setFieldName(field.getFieldName());
        item.setFieldAlias(field.getFieldAlias());
        item.setFieldType(field.getFieldType());
        item.setComponentType(field.getComponentType());
        item.setDictType(field.getDictType());
        item.setFieldRequired(field.getFieldRequired());
        item.setSortNo(field.getSortNo());
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

    private OaTemplateVersionVO toVersionVo(OaPlatformTemplateVersionEntity entity) {
        return OaConvert.toPlatformVersionVo(entity, null);
    }

    private OaTemplateVersionVO toVersionVoWithFields(OaPlatformTemplateVersionEntity entity) {
        List<OaFieldDefVO> fields = fieldDefMapper.selectByVersionId(entity.getId()).stream()
                .map(OaConvert::toPlatformFieldVo).toList();
        return OaConvert.toPlatformVersionVo(entity, fields);
    }
}
