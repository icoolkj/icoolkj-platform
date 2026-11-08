package com.icoolkj.platform.framework.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis channel: API enqueues run-now commands; worker consumes and writes status.
 * Authoritative history lives in {@code sys_job_log}; Redis holds near-term status only.
 */
@Service
public class ManagedJobTriggerService {

    public static final String QUEUE_KEY = "icoolkj:job:queue";
    private static final String STATUS_KEY_PREFIX = "icoolkj:job:status:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public ManagedJobTriggerService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void enqueue(String jobCode, String triggerUserId) {
        if (!ManagedJobCatalog.isKnown(jobCode)) {
            throw new BusinessException("error.job.unknown");
        }
        if (!StringUtils.hasText(triggerUserId)) {
            throw new BusinessException("error.job.trigger_user_required");
        }
        ManagedJobCommand command = new ManagedJobCommand(
                jobCode.trim(), JobTriggerType.MANUAL, triggerUserId.trim());
        try {
            stringRedisTemplate.opsForList().rightPush(QUEUE_KEY, objectMapper.writeValueAsString(command));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to enqueue managed job command", ex);
        }
        Map<String, String> patch = new HashMap<>();
        patch.put("lastTriggerAt", Instant.now().toString());
        patch.put("lastStatus", "QUEUED");
        patch.put("lastMessage", "queued");
        patch.put("lastTriggerUserId", triggerUserId.trim());
        stringRedisTemplate.opsForHash().putAll(STATUS_KEY_PREFIX + jobCode.trim(), patch);
        stringRedisTemplate.expire(STATUS_KEY_PREFIX + jobCode.trim(), 30, TimeUnit.DAYS);
    }

    /** @deprecated use {@link #enqueue(String, String)} */
    @Deprecated
    public void enqueue(String jobId) {
        throw new BusinessException("error.job.trigger_user_required");
    }

    public ManagedJobCommand poll() {
        String raw = stringRedisTemplate.opsForList().leftPop(QUEUE_KEY);
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        // Backward compatible: bare jobCode string without JSON
        if (!trimmed.startsWith("{")) {
            if (!ManagedJobCatalog.isKnown(trimmed)) {
                return null;
            }
            return new ManagedJobCommand(trimmed, JobTriggerType.MANUAL, null);
        }
        try {
            return objectMapper.readValue(trimmed, ManagedJobCommand.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Invalid managed job command JSON: " + trimmed, ex);
        }
    }

    public void markRunning(String jobCode) {
        writeStatus(jobCode, "RUNNING", "running", true, false);
    }

    public void markSuccess(String jobCode, String message) {
        writeStatus(jobCode, "SUCCESS", message == null ? "ok" : message, false, true);
    }

    public void markFailed(String jobCode, String message) {
        writeStatus(jobCode, "FAILED", message == null ? "failed" : message, false, true);
    }

    public Map<Object, Object> status(String jobCode) {
        if (!StringUtils.hasText(jobCode)) {
            return Map.of();
        }
        return stringRedisTemplate.opsForHash().entries(STATUS_KEY_PREFIX + jobCode.trim());
    }

    private void writeStatus(String jobCode, String status, String message, boolean started, boolean finished) {
        Map<String, String> patch = new HashMap<>();
        patch.put("lastStatus", status);
        patch.put("lastMessage", message);
        String now = Instant.now().toString();
        if (started) {
            patch.put("lastStartedAt", now);
        }
        if (finished) {
            patch.put("lastFinishedAt", now);
        }
        stringRedisTemplate.opsForHash().putAll(STATUS_KEY_PREFIX + jobCode.trim(), patch);
        stringRedisTemplate.expire(STATUS_KEY_PREFIX + jobCode.trim(), 30, TimeUnit.DAYS);
    }
}
