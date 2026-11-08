package com.icoolkj.platform.system.auth.service.impl;

import com.icoolkj.platform.common.constant.CacheKeyConstants;
import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.framework.auth.dto.AuthLoginContext;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.loginlog.service.LoginLogService;
import com.icoolkj.platform.system.auth.service.LoginSecurityPolicyService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class LoginSecurityPolicyServiceImpl implements LoginSecurityPolicyService {

    private static final String ERROR_AUTH_INVALID = "error.auth.invalid_credentials";
    private static final String ERROR_AUTH_LOCKED = "error.auth.account_locked";
    private static final String ERROR_AUTH_IP_RATE_LIMITED = "error.auth.ip_rate_limited";
    private static final String ERROR_AUTH_LOGIN_SUCCESS = "error.auth.login_success";

    private final LoginSecurityConfigService loginSecurityConfigService;
    private final CacheTtlProperties cacheTtlProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;
    private final LoginLogService loginLogService;
    private final I18nMessageResolver i18n;

    public LoginSecurityPolicyServiceImpl(LoginSecurityConfigService loginSecurityConfigService,
                                          CacheTtlProperties cacheTtlProperties,
                                          StringRedisTemplate stringRedisTemplate,
                                          UserMapper userMapper,
                                          LoginLogService loginLogService,
                                          I18nMessageResolver i18n) {
        this.loginSecurityConfigService = loginSecurityConfigService;
        this.cacheTtlProperties = cacheTtlProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.userMapper = userMapper;
        this.loginLogService = loginLogService;
        this.i18n = i18n;
    }

    @Override
    public void ensureLoginAllowed(String tenantId, String username, UserEntity user) {
        int maxRetry = loginSecurityConfigService.getMaxFailCount(tenantId);
        if (isLockedByRedis(tenantId, username, maxRetry)
                || (user != null && user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now()))) {
            int[] remaining = toMinutesSeconds(remainingLockDuration(tenantId, username, user));
            throw new UnauthorizedException(ERROR_AUTH_LOCKED, remaining[0], remaining[1]);
        }
    }

    @Override
    public void ensureIpRateAllowed(String tenantId, String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return;
        }
        int maxPerIp = loginSecurityConfigService.getMaxFailCountPerIp(tenantId);
        if (maxPerIp <= 0) {
            return;
        }
        String value = stringRedisTemplate.opsForValue().get(ipFailCountKey(clientIp));
        if (!StringUtils.hasText(value)) {
            return;
        }
        try {
            if (Integer.parseInt(value) >= maxPerIp) {
                int[] remaining = toMinutesSeconds(remainingIpLockDuration(tenantId, clientIp));
                throw new UnauthorizedException(ERROR_AUTH_IP_RATE_LIMITED, remaining[0], remaining[1]);
            }
        } catch (NumberFormatException ignored) {
            // treat as unlocked
        }
    }

    @Override
    public void onLoginSuccess(UserEntity user, AuthLoginContext context) {
        clearFailCount(user.getTenantId(), user.getUsername());
        clearIpFailCount(resolveClientIp(context));

        userMapper.resetLoginSuccess(
                user.getId(),
                resolveClientIp(context),
                LocalDateTime.now());

        loginLogService.recordSuccess(
                user.getTenantId(),
                user.getUsername(),
                i18n.resolve(ERROR_AUTH_LOGIN_SUCCESS),
                resolveClientIp(context),
                resolveUserAgent(context)
        );
    }

    @Override
    public void onLoginFailure(String tenantId,
                               String username,
                               UserEntity user,
                               String message,
                               AuthLoginContext context) {
        int maxRetry = loginSecurityConfigService.getMaxFailCount(tenantId);
        int failCount = incrementFailCount(tenantId, username, user);
        incrementIpFailCount(tenantId, resolveClientIp(context));

        LocalDateTime lockedUntil = null;
        if (user != null && failCount >= maxRetry) {
            lockedUntil = LocalDateTime.now()
                    .plusMinutes(loginSecurityConfigService.getLockMinutes(tenantId));
            userMapper.updateLockState(user.getId(), failCount, lockedUntil);
            user.setLockedUntil(lockedUntil);
            user.setLoginFailCount(failCount);
        }

        if (failCount >= maxRetry) {
            int[] remaining = toMinutesSeconds(remainingLockDuration(tenantId, username, user));
            loginLogService.recordFailure(
                    tenantId,
                    username,
                    i18n.resolve(ERROR_AUTH_LOCKED, remaining[0], remaining[1]),
                    resolveClientIp(context),
                    resolveUserAgent(context)
            );
            throw new UnauthorizedException(ERROR_AUTH_LOCKED, remaining[0], remaining[1]);
        }

        int remainingAttempts = Math.max(0, maxRetry - failCount);
        String logMessage = ERROR_AUTH_INVALID.equals(message)
                ? i18n.resolve(ERROR_AUTH_INVALID, remainingAttempts)
                : i18n.resolve(message);
        loginLogService.recordFailure(
                tenantId,
                username,
                logMessage,
                resolveClientIp(context),
                resolveUserAgent(context)
        );
        throw new UnauthorizedException(ERROR_AUTH_INVALID, remainingAttempts);
    }

    private int incrementFailCount(String tenantId, String username, UserEntity user) {
        String redisKey = failCountKey(tenantId, username);
        Long redisCount = stringRedisTemplate.opsForValue().increment(redisKey);
        if (redisCount != null && redisCount == 1L) {
            stringRedisTemplate.expire(redisKey, failCountTtl(tenantId));
        }

        int failCount = redisCount == null ? 1 : redisCount.intValue();
        if (user != null) {
            userMapper.updateLoginFailCount(user.getId(), failCount);
        }
        return failCount;
    }

    private void incrementIpFailCount(String tenantId, String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return;
        }
        int maxPerIp = loginSecurityConfigService.getMaxFailCountPerIp(tenantId);
        if (maxPerIp <= 0) {
            return;
        }
        String redisKey = ipFailCountKey(clientIp);
        Long redisCount = stringRedisTemplate.opsForValue().increment(redisKey);
        if (redisCount != null && redisCount == 1L) {
            stringRedisTemplate.expire(redisKey, ipFailCountTtl(tenantId));
        }
    }

    private void clearFailCount(String tenantId, String username) {
        stringRedisTemplate.delete(failCountKey(tenantId, username));
    }

    @Override
    public void clearLoginFailCounts(String tenantId, String username) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(username)) {
            return;
        }
        clearFailCount(tenantId, username.trim());
    }

    private void clearIpFailCount(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return;
        }
        stringRedisTemplate.delete(ipFailCountKey(clientIp));
    }

    private boolean isLockedByRedis(String tenantId, String username, int maxRetry) {
        String value = stringRedisTemplate.opsForValue().get(failCountKey(tenantId, username));
        if (!StringUtils.hasText(value)) {
            return false;
        }
        try {
            return Integer.parseInt(value) >= maxRetry;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private Duration remainingLockDuration(String tenantId, String username, UserEntity user) {
        if (user != null && user.getLockedUntil() != null) {
            Duration until = Duration.between(LocalDateTime.now(), user.getLockedUntil());
            if (!until.isNegative() && !until.isZero()) {
                return until;
            }
        }
        Long ttlSeconds = stringRedisTemplate.getExpire(failCountKey(tenantId, username), TimeUnit.SECONDS);
        if (ttlSeconds != null && ttlSeconds > 0) {
            return Duration.ofSeconds(ttlSeconds);
        }
        long lockMinutes = loginSecurityConfigService.getLockMinutes(tenantId);
        return lockMinutes > 0 ? Duration.ofMinutes(lockMinutes) : Duration.ofSeconds(1);
    }

    private Duration remainingIpLockDuration(String tenantId, String clientIp) {
        Long ttlSeconds = stringRedisTemplate.getExpire(ipFailCountKey(clientIp), TimeUnit.SECONDS);
        if (ttlSeconds != null && ttlSeconds > 0) {
            return Duration.ofSeconds(ttlSeconds);
        }
        long lockMinutes = loginSecurityConfigService.getIpLockMinutes(tenantId);
        return lockMinutes > 0 ? Duration.ofMinutes(lockMinutes) : Duration.ofSeconds(1);
    }

    /**
     * Split duration into minutes and seconds for MessageFormat placeholders.
     * Always at least 0m 1s while still locked to avoid a false "unlocked" message.
     */
    private static int[] toMinutesSeconds(Duration duration) {
        long totalSeconds = Math.max(1, duration.getSeconds());
        int minutes = (int) (totalSeconds / 60);
        int seconds = (int) (totalSeconds % 60);
        return new int[]{minutes, seconds};
    }

    private String failCountKey(String tenantId, String username) {
        return cacheTtlProperties.getKeyPrefix() + ":"
                + CacheKeyConstants.AUTH_LOGIN_FAIL
                + tenantId + ":" + username;
    }

    private String ipFailCountKey(String clientIp) {
        return cacheTtlProperties.getKeyPrefix() + ":"
                + CacheKeyConstants.AUTH_LOGIN_FAIL_IP
                + clientIp;
    }

    private Duration failCountTtl(String tenantId) {
        long lockMinutes = loginSecurityConfigService.getLockMinutes(tenantId);
        if (lockMinutes > 0) {
            return Duration.ofMinutes(lockMinutes);
        }
        return Duration.ofSeconds(cacheTtlProperties.getTtl().getAuthLoginFail());
    }

    private Duration ipFailCountTtl(String tenantId) {
        long lockMinutes = loginSecurityConfigService.getIpLockMinutes(tenantId);
        if (lockMinutes > 0) {
            return Duration.ofMinutes(lockMinutes);
        }
        return Duration.ofSeconds(cacheTtlProperties.getTtl().getAuthLoginFail());
    }

    private String resolveClientIp(AuthLoginContext context) {
        return context == null ? null : context.getClientIp();
    }

    private String resolveUserAgent(AuthLoginContext context) {
        return context == null ? null : context.getUserAgent();
    }
}
