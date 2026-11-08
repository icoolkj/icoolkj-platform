package com.icoolkj.platform.framework.aspect;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.framework.web.HttpCharsetUtils;
import com.icoolkj.platform.framework.web.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

@Aspect
@Component
public class IdempotentAspect {

    private static final String KEY_PREFIX = "idempotent:";

    private final StringRedisTemplate stringRedisTemplate;
    private final CacheTtlProperties cacheTtlProperties;

    public IdempotentAspect(StringRedisTemplate stringRedisTemplate,
                            CacheTtlProperties cacheTtlProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheTtlProperties = cacheTtlProperties;
    }

    @Around("@within(com.icoolkj.platform.framework.annotation.Idempotent) || @annotation(com.icoolkj.platform.framework.annotation.Idempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Idempotent idempotent = findIdempotent(joinPoint);
        String key = buildKey(joinPoint, idempotent);
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, CommonConstants.YES, Duration.ofSeconds(Math.max(1, idempotent.timeoutSeconds())));
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException(idempotent.message());
        }
        boolean success = false;
        try {
            Object result = joinPoint.proceed();
            success = true;
            return result;
        } finally {
            if (!success) {
                stringRedisTemplate.delete(key);
            }
        }
    }

    private Idempotent findIdempotent(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Idempotent annotation = AnnotationUtils.findAnnotation(method, Idempotent.class);
        if (annotation != null) {
            return annotation;
        }
        return AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), Idempotent.class);
    }

    private String buildKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        String tenantId = valueOrDefault(TenantContextHolder.getTenantId(), TenantConstants.PLATFORM_TENANT_ID);
        String userId = valueOrDefault(SecurityContextHelper.getUserId(), "anonymous");
        String businessKey = firstText(idempotent.key(), idempotent.value(), defaultRequestKey(joinPoint));
        String raw = tenantId + ":" + userId + ":" + businessKey;
        String digest = DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
        return cacheTtlProperties.getKeyPrefix() + ":" + KEY_PREFIX + digest;
    }

    private String defaultRequestKey(ProceedingJoinPoint joinPoint) {
        HttpServletRequest request = HttpRequestUtils.getCurrentRequest();
        if (request == null) {
            return joinPoint.getSignature().toLongString() + ":" + Arrays.deepToString(joinPoint.getArgs());
        }
        String body = readCachedBody(request);
        String payload = StringUtils.hasText(body) ? body : Arrays.deepToString(joinPoint.getArgs());
        return request.getMethod() + ":" + request.getRequestURI() + ":" + request.getQueryString() + ":" + payload;
    }

    private String readCachedBody(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper == null || wrapper.getContentAsByteArray().length == 0) {
            return null;
        }
        return HttpCharsetUtils.decodeCachedBytes(
                wrapper.getContentAsByteArray(),
                wrapper.getContentType(),
                wrapper.getCharacterEncoding());
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String valueOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
