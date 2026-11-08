package com.icoolkj.platform.infra.async;

import com.icoolkj.platform.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Indexes {@link AsyncTaskHandler} beans by {@link AsyncTaskHandler#taskType()}. */
@Component
public class AsyncTaskRegistry {

    private final Map<String, AsyncTaskHandler> handlers;

    public AsyncTaskRegistry(List<AsyncTaskHandler> handlerList) {
        Map<String, AsyncTaskHandler> map = new ConcurrentHashMap<>();
        if (handlerList != null) {
            for (AsyncTaskHandler handler : handlerList) {
                if (handler == null || !StringUtils.hasText(handler.taskType())) {
                    continue;
                }
                String type = handler.taskType().trim();
                AsyncTaskHandler previous = map.putIfAbsent(type, handler);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate AsyncTaskHandler for taskType=" + type);
                }
            }
        }
        this.handlers = Collections.unmodifiableMap(map);
    }

    public AsyncTaskHandler require(String taskType) {
        if (!StringUtils.hasText(taskType)) {
            throw new BusinessException("error.async.task_type_required");
        }
        AsyncTaskHandler handler = handlers.get(taskType.trim());
        if (handler == null) {
            throw new BusinessException("error.async.handler_not_found", taskType.trim());
        }
        return handler;
    }
}
