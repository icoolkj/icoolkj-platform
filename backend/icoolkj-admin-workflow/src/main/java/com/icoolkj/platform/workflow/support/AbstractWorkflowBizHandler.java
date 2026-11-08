package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.spi.WorkflowBizHandler;

import java.util.function.Function;

/**
 * 流程终态回调基类：统一在业务租户维度读写业务表（忽略操作者租户行过滤）。
 */
public abstract class AbstractWorkflowBizHandler implements WorkflowBizHandler {

    protected final WorkflowCrossTenantSupport crossTenant;

    protected AbstractWorkflowBizHandler(WorkflowCrossTenantSupport crossTenant) {
        this.crossTenant = crossTenant;
    }

    protected <T> T requireBiz(String bizId, Function<String, T> loader, String notFoundErrorKey) {
        T entity = crossTenant.runIgnoringBizTenant(() -> loader.apply(bizId));
        if (entity == null) {
            throw new BusinessException(notFoundErrorKey);
        }
        return entity;
    }

    protected void updateBizIgnoringTenant(Runnable update) {
        crossTenant.runIgnoringBizTenant(update);
    }
}
