package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/**
 * 工作流跨租户读写与审批访问统一入口。
 * <p>
 * 平台管理员（操作者租户 {@code 0}）审批租户业务时，业务表仍带业务租户 {@code tenant_id}，
 * 须临时关闭 MyBatis 租户行过滤，并通过 {@link WorkflowAccessGuard} 校验流程参与权。
 * 详见 {@code docs/architecture/24-workflow-cross-tenant.md}。
 */
@Component
public class WorkflowCrossTenantSupport {

    private final WorkflowBizProcessLookup bizProcessLookup;
    private final WorkflowAccessGuard accessGuard;

    public WorkflowCrossTenantSupport(WorkflowBizProcessLookup bizProcessLookup,
                                      WorkflowAccessGuard accessGuard) {
        this.bizProcessLookup = bizProcessLookup;
        this.accessGuard = accessGuard;
    }

    /** 跨操作者租户读取/更新业务表（行内 {@code tenant_id} 为业务租户）。 */
    public <T> T runIgnoringBizTenant(Supplier<T> supplier) {
        return TenantScopeRunner.runIgnoringTenant(supplier);
    }

    public void runIgnoringBizTenant(Runnable runnable) {
        TenantScopeRunner.runIgnoringTenant(runnable);
    }

    /**
     * 审批详情/跨租户业务读：校验当前用户对该流程实例有查看权，且 {@code bizId} 与 {@code wf_biz_process} 一致。
     */
    public WfBizProcessEntity assertCanApproveDetail(String processInstanceId, String expectedBizId) {
        if (!StringUtils.hasText(processInstanceId)) {
            throw new BusinessException("error.forbidden");
        }
        String userId = requireUserId();
        WfBizProcessEntity bizProcess = bizProcessLookup.requireByProcessInstanceId(processInstanceId);
        if (StringUtils.hasText(expectedBizId) && !expectedBizId.equals(bizProcess.getBizId())) {
            throw new BusinessException("error.forbidden");
        }
        accessGuard.assertCanViewInstance(userId, bizProcess);
        return bizProcess;
    }

    private static String requireUserId() {
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("error.unauthorized");
        }
        return userId;
    }
}
