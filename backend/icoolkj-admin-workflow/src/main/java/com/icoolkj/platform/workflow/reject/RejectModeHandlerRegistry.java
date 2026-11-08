package com.icoolkj.platform.workflow.reject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RejectModeHandlerRegistry {

    private final Map<String, RejectModeHandler> handlersByMode;

    public RejectModeHandlerRegistry(List<RejectModeHandler> handlers) {
        Map<String, RejectModeHandler> map = new LinkedHashMap<>();
        if (handlers != null) {
            for (RejectModeHandler h : handlers) {
                if (h == null || !StringUtils.hasText(h.rejectMode())) {
                    continue;
                }
                String key = h.rejectMode().trim().toLowerCase(Locale.ROOT);
                if (map.containsKey(key)) {
                    throw new IllegalStateException("Duplicate RejectModeHandler: " + key);
                }
                map.put(key, h);
            }
        }
        this.handlersByMode = Map.copyOf(map);
    }

    /** @return null 表示走 BPMN end（无跳转 Handler） */
    public RejectModeHandler find(String rejectMode) {
        if (!StringUtils.hasText(rejectMode)) {
            return null;
        }
        return handlersByMode.get(rejectMode.trim().toLowerCase(Locale.ROOT));
    }
}
