package com.icoolkj.platform.workflow.approver;

import java.util.Collections;
import java.util.Map;

/** 单次审批人解析上下文 */
public record ApproverStrategyContext(
        String strategyType,
        Map<String, Object> config,
        String resolveScope,
        String lookupTenantId,
        String starterUserId,
        Map<String, Object> processVariables
) {
    public ApproverStrategyContext {
        if (processVariables == null) {
            processVariables = Collections.emptyMap();
        }
    }

    /** 兼容旧调用：无流程变量 */
    public ApproverStrategyContext(String strategyType,
                                   Map<String, Object> config,
                                   String resolveScope,
                                   String lookupTenantId,
                                   String starterUserId) {
        this(strategyType, config, resolveScope, lookupTenantId, starterUserId, Collections.emptyMap());
    }
}
