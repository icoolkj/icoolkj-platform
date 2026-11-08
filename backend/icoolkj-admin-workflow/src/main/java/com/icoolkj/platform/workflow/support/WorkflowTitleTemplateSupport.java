package com.icoolkj.platform.workflow.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.util.StringUtils;

/**
 * 套用 {@code process_config.behavior.titleTemplate / summaryTemplate}。
 * <p>语言中性占位符：{@code {starter}} {@code {processName}} {@code {startTime}} {@code {bizType}}。
 * 兼容历史中文方括号占位（同一语义）。
 */
public final class WorkflowTitleTemplateSupport {

    private static final DateTimeFormatter START_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private WorkflowTitleTemplateSupport() {
    }

    public static String apply(String template, Map<String, String> tokens) {
        if (!StringUtils.hasText(template) || tokens == null || tokens.isEmpty()) {
            return null;
        }
        String out = template;
        for (Map.Entry<String, String> e : tokens.entrySet()) {
            String value = e.getValue() == null ? "" : e.getValue();
            out = out.replace("{" + e.getKey() + "}", value);
        }
        // legacy UI seeds
        out = out.replace("[发起人]", tokens.getOrDefault("starter", ""));
        out = out.replace("[流程名称]", tokens.getOrDefault("processName", ""));
        out = out.replace("[发起时间]", tokens.getOrDefault("startTime", ""));
        out = out.replace("[业务类型]", tokens.getOrDefault("bizType", ""));
        return out.trim();
    }

    public static Map<String, String> defaultTokens(String starterDisplay, String processName,
                                                    String bizType, LocalDateTime startTime) {
        String time = startTime == null ? "" : START_TIME_FMT.format(startTime);
        return Map.of(
                "starter", starterDisplay == null ? "" : starterDisplay,
                "processName", processName == null ? "" : processName,
                "bizType", bizType == null ? "" : bizType,
                "startTime", time
        );
    }
}
