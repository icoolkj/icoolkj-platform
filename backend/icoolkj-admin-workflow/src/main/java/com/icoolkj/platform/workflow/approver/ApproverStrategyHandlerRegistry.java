package com.icoolkj.platform.workflow.approver;

import com.icoolkj.platform.common.exception.BusinessException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 审批人策略注册表 */
@Component
public class ApproverStrategyHandlerRegistry {

    private final Map<String, ApproverStrategyHandler> handlersByType;

    public ApproverStrategyHandlerRegistry(List<ApproverStrategyHandler> handlers) {
        Map<String, ApproverStrategyHandler> map = new LinkedHashMap<>();
        if (handlers != null) {
            for (ApproverStrategyHandler handler : handlers) {
                map.put(normalize(handler.strategyType()), handler);
            }
        }
        this.handlersByType = Map.copyOf(map);
    }

    public ApproverStrategyHandler require(String strategyType) {
        ApproverStrategyHandler handler = handlersByType.get(normalize(strategyType));
        if (handler == null) {
            throw new BusinessException("error.workflow.approver_strategy_unsupported");
        }
        return handler;
    }

    public static String normalize(String strategyType) {
        if (!StringUtils.hasText(strategyType)) {
            return "";
        }
        return strategyType.trim().toLowerCase(Locale.ROOT);
    }
}
