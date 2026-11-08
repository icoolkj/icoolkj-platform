package com.icoolkj.platform.workflow.convert;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.workflow.constant.WfBindingMode;
import com.icoolkj.platform.workflow.domain.WfBizBindingPolicyEntity;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyUpdateRequest;
import com.icoolkj.platform.workflow.vo.WorkflowBizBindingPolicyVO;

/** 业务绑定策略对象转换 */
public final class WorkflowBizBindingPolicyConvert {

    private WorkflowBizBindingPolicyConvert() {
    }

    public static WorkflowBizBindingPolicyVO toVo(WfBizBindingPolicyEntity entity) {
        if (entity == null) {
            return null;
        }
        WorkflowBizBindingPolicyVO vo = new WorkflowBizBindingPolicyVO();
        vo.setId(entity.getId());
        vo.setBizType(entity.getBizType());
        vo.setBindingMode(entity.getBindingMode());
        vo.setAllowTenantApproverOverride(entity.getAllowTenantApproverOverride());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    public static WfBizBindingPolicyEntity toEntity(WorkflowBizBindingPolicyCreateRequest request) {
        WfBizBindingPolicyEntity entity = new WfBizBindingPolicyEntity();
        entity.setBizType(request.getBizType().trim());
        entity.setBindingMode(request.getBindingMode().trim());
        entity.setAllowTenantApproverOverride(resolveAllowOverride(request.getBindingMode(), request.getAllowTenantApproverOverride()));
        entity.setRemark(trimToNull(request.getRemark()));
        return entity;
    }

    public static void copy(WorkflowBizBindingPolicyUpdateRequest request, WfBizBindingPolicyEntity entity) {
        entity.setBindingMode(request.getBindingMode().trim());
        entity.setAllowTenantApproverOverride(resolveAllowOverride(request.getBindingMode(), request.getAllowTenantApproverOverride()));
        entity.setRemark(trimToNull(request.getRemark()));
    }

    private static String resolveAllowOverride(String bindingMode, String allowOverride) {
        if (WfBindingMode.TENANT_PREFERRED.equals(bindingMode)) {
            return CommonConstants.YES;
        }
        if (allowOverride == null || allowOverride.isBlank()) {
            return CommonConstants.YES;
        }
        return YesNoEnum.isNo(allowOverride.trim()) ? CommonConstants.NO : CommonConstants.YES;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
