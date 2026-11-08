package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.config.WorkflowTenantSupport;
import org.springframework.stereotype.Component;

/** 业务绑定策略仅平台管理员可维护 */
@Component
public class WorkflowBizBindingPolicyAccessSupport {

    public void assertPlatformOperator() {
        if (!WorkflowTenantSupport.isPlatformOperator()) {
            throw new BusinessException("error.workflow.biz_binding_policy_access_denied");
        }
    }
}
