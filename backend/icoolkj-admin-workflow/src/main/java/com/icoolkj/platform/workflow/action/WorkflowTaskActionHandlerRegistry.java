package com.icoolkj.platform.workflow.action;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 运行时任务动作 Handler 注册表（扩展预留；驳回跳转见 {@code reject.RejectModeHandlerRegistry}）。 */
@Component
public class WorkflowTaskActionHandlerRegistry {

    private final Map<String, WorkflowTaskActionHandler> handlersByType;

    public WorkflowTaskActionHandlerRegistry(List<WorkflowTaskActionHandler> handlers) {
        Map<String, WorkflowTaskActionHandler> map = new LinkedHashMap<>();
        if (handlers != null) {
            for (WorkflowTaskActionHandler h : handlers) {
                if (h == null || !StringUtils.hasText(h.actionType())) {
                    continue;
                }
                String key = h.actionType().trim().toLowerCase(Locale.ROOT);
                if (map.containsKey(key)) {
                    throw new IllegalStateException("Duplicate WorkflowTaskActionHandler: " + key);
                }
                map.put(key, h);
            }
        }
        this.handlersByType = Map.copyOf(map);
    }

    public Optional<WorkflowTaskActionHandler> find(String actionType) {
        if (!StringUtils.hasText(actionType)) {
            return Optional.empty();
        }
        return Optional.ofNullable(handlersByType.get(actionType.trim().toLowerCase(Locale.ROOT)));
    }
}
