package com.icoolkj.platform.framework.security.service;

import com.icoolkj.platform.common.constant.CacheKeyConstants;
import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import com.icoolkj.platform.framework.jwt.JwtProperties;
import com.icoolkj.platform.framework.security.model.LoginSession;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Opaque refresh tokens bound to Redis login sessions, with rotation and reuse detection.
 */
@Service
public class RefreshTokenService {

    private final StringRedisTemplate stringRedisTemplate;
    private final CacheTtlProperties cacheTtlProperties;
    private final JwtProperties jwtProperties;
    private final LoginSessionService loginSessionService;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(StringRedisTemplate stringRedisTemplate,
                               CacheTtlProperties cacheTtlProperties,
                               JwtProperties jwtProperties,
                               LoginSessionService loginSessionService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheTtlProperties = cacheTtlProperties;
        this.jwtProperties = jwtProperties;
        this.loginSessionService = loginSessionService;
    }

    public String issue(String sessionId) {
        LoginSession session = loginSessionService.getSession(sessionId)
                .orElseThrow(() -> new UnauthorizedException("error.auth.session_invalid"));
        revokeHash(session.getRefreshTokenHash());

        String plain = generatePlain();
        String hash = sha256Hex(plain);
        session.setRefreshTokenHash(hash);
        loginSessionService.saveSession(session);

        stringRedisTemplate.opsForValue().set(refreshKey(hash), sessionId, refreshTtl());
        return plain;
    }

    /**
     * Rotates refresh for a valid token. Reuse of a retired token kills the session.
     */
    public RotatedRefresh rotate(String plainRefresh) {
        if (!StringUtils.hasText(plainRefresh)) {
            throw new UnauthorizedException("error.auth.refresh_invalid");
        }
        String hash = sha256Hex(plainRefresh.trim());
        String key = refreshKey(hash);
        String sessionId = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(sessionId)) {
            throw new UnauthorizedException("error.auth.refresh_invalid");
        }

        Optional<LoginSession> sessionOpt = loginSessionService.getSession(sessionId);
        if (sessionOpt.isEmpty()) {
            stringRedisTemplate.delete(key);
            throw new UnauthorizedException("error.auth.refresh_invalid");
        }
        LoginSession session = sessionOpt.get();
        if (!hash.equals(session.getRefreshTokenHash())) {
            // Reuse after rotation → possible theft
            loginSessionService.removeSession(sessionId);
            stringRedisTemplate.delete(key);
            throw new UnauthorizedException("error.auth.refresh_reused");
        }

        stringRedisTemplate.delete(key);
        String newPlain = generatePlain();
        String newHash = sha256Hex(newPlain);
        session.setRefreshTokenHash(newHash);
        loginSessionService.saveSession(session);
        stringRedisTemplate.opsForValue().set(refreshKey(newHash), sessionId, refreshTtl());

        return new RotatedRefresh(session, newPlain);
    }

    public void revokeForSession(LoginSession session) {
        if (session == null) {
            return;
        }
        revokeHash(session.getRefreshTokenHash());
    }

    private void revokeHash(String hash) {
        if (StringUtils.hasText(hash)) {
            stringRedisTemplate.delete(refreshKey(hash));
        }
    }

    private String generatePlain() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String refreshKey(String hash) {
        return cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.AUTH_REFRESH + hash;
    }

    private Duration refreshTtl() {
        return Duration.ofSeconds(Math.max(60L, jwtProperties.getRefreshTokenExpireSeconds()));
    }

    static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    public record RotatedRefresh(LoginSession session, String refreshToken) {
    }
}
