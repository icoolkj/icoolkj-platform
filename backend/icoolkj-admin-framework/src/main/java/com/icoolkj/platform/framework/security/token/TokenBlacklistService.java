package com.icoolkj.platform.framework.security.token;

import com.icoolkj.platform.common.constant.CacheKeyConstants;
import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import com.icoolkj.platform.framework.jwt.JwtTokenService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

/**
 * Redis-backed JWT access-token blacklist (SHA-256 fingerprint, TTL = remaining JWT life).
 */
@Service
public class TokenBlacklistService {

    private static final String MARKER = CommonConstants.YES;

    private final StringRedisTemplate stringRedisTemplate;
    private final CacheTtlProperties cacheTtlProperties;
    private final JwtTokenService jwtTokenService;

    public TokenBlacklistService(StringRedisTemplate stringRedisTemplate,
                                 CacheTtlProperties cacheTtlProperties,
                                 JwtTokenService jwtTokenService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheTtlProperties = cacheTtlProperties;
        this.jwtTokenService = jwtTokenService;
    }

    public void blacklist(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return;
        }
        Duration ttl = jwtTokenService.remainingTtl(rawToken);
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return;
        }
        stringRedisTemplate.opsForValue().set(blacklistKey(rawToken), MARKER, ttl);
    }

    public boolean isBlacklisted(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return false;
        }
        Boolean exists = stringRedisTemplate.hasKey(blacklistKey(rawToken));
        return Boolean.TRUE.equals(exists);
    }

    private String blacklistKey(String rawToken) {
        return cacheTtlProperties.getKeyPrefix() + ":"
                + CacheKeyConstants.AUTH_BLACKLIST
                + sha256Hex(rawToken);
    }

    private static String sha256Hex(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
