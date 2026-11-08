package com.icoolkj.platform.oa.convert;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.oa.domain.OaPlatformFieldDefEntity;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateVersionEntity;
import com.icoolkj.platform.oa.domain.OaTenantFieldDefEntity;
import com.icoolkj.platform.oa.domain.OaTenantTemplateVersionEntity;
import com.icoolkj.platform.oa.dto.OaFieldDefItemRequest;
import com.icoolkj.platform.oa.vo.OaFieldDefVO;
import com.icoolkj.platform.oa.vo.OaTemplateVersionVO;
import java.util.List;

/** OA 版本/字段对象转换（手写扩展，不随 Velocity 覆盖） */
public final class OaConvert {

    private OaConvert() {
    }

    public static OaTemplateVersionVO toPlatformVersionVo(OaPlatformTemplateVersionEntity entity, List<OaFieldDefVO> fields) {
        OaTemplateVersionVO vo = new OaTemplateVersionVO();
        vo.setId(entity.getId());
        vo.setTemplateId(entity.getTemplateId());
        vo.setVersionNo(entity.getVersionNo());
        vo.setVersionStatus(entity.getVersionStatus());
        vo.setWfModelId(entity.getWfModelId());
        vo.setWfModelVersionNo(entity.getWfModelVersionNo());
        vo.setPublishedAt(entity.getPublishedAt());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setFields(fields);
        vo.setLayoutJson(entity.getLayoutJson());
        return vo;
    }

    public static OaTemplateVersionVO toTenantVersionVo(OaTenantTemplateVersionEntity entity, List<OaFieldDefVO> fields) {
        OaTemplateVersionVO vo = new OaTemplateVersionVO();
        vo.setId(entity.getId());
        vo.setTemplateId(entity.getTenantTemplateId());
        vo.setVersionNo(entity.getVersionNo());
        vo.setVersionStatus(entity.getVersionStatus());
        vo.setWfModelId(entity.getWfModelId());
        vo.setWfModelVersionNo(entity.getWfModelVersionNo());
        vo.setPlatformTplVerId(entity.getPlatformTplVerId());
        vo.setPublishedAt(entity.getPublishedAt());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setFields(fields);
        vo.setLayoutJson(entity.getLayoutJson());
        vo.setLayoutSnapshotJson(entity.getLayoutSnapshotJson());
        return vo;
    }

