package com.icoolkj.platform.infra.async.redis;

import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantContext;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.infra.async.AsyncProperties;
import com.icoolkj.platform.infra.async.AsyncTaskConsumer;
import com.icoolkj.platform.infra.async.AsyncTaskExecutionContext;
import com.icoolkj.platform.infra.async.AsyncTaskMessage;
import com.icoolkj.platform.infra.async.AsyncTaskPrincipalResolver;
import com.icoolkj.platform.infra.async.AsyncTaskRetrySupport;
import com.icoolkj.platform.infra.async.AsyncTaskStatus;
import com.icoolkj.platform.infra.async.handler.DefaultAsyncTaskExecutor;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Redis Stream consumer group reader. Enabled only when {@code consume=true}
 * (worker process).
 */
@Component
@ConditionalOnProperty(prefix = "icoolkj.async", name = "provider", havingValue = "redis-stream")
@ConditionalOnExpression("${icoolkj.async.consume:false}")
public class RedisStreamTaskConsumer implements AsyncTaskConsumer {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamTaskConsumer.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final AsyncProperties asyncProperties;
    private final AsyncTaskMapper asyncTaskMapper;
    private final DefaultAsyncTaskExecutor asyncTaskExecutor;
    private final AsyncTaskRetrySupport asyncTaskRetrySupport;
    private final RedisStreamTaskPublisher publisher;
    private final ObjectProvider<AsyncTaskPrincipalResolver> principalResolver;

