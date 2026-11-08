package com.icoolkj.platform.infra.async.redis;

import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.infra.async.AsyncTaskBus;
import com.icoolkj.platform.infra.async.AsyncTaskExecutionContext;
import com.icoolkj.platform.infra.async.AsyncTaskMessage;
import com.icoolkj.platform.infra.async.AsyncTaskPublisher;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Redis Stream bus: API publishes task id; worker consumes separately.
 */
@Component
@ConditionalOnProperty(prefix = "icoolkj.async", name = "provider", havingValue = "redis-stream")
public class RedisStreamAsyncTaskBusImpl implements AsyncTaskBus, AsyncTaskPublisher {

    private final AsyncTaskMapper asyncTaskMapper;
    private final RedisStreamTaskPublisher publisher;

    public RedisStreamAsyncTaskBusImpl(AsyncTaskMapper asyncTaskMapper, RedisStreamTaskPublisher publisher) {
        this.asyncTaskMapper = asyncTaskMapper;
        this.publisher = publisher;
    }

    @Override
    public void publish(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return;
        }
        String id = taskId.trim();
        AsyncTaskEntity entity = asyncTaskMapper.selectById(id);
        String tenantId = entity != null && StringUtils.hasText(entity.getTenantId())
                ? entity.getTenantId()
                : (TenantContextHolder.get() == null ? null : TenantContextHolder.get().getTenantId());
        String userId = entity != null && StringUtils.hasText(entity.getUserId())
                ? entity.getUserId()
                : SecurityContextHelper.getUserId();
        String locale = entity != null && StringUtils.hasText(entity.getLocale())
                ? entity.getLocale()
                : AsyncTaskExecutionContext.captureLocaleTag();
        publisher.publish(AsyncTaskMessage.of(id, tenantId, userId, locale));
    }
}
