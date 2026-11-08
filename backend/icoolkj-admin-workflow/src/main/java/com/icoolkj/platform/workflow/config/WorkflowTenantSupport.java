package com.icoolkj.platform.workflow.config;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import org.springframework.util.StringUtils;

/**
 * 工作流租户语义辅助。
 * <p>
 * 三层租户（勿混用）：
 * <ul>
 *   <li><b>操作者租户</b> — 当前登录用户 {@link TenantContextHolder} 的 {@code tenant_id}（平台管理员为 {@code 0}）</li>
 *   <li><b>业务租户</b> — 业务表行 {@code tenant_id}、流程变量 {@code tenantId}、{@code wf_biz_process.tenant_id}</li>
 *   <li><b>Flowable 租户</b> — 流程定义/实例部署维度，取 {@code wf_model.tenant_id}
 *       （{@code platform_mandatory} 模型为 {@code 0}，与业务租户可不同）</li>
 * </ul>
 * 详见 {@code docs/architecture/24-workflow-cross-tenant.md}。
 */
public final class WorkflowTenantSupport {

    private WorkflowTenantSupport() {
    }

    /** 当前登录上下文操作者租户 ID，平台操作时为 {@code 0} */
    public static String currentOperatorTenantId() {
        String tenantId = TenantContextHolder.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            return TenantConstants.PLATFORM_TENANT_ID;
        }
        return tenantId;
    }

    /** @deprecated 使用 {@link #currentOperatorTenantId()}，名称更准确 */
    @Deprecated
    public static String currentTenantId() {
        return currentOperatorTenantId();
    }

    /**
     * 规范化租户 ID 供 Flowable API 使用（空则回落平台 {@code 0}）。
     * 启动实例时须传入<b>模型归属</b>租户，而非业务租户。
     */
    public static String flowableTenantIdForModel(String modelTenantId) {
        if (!StringUtils.hasText(modelTenantId)) {
            return TenantConstants.PLATFORM_TENANT_ID;
        }
        return modelTenantId.trim();
    }

    /** @deprecated 使用 {@link #flowableTenantIdForModel(String)} */
    @Deprecated
    public static String flowableTenantId(String tenantId) {
        return flowableTenantIdForModel(tenantId);
    }

    /** 模型归属租户：平台管理员写平台模板，租户管理员写本租户 */
    public static String modelOwnerTenantId() {
        if (TenantContextHolder.isPlatform()) {
            return TenantConstants.PLATFORM_TENANT_ID;
        }
        return currentOperatorTenantId();
    }

    public static boolean isPlatformOperator() {
        return TenantContextHolder.isPlatform();
    }
}
