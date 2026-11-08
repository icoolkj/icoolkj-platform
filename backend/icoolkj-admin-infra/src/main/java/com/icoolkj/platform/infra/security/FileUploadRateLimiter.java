package com.icoolkj.platform.infra.security;

import com.icoolkj.platform.common.constant.CacheKeyConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import com.icoolkj.platform.infra.storage.StorageProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/** Redis-backed upload rate limiter (per tenant + user, 60s window). */
@Component
public class FileUploadRateLimiter {

    private final StringRedisTemplate stringRedisTemplate;
    private final CacheTtlProperties cacheTtlProperties;
    private final StorageProperties storageProperties;

    public FileUploadRateLimiter(StringRedisTemplate stringRedisTemplate,
                                 CacheTtlProperties cacheTtlProperties,
                                 StorageProperties storageProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheTtlProperties = cacheTtlProperties;
        this.storageProperties = storageProperties;
    }

    public void checkAndIncrement(String tenantId, String userId) {
        StorageProperties.RateLimit rateLimit = storageProperties.getSecurity().getRateLimit();
        if (!rateLimit.isEnabled()) {
            return;
        }
        String tenant = StringUtils.hasText(tenantId) ? tenantId.trim() : "0";
        String user = StringUtils.hasText(userId) ? userId.trim() : "0";
        String key = cacheTtlProperties.getKeyPrefix() + ":"
                + CacheKeyConstants.FILE_UPLOAD_RATE
                + tenant + ":" + user;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(60));
        }
        if (count != null && count > rateLimit.getMaxUploadsPerMinute()) {
            throw new BusinessException("error.file.upload_rate_limited");
        }
    }
}
