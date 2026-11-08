package com.icoolkj.platform.workflow.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 解析 design 节点 strategyConfig JSON 字段 */
@Component
public class WorkflowStrategyConfigSupport {

    private final ObjectMapper objectMapper;

    public WorkflowStrategyConfigSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String requireDelegateExpression(String strategyConfig, String taskKey) {
        String expr = readText(strategyConfig, "delegateExpression");
        if (!StringUtils.hasText(expr)) {
            throw new IllegalArgumentException("delegateExpression required for " + taskKey);
        }
        return expr.trim();
    }

    public String requireCalledElement(String strategyConfig, String taskKey) {
        String called = readText(strategyConfig, "calledElement");
        if (!StringUtils.hasText(called)) {
            throw new IllegalArgumentException("calledElement required for " + taskKey);
        }
        return called.trim();
    }

    private String readText(String strategyConfig, String field) {
        if (!StringUtils.hasText(strategyConfig)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(strategyConfig.trim());
            JsonNode node = root.get(field);
            return node == null || node.isNull() ? null : node.asText();
        } catch (Exception ex) {
            return null;
        }
    }
}
