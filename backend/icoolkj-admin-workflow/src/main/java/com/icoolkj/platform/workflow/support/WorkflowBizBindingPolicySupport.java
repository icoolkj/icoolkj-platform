package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.constant.WfBindingMode;
import com.icoolkj.platform.workflow.domain.WfBizBindingPolicyEntity;
import com.icoolkj.platform.workflow.mapper.WfBizBindingPolicyMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 业务类型与流程模型绑定策略 */
@Component
public class WorkflowBizBindingPolicySupport {

    private final WfBizBindingPolicyMapper bindingPolicyMapper;

    public WorkflowBizBindingPolicySupport(WfBizBindingPolicyMapper bindingPolicyMapper) {
        this.bindingPolicyMapper = bindingPolicyMapper;
    }

    public String resolveBindingMode(String bizType) {
        WfBizBindingPolicyEntity policy = findPolicy(bizType);
        if (policy == null || !StringUtils.hasText(policy.getBindingMode())) {
            return WfBindingMode.TENANT_PREFERRED;
        }
        return policy.getBindingMode().trim();
    }

    public boolean isPlatformMandatory(String bizType) {
        return WfBindingMode.PLATFORM_MANDATORY.equals(resolveBindingMode(bizType));
    }

    public boolean isTenantRequired(String bizType) {
        return WfBindingMode.TENANT_REQUIRED.equals(resolveBindingMode(bizType));
    }

    /** 是否允许出现在租户「同步平台模型」目录（自治/兼容优先，不含平台强制） */
    public boolean isSyncCatalogCandidate(String bizType) {
        String mode = resolveBindingMode(bizType);
        return WfBindingMode.TENANT_REQUIRED.equals(mode) || WfBindingMode.TENANT_PREFERRED.equals(mode);
    }

    public boolean allowTenantApproverOverride(String bizType) {
        WfBizBindingPolicyEntity policy = findPolicy(bizType);
        if (policy == null) {
            return true;
        }
        return !YesNoEnum.isNo(policy.getAllowTenantApproverOverride());
    }

    /** 租户管理员创建模型时校验：platform_mandatory 的 biz_type 禁止租户自建流程 */
    public void assertMayCreateModel(String ownerTenantId, String bizType) {
        if (!StringUtils.hasText(bizType)) {
            return;
        }
        if (!isPlatformMandatory(bizType)) {
            return;
        }
        if (TenantConstants.PLATFORM_TENANT_ID.equals(ownerTenantId)) {
            return;
        }
        throw new BusinessException("error.workflow.biz_type_platform_mandatory");
    }

    private WfBizBindingPolicyEntity findPolicy(String bizType) {
        if (!StringUtils.hasText(bizType)) {
            return null;
        }
        return bindingPolicyMapper.selectByBizType(bizType.trim());
    }
}