    public static OaFieldDefVO toPlatformFieldVo(OaPlatformFieldDefEntity entity) {
        OaFieldDefVO vo = new OaFieldDefVO();
        vo.setId(entity.getId());
        vo.setFieldCode(entity.getFieldCode());
        vo.setFieldName(entity.getFieldName());
        vo.setFieldAlias(entity.getFieldAlias());
        vo.setFieldType(entity.getFieldType());
        vo.setComponentType(entity.getComponentType());
        vo.setDictType(entity.getDictType());
        vo.setFieldRequired(entity.getFieldRequired());
        vo.setSortNo(entity.getSortNo());
        vo.setFieldGroup(entity.getFieldGroup());
        vo.setColSpan(entity.getColSpan());
        vo.setDefaultValue(entity.getDefaultValue());
        vo.setMaxLength(entity.getMaxLength());
        vo.setPlatformLocked(entity.getPlatformLocked());
        vo.setUsedInCondition(entity.getUsedInCondition());
        vo.setUsedInList(entity.getUsedInList());
        vo.setPromoteSlot(entity.getPromoteSlot());
        vo.setListSlotSync(entity.getListSlotSync());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    public static OaFieldDefVO toTenantFieldVo(OaTenantFieldDefEntity entity) {
        OaFieldDefVO vo = toPlatformFieldVo(copyToPlatform(entity));
        vo.setOriginType(entity.getOriginType());
        vo.setSourcePlatformFieldId(entity.getSourcePlatformFieldId());
        return vo;
    }

    public static OaPlatformFieldDefEntity toPlatformFieldEntity(
            OaFieldDefItemRequest item, String tenantId, String versionId) {
        OaPlatformFieldDefEntity entity = new OaPlatformFieldDefEntity();
        entity.setTenantId(tenantId);
        entity.setTemplateVersionId(versionId);
        entity.setFieldCode(item.getFieldCode().trim());
        entity.setFieldName(item.getFieldName().trim());
        entity.setFieldAlias(item.getFieldAlias());
        entity.setFieldType(item.getFieldType().trim());
        entity.setComponentType(item.getComponentType());
        entity.setDictType(item.getDictType());
        entity.setFieldRequired(defaultYesNo(item.getFieldRequired()));
        entity.setSortNo(item.getSortNo() == null ? 0 : item.getSortNo());
        entity.setFieldGroup(item.getFieldGroup());
        entity.setColSpan(defaultColSpan(item.getColSpan()));
        entity.setDefaultValue(item.getDefaultValue());
        entity.setMaxLength(item.getMaxLength());
        entity.setPlatformLocked(defaultYesNo(item.getPlatformLocked()));
        entity.setUsedInCondition(defaultYesNo(item.getUsedInCondition()));
        entity.setUsedInList(defaultYesNo(item.getUsedInList()));
        entity.setPromoteSlot(item.getPromoteSlot());
        entity.setListSlotSync(defaultYesNo(item.getListSlotSync()));
        entity.setStatus(defaultStatus(item.getStatus()));
        entity.setRemark(item.getRemark());
        return entity;
    }

    public static OaTenantFieldDefEntity toTenantFieldEntity(
            OaFieldDefItemRequest item, String tenantId, String versionId, String originType, String sourceId) {
        OaTenantFieldDefEntity entity = new OaTenantFieldDefEntity();
        entity.setTenantId(tenantId);
        entity.setTemplateVersionId(versionId);
        entity.setSourcePlatformFieldId(sourceId);
        entity.setFieldCode(item.getFieldCode().trim());
        entity.setFieldName(item.getFieldName().trim());
        entity.setFieldAlias(item.getFieldAlias());
        entity.setFieldType(item.getFieldType().trim());
        entity.setComponentType(item.getComponentType());
        entity.setDictType(item.getDictType());
        entity.setFieldRequired(defaultYesNo(item.getFieldRequired()));
        entity.setSortNo(item.getSortNo() == null ? 0 : item.getSortNo());
        entity.setFieldGroup(item.getFieldGroup());
        entity.setColSpan(defaultColSpan(item.getColSpan()));
        entity.setDefaultValue(item.getDefaultValue());
        entity.setMaxLength(item.getMaxLength());
        entity.setPlatformLocked(defaultYesNo(item.getPlatformLocked()));
        entity.setUsedInCondition(defaultYesNo(item.getUsedInCondition()));
        entity.setUsedInList(defaultYesNo(item.getUsedInList()));
        entity.setPromoteSlot(item.getPromoteSlot());
        entity.setListSlotSync(defaultYesNo(item.getListSlotSync()));
        entity.setOriginType(originType);
        entity.setStatus(defaultStatus(item.getStatus()));
        entity.setRemark(item.getRemark());
        return entity;
    }

    public static OaTenantFieldDefEntity copyFromPlatform(OaPlatformFieldDefEntity platform, String tenantId, String versionId) {
        OaTenantFieldDefEntity entity = new OaTenantFieldDefEntity();
        entity.setTenantId(tenantId);
        entity.setTemplateVersionId(versionId);
        entity.setSourcePlatformFieldId(platform.getId());
        entity.setFieldCode(platform.getFieldCode());
        entity.setFieldName(platform.getFieldName());
        entity.setFieldAlias(platform.getFieldAlias());
        entity.setFieldType(platform.getFieldType());
        entity.setComponentType(platform.getComponentType());
        entity.setDictType(platform.getDictType());
        entity.setFieldRequired(platform.getFieldRequired());
        entity.setSortNo(platform.getSortNo());
        entity.setFieldGroup(platform.getFieldGroup());
        entity.setColSpan(platform.getColSpan());
        entity.setDefaultValue(platform.getDefaultValue());
        entity.setMaxLength(platform.getMaxLength());
        entity.setPlatformLocked(platform.getPlatformLocked());
        entity.setUsedInCondition(platform.getUsedInCondition());
        entity.setUsedInList(platform.getUsedInList());
        entity.setPromoteSlot(platform.getPromoteSlot());
        entity.setListSlotSync(platform.getListSlotSync());
        entity.setOriginType(com.icoolkj.platform.oa.constant.OaFieldOriginType.PLATFORM_COPY);
        entity.setStatus(platform.getStatus());
        entity.setRemark(platform.getRemark());
        return entity;
    }

    private static OaPlatformFieldDefEntity copyToPlatform(OaTenantFieldDefEntity entity) {
        OaPlatformFieldDefEntity platform = new OaPlatformFieldDefEntity();
        platform.setId(entity.getId());
        platform.setFieldCode(entity.getFieldCode());
        platform.setFieldName(entity.getFieldName());
        platform.setFieldAlias(entity.getFieldAlias());
        platform.setFieldType(entity.getFieldType());
        platform.setComponentType(entity.getComponentType());
        platform.setDictType(entity.getDictType());
        platform.setFieldRequired(entity.getFieldRequired());
        platform.setSortNo(entity.getSortNo());
        platform.setFieldGroup(entity.getFieldGroup());
        platform.setColSpan(entity.getColSpan());
        platform.setDefaultValue(entity.getDefaultValue());
        platform.setMaxLength(entity.getMaxLength());
        platform.setPlatformLocked(entity.getPlatformLocked());
        platform.setUsedInCondition(entity.getUsedInCondition());
        platform.setUsedInList(entity.getUsedInList());
        platform.setPromoteSlot(entity.getPromoteSlot());
        platform.setListSlotSync(entity.getListSlotSync());
        platform.setStatus(entity.getStatus());
        platform.setRemark(entity.getRemark());
        return platform;
    }

    private static String defaultYesNo(String value) {
        return value == null || value.isBlank() ? CommonConstants.NO : value;
    }

    private static String defaultStatus(String value) {
        return value == null || value.isBlank() ? CommonConstants.STATUS_NORMAL : value;
    }

    private static int defaultColSpan(Integer value) {
        return value != null && value == 2 ? 2 : 1;
    }
}
