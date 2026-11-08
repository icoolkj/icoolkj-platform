package com.icoolkj.platform.workflow.convert;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.domain.WfModelVersionEntity;
import com.icoolkj.platform.workflow.dto.WorkflowModelCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelUpdateRequest;
import com.icoolkj.platform.workflow.vo.WorkflowModelDetailVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelVersionVO;
import org.springframework.util.StringUtils;

/** 流程模型对象转换 */
public final class WorkflowModelConvert {

    private WorkflowModelConvert() {
    }

    public static WorkflowModelVO toVo(WfModelEntity entity) {
        if (entity == null) {
            return null;
        }
        WorkflowModelVO vo = new WorkflowModelVO();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setModelKey(entity.getModelKey());
        vo.setModelName(entity.getModelName());
        vo.setCategoryName(entity.getCategoryName());
        vo.setBizType(entity.getBizType());
        vo.setFormType(entity.getFormType());
        vo.setFormRoute(entity.getFormRoute());
        vo.setLatestVersion(entity.getLatestVersion());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setProcessConfig(entity.getProcessConfig());
        return vo;
    }

    public static WorkflowModelDetailVO toDetailVo(WfModelEntity entity, WfModelVersionEntity publishedVersion) {
        WorkflowModelDetailVO detail = new WorkflowModelDetailVO();
        WorkflowModelVO base = toVo(entity);
        if (base != null) {
            detail.setId(base.getId());
            detail.setTenantId(base.getTenantId());
            detail.setModelKey(base.getModelKey());
            detail.setModelName(base.getModelName());
            detail.setCategoryName(base.getCategoryName());
            detail.setBizType(base.getBizType());
            detail.setFormType(base.getFormType());
            detail.setFormRoute(base.getFormRoute());
            detail.setLatestVersion(base.getLatestVersion());
            detail.setStatus(base.getStatus());
            detail.setRemark(base.getRemark());
            detail.setProcessConfig(base.getProcessConfig());
        }
        if (publishedVersion != null) {
            detail.setBpmnXml(publishedVersion.getBpmnXml());
            detail.setDesignJson(publishedVersion.getDesignJson());
        }
        return detail;
    }

    public static WorkflowModelVersionVO toVersionVo(WfModelVersionEntity entity) {
        if (entity == null) {
            return null;
        }
        WorkflowModelVersionVO vo = new WorkflowModelVersionVO();
        vo.setId(entity.getId());
        vo.setModelId(entity.getModelId());
        vo.setVersionNo(entity.getVersionNo());
        vo.setDeploymentId(entity.getDeploymentId());
        vo.setProcessDefinitionId(entity.getProcessDefinitionId());
        vo.setProcessDefinitionKey(entity.getProcessDefinitionKey());
        vo.setPublished(entity.getPublished() != null && entity.getPublished() == 1);
        vo.setHasDesignJson(StringUtils.hasText(entity.getDesignJson()));
        vo.setRemark(entity.getRemark());
        return vo;
    }

    public static WfModelEntity toEntity(WorkflowModelCreateRequest request, String tenantId) {
        WfModelEntity entity = new WfModelEntity();
        entity.setTenantId(tenantId);
        entity.setModelKey(request.getModelKey().trim());
        entity.setModelName(request.getModelName().trim());
        entity.setCategoryName(trimToNull(request.getCategoryName()));
        entity.setBizType(request.getBizType().trim());
        entity.setFormType(defaultFormType(request.getFormType()));
        entity.setFormRoute(trimToNull(request.getFormRoute()));
        entity.setLatestVersion(0);
        entity.setStatus(defaultStatus(request.getStatus()));
        entity.setRemark(trimToNull(request.getRemark()));
        return entity;
    }

    public static void copy(WorkflowModelUpdateRequest request, WfModelEntity entity) {
        entity.setModelName(request.getModelName().trim());
        entity.setFormType(defaultFormType(request.getFormType()));
        entity.setFormRoute(trimToNull(request.getFormRoute()));
        entity.setStatus(defaultStatus(request.getStatus()));
        entity.setRemark(trimToNull(request.getRemark()));
    }

    private static String defaultFormType(String formType) {
        return formType == null || formType.isBlank() ? "system" : formType.trim();
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
