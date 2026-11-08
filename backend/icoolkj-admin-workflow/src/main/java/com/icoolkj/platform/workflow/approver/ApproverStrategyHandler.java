package com.icoolkj.platform.workflow.approver;

import java.util.List;
import java.util.Map;

/**
 * 审批人策略（Strategy）：校验 strategyConfig + 解析用户列表。
 * 新增策略类型时新增 Handler Bean，禁止在 Support 堆 if。
 */
public interface ApproverStrategyHandler {

    String strategyType();

    void validateConfig(Map<String, Object> config);

    List<String> resolveUserIds(ApproverStrategyContext context);

    /** 诊断描述（异常占位） */
    default String describe(Map<String, Object> config, String resolveScope) {
        return strategyType() + " / " + (resolveScope == null ? "-" : resolveScope);
    }
}