    public RedisStreamTaskConsumer(StringRedisTemplate stringRedisTemplate,
                                   AsyncProperties asyncProperties,
                                   AsyncTaskMapper asyncTaskMapper,
                                   DefaultAsyncTaskExecutor asyncTaskExecutor,
                                   AsyncTaskRetrySupport asyncTaskRetrySupport,
                                   RedisStreamTaskPublisher publisher,
                                   ObjectProvider<AsyncTaskPrincipalResolver> principalResolver) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.asyncProperties = asyncProperties;
        this.asyncTaskMapper = asyncTaskMapper;
        this.asyncTaskExecutor = asyncTaskExecutor;
        this.asyncTaskRetrySupport = asyncTaskRetrySupport;
        this.publisher = publisher;
        this.principalResolver = principalResolver;
    }

    @Override
    public int poll() {
        publisher.ensureConsumerGroup();
        int processed = 0;
        int batch = Math.max(1, asyncProperties.getPollBatchSize());
        processed += readAndProcess(ReadOffset.from("0"), batch);
        if (processed < batch) {
            processed += readAndProcess(ReadOffset.lastConsumed(), batch - processed);
        }
        return processed;
    }

    @SuppressWarnings("unchecked")
    private int readAndProcess(ReadOffset offset, int count) {
        if (count <= 0) {
            return 0;
        }
        String key = asyncProperties.getQueueName();
        String group = asyncProperties.getConsumerGroup();
        String consumerName = resolveConsumerName();
        List<MapRecord<String, Object, Object>> records;
        try {
            records = stringRedisTemplate.opsForStream().read(
                    Consumer.from(group, consumerName),
                    StreamReadOptions.empty().count(count).block(Duration.ofMillis(200)),
                    StreamOffset.create(key, offset));
        } catch (Exception ex) {
            log.warn("Async stream read failed key={} group={}: {}", key, group, ex.getMessage());
            return 0;
        }
        if (records == null || records.isEmpty()) {
            return 0;
        }
        int n = 0;
        for (MapRecord<String, Object, Object> record : records) {
            try {
                processRecord(record);
                n++;
            } catch (Throwable ex) {
                log.error("Async stream record failed id={}", record.getId(), ex);
                try {
                    forceFailAndAck(record, ex);
                } catch (Exception nested) {
                    log.error("Async stream force-fail failed id={}", record.getId(), nested);
                }
            }
        }
        return n;
    }

    private void processRecord(MapRecord<String, Object, Object> record) {
        AsyncTaskMessage message = parse(record.getValue());
        if (!StringUtils.hasText(message.taskId())) {
            ack(record);
            return;
        }

        TenantContext tenantFromMessage = StringUtils.hasText(message.tenantId())
                ? TenantContext.tenant(message.tenantId(), null)
                : TenantContext.ignore();
        TenantContext previousTenant = TenantContextHolder.get();
        try {
            TenantContextHolder.set(tenantFromMessage);

            AsyncTaskEntity entity = asyncTaskMapper.selectById(message.taskId());
            if (entity == null && !TenantContextHolder.isIgnoreTenant()) {
                TenantContextHolder.set(TenantContext.ignore());
                entity = asyncTaskMapper.selectById(message.taskId());
            }
            if (entity == null) {
                log.warn("Async task missing id={}, acking", message.taskId());
                ack(record);
                return;
            }
            if (AsyncTaskStatus.isTerminal(entity.getTaskStatus())) {
                ack(record);
                return;
            }

            // Prefer entity tenant (authoritative) for the rest of the execution
            TenantContext tenant = AsyncTaskExecutionContext.tenantFrom(message, entity);
            if (tenant != null) {
                TenantContextHolder.set(tenant);
            }
            String tenantId = AsyncTaskExecutionContext.tenantIdFrom(message, entity);
            String userId = AsyncTaskExecutionContext.userIdFrom(message, entity);
            Authentication auth = resolveAuthentication(tenantId, userId);
            java.util.Locale locale = AsyncTaskExecutionContext.toLocale(
                    AsyncTaskExecutionContext.localeFrom(message, entity));
            AsyncTaskExecutionContext.runWith(auth, tenant, locale, () -> {
                asyncTaskExecutor.execute(message.taskId());
                return null;
            });

            // Keep tenant bound for post-status reads/updates
            if (tenant != null) {
                TenantContextHolder.set(tenant);
            }
            AsyncTaskEntity after = asyncTaskMapper.selectById(message.taskId());
            if (after == null || AsyncTaskStatus.SUCCESS.equals(after.getTaskStatus())
                    || AsyncTaskStatus.CANCELLED.equals(after.getTaskStatus())) {
                ack(record);
                return;
            }
            if (AsyncTaskStatus.FAILED.equals(after.getTaskStatus())) {
                if (asyncTaskRetrySupport.prepareRetryIfApplicable(message.taskId())) {
                    log.info("Async task {} will retry", message.taskId());
                    return;
                }
                publisher.publishDeadLetter(message, after.getErrorMessage());
                ack(record);
                log.info("Async task {} failed terminal, acked", message.taskId());
                return;
            }
            log.warn("Async task {} left unacked status={}", message.taskId(),
                    after.getTaskStatus());
        } finally {
            if (previousTenant != null) {
                TenantContextHolder.set(previousTenant);
            } else {
                TenantContextHolder.clear();
            }
        }
    }

    private Authentication resolveAuthentication(String tenantId, String userId) {
        AsyncTaskPrincipalResolver resolver = principalResolver.getIfAvailable();
        if (resolver != null) {
            Authentication auth = resolver.resolve(tenantId, userId);
            if (auth != null) {
                return auth;
            }
            log.warn("Async principal resolver returned null tenantId={} userId={}", tenantId, userId);
        }
        return AsyncTaskExecutionContext.minimalAuthentication(tenantId, userId);
    }

    private void forceFailAndAck(MapRecord<String, Object, Object> record, Throwable ex) {
        AsyncTaskMessage message = parse(record.getValue());
        if (!StringUtils.hasText(message.taskId())) {
            ack(record);
            return;
        }
        TenantContext previous = TenantContextHolder.get();
        try {
            TenantContextHolder.set(StringUtils.hasText(message.tenantId())
                    ? TenantContext.tenant(message.tenantId(), null)
                    : TenantContext.ignore());
            AsyncTaskEntity entity = asyncTaskMapper.selectById(message.taskId());
            if (entity != null && !AsyncTaskStatus.isTerminal(entity.getTaskStatus())) {
                entity.setTaskStatus(AsyncTaskStatus.FAILED);
                String err = ex.getClass().getName()
                        + (StringUtils.hasText(ex.getMessage()) ? ": " + ex.getMessage() : "");
                entity.setErrorMessage(err.length() > 2000 ? err.substring(0, 2000) : err);
                entity.setFinishedAt(java.time.LocalDateTime.now());
                AuditFieldHelper.fillUpdate(entity);
                asyncTaskMapper.updateById(entity);
                publisher.publishDeadLetter(message, entity.getErrorMessage());
            }
            ack(record);
        } finally {
            if (previous != null) {
                TenantContextHolder.set(previous);
            } else {
                TenantContextHolder.clear();
            }
        }
    }

    private void ack(MapRecord<String, Object, Object> record) {
        stringRedisTemplate.opsForStream().acknowledge(
                asyncProperties.getQueueName(),
                asyncProperties.getConsumerGroup(),
                record.getId());
    }

    private AsyncTaskMessage parse(Map<Object, Object> value) {
        if (value == null || value.isEmpty()) {
            return AsyncTaskMessage.of("", null, null, null);
        }
        return AsyncTaskMessage.of(
                str(value.get(AsyncTaskMessage.FIELD_TASK_ID)),
                str(value.get(AsyncTaskMessage.FIELD_TENANT_ID)),
                str(value.get(AsyncTaskMessage.FIELD_USER_ID)),
                str(value.get(AsyncTaskMessage.FIELD_LOCALE)));
    }

    private static String str(Object raw) {
        return raw == null ? null : String.valueOf(raw);
    }

    private String resolveConsumerName() {
        if (StringUtils.hasText(asyncProperties.getConsumerName())) {
            return asyncProperties.getConsumerName().trim();
        }
        return "icoolkj-async-consumer";
    }
}
