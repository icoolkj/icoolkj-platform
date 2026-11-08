package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.config.WorkflowTenantSupport;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** 流程模型操作租户边界：租户可读写本租户模型，只读查看平台预置模型 */
@Component
public class WorkflowModelAccessSupport {

    public void assertOwnedByOperator(WfModelEntity model) {
        if (model == null) {
            throw new BusinessException("error.workflow.model_not_found");
        }
        String ownerTenantId = WorkflowTenantSupport.modelOwnerTenantId();
        if (!Objects.equals(ownerTenantId, model.getTenantId())) {
            throw new BusinessException("error.workflow.model_access_denied");
        }
    }

    /** 本租户模型，或（非平台操作者查看）平台预置模型 */
    public void assertReadableByOperator(WfModelEntity model) {
        if (model == null) {
            throw new BusinessException("error.workflow.model_not_found");
        }
        String ownerTenantId = WorkflowTenantSupport.modelOwnerTenantId();
        if (Objects.equals(ownerTenantId, model.getTenantId())) {
            return;
        }
        if (!TenantConstants.PLATFORM_TENANT_ID.equals(ownerTenantId)
                && TenantConstants.PLATFORM_TENANT_ID.equals(model.getTenantId())) {
            return;
        }
        throw new BusinessException("error.workflow.model_access_denied");
    }
}
