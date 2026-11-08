package com.icoolkj.platform.infra.async.redis;

import com.icoolkj.platform.infra.async.AsyncProperties;
import com.icoolkj.platform.infra.async.AsyncTaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/** Publishes {@link AsyncTaskMessage} to the configured Redis Stream. */
@Component
@ConditionalOnProperty(prefix = "icoolkj.async", name = "provider", havingValue = "redis-stream")
public class RedisStreamTaskPublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamTaskPublisher.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final AsyncProperties asyncProperties;
    private final Object groupLock = new Object();
    private volatile boolean groupReady;

    public RedisStreamTaskPublisher(StringRedisTemplate stringRedisTemplate, AsyncProperties asyncProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.asyncProperties = asyncProperties;
    }

    public RecordId publish(AsyncTaskMessage message) {
        if (message == null || !StringUtils.hasText(message.taskId())) {
            throw new IllegalArgumentException("async taskId required");
        }
        ensureConsumerGroup();
        Map<String, String> fields = new HashMap<>(5);
        fields.put(AsyncTaskMessage.FIELD_TASK_ID, message.taskId());
        if (StringUtils.hasText(message.tenantId())) {
            fields.put(AsyncTaskMessage.FIELD_TENANT_ID, message.tenantId());
        }
        if (StringUtils.hasText(message.userId())) {
            fields.put(AsyncTaskMessage.FIELD_USER_ID, message.userId());
        }
        if (StringUtils.hasText(message.locale())) {
            fields.put(AsyncTaskMessage.FIELD_LOCALE, message.locale());
        }
        MapRecord<String, String, String> record = StreamRecords.mapBacked(fields)
                .withStreamKey(asyncProperties.getQueueName());
        RecordId id = stringRedisTemplate.opsForStream().add(record);
        log.debug("Published async task {} recordId={}", message.taskId(), id);
        return id;
    }

    /** Best-effort DLQ append (does not create a consumer group on DLQ). */
    public void publishDeadLetter(AsyncTaskMessage message, String reason) {
        if (!asyncProperties.getDeadLetter().isEnabled() || message == null) {
            return;
        }
        String dlq = asyncProperties.getDeadLetter().getQueueName();
        if (!StringUtils.hasText(dlq)) {
            return;
        }
        Map<String, String> fields = new HashMap<>(7);
        fields.put(AsyncTaskMessage.FIELD_TASK_ID, message.taskId());
        if (StringUtils.hasText(message.tenantId())) {
            fields.put(AsyncTaskMessage.FIELD_TENANT_ID, message.tenantId());
        }
        if (StringUtils.hasText(message.userId())) {
            fields.put(AsyncTaskMessage.FIELD_USER_ID, message.userId());
        }
        if (StringUtils.hasText(message.locale())) {
            fields.put(AsyncTaskMessage.FIELD_LOCALE, message.locale());
        }
        if (StringUtils.hasText(reason)) {
            fields.put("reason", reason.trim());
        }
        stringRedisTemplate.opsForStream().add(StreamRecords.mapBacked(fields).withStreamKey(dlq.trim()));
    }

    public void ensureConsumerGroup() {
        if (groupReady) {
            return;
        }
        synchronized (groupLock) {
            if (groupReady) {
                return;
            }
            String key = asyncProperties.getQueueName();
            String group = asyncProperties.getConsumerGroup();
            try {
                stringRedisTemplate.opsForStream().createGroup(key, group);
                groupReady = true;
            } catch (Exception ex) {
                // BUSYGROUP / stream already exists with group — treat as ready
                String msg = ex.getMessage() == null ? "" : ex.getMessage();
                if (msg.contains("BUSYGROUP") || msg.contains("already exists")) {
                    groupReady = true;
                } else {
                    try {
                        // Create stream + group from scratch
                        stringRedisTemplate.opsForStream().createGroup(key,
                                org.springframework.data.redis.connection.stream.ReadOffset.from("0-0"),
                                group);
                        groupReady = true;
                    } catch (Exception nested) {
                        String nestedMsg = nested.getMessage() == null ? "" : nested.getMessage();
                        if (nestedMsg.contains("BUSYGROUP") || nestedMsg.contains("already exists")) {
                            groupReady = true;
                        } else {
                            log.warn("Failed to ensure async consumer group key={} group={}: {}",
                                    key, group, nested.getMessage());
                            throw nested instanceof RuntimeException re ? re : new IllegalStateException(nested);
                        }
                    }
                }
            }
        }
    }
}
