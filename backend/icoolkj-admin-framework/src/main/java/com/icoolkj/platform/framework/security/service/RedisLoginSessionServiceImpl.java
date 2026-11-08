package com.icoolkj.platform.framework.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.CacheKeyConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import com.icoolkj.platform.framework.jwt.JwtProperties;
import com.icoolkj.platform.framework.security.model.LoginSession;
import com.icoolkj.platform.framework.security.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class RedisLoginSessionServiceImpl implements LoginSessionService {

    private static final Logger log = LoggerFactory.getLogger(RedisLoginSessionServiceImpl.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheTtlProperties cacheTtlProperties;
    private final JwtProperties jwtProperties;

    public RedisLoginSessionServiceImpl(StringRedisTemplate stringRedisTemplate,
                                        ObjectMapper objectMapper,
                                        CacheTtlProperties cacheTtlProperties,
                                        JwtProperties jwtProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.cacheTtlProperties = cacheTtlProperties;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String createSession(LoginUser loginUser, String tenantCode, ConcurrentLoginOptions options) {
        ConcurrentLoginOptions policy = options != null ? options : ConcurrentLoginOptions.allowAll();
        enforceConcurrentLoginPolicy(loginUser.getTenantId(), loginUser.getUserId(), policy);

        String sessionId = UUID.randomUUID().toString().replace("-", "");

        LoginSession session = new LoginSession();
        session.setSessionId(sessionId);
        session.setUserId(loginUser.getUserId());
        session.setUsername(loginUser.getUsername());
        session.setTenantId(loginUser.getTenantId());
        session.setTenantCode(tenantCode);
        session.setCreatedAt(Instant.now());
        session.setMustChangePassword(!loginUser.isCredentialsNonExpired());
        session.setMustEnrollMfa(loginUser.isMustEnrollMfa());

        saveSession(session);

        String userSessionsKey = userSessionsKey(loginUser.getTenantId(), loginUser.getUserId());
        stringRedisTemplate.opsForSet().add(userSessionsKey, sessionId);
        stringRedisTemplate.expire(userSessionsKey, sessionTtl());

        return sessionId;
    }

    private void enforceConcurrentLoginPolicy(String tenantId, String userId, ConcurrentLoginOptions policy) {
        if (policy.allowMultiLogin()) {
            return;
        }
        List<LoginSession> existing = listUserSessions(tenantId, userId);
        if (existing.isEmpty()) {
            return;
        }
        if (policy.kickoutOldest()) {
            existing.stream()
                    .sorted(Comparator.comparing(LoginSession::getCreatedAt,
                            Comparator.nullsFirst(Comparator.naturalOrder())))
                    .forEach(s -> removeSession(s.getSessionId()));
            return;
        }
        throw new BusinessException("error.auth.concurrent_login_denied");
    }

    @Override
    public Optional<LoginSession> getSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return Optional.empty();
        }
        String payload = stringRedisTemplate.opsForValue().get(sessionKey(sessionId));
        if (!StringUtils.hasText(payload)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(payload, LoginSession.class));
        } catch (JsonProcessingException ex) {
            log.warn("Failed to deserialize login session, sid={}", sessionId, ex);
            return Optional.empty();
        }
    }

    @Override
    public boolean isSessionValid(String sessionId, String userId) {
        return getSession(sessionId)
                .map(session -> userId != null && userId.equals(session.getUserId()))
                .orElse(false);
    }

    @Override
    public void removeSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        getSession(sessionId).ifPresent(session -> {
            deleteRefreshKey(session.getRefreshTokenHash());
            stringRedisTemplate.delete(sessionKey(sessionId));
            stringRedisTemplate.opsForSet().remove(
                    userSessionsKey(session.getTenantId(), session.getUserId()),
                    sessionId
            );
        });
    }

    @Override
    public void removeAllUserSessions(String tenantId, String userId) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(userId)) {
            return;
        }
        String userSessionsKey = userSessionsKey(tenantId, userId);
        Set<String> sessionIds = stringRedisTemplate.opsForSet().members(userSessionsKey);
        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                if (StringUtils.hasText(sessionId)) {
                    getSession(sessionId).ifPresent(s -> deleteRefreshKey(s.getRefreshTokenHash()));
                    stringRedisTemplate.delete(sessionKey(sessionId));
                }
            }
        }
        stringRedisTemplate.delete(userSessionsKey);
    }

    @Override
    public List<LoginSession> listUserSessions(String tenantId, String userId) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(userId)) {
            return List.of();
        }
        Set<String> sessionIds = stringRedisTemplate.opsForSet().members(userSessionsKey(tenantId, userId));
        if (sessionIds == null || sessionIds.isEmpty()) {
            return List.of();
        }
        List<LoginSession> sessions = new ArrayList<>();
        for (String sessionId : sessionIds) {
            getSession(sessionId).ifPresent(sessions::add);
        }
        sessions.sort(Comparator.comparing(LoginSession::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return sessions;
    }

    @Override
    public void clearMustChangePassword(String sessionId) {
        getSession(sessionId).ifPresent(session -> {
            if (!session.isMustChangePassword()) {
                return;
            }
            session.setMustChangePassword(false);
            saveSession(session);
        });
    }

    @Override
    public void clearMustEnrollMfa(String sessionId) {
        getSession(sessionId).ifPresent(session -> {
            if (!session.isMustEnrollMfa()) {
                return;
            }
            session.setMustEnrollMfa(false);
            saveSession(session);
        });
    }

    @Override
    public List<LoginSession> listOnlineSessions(String tenantId) {
        String pattern = StringUtils.hasText(tenantId)
                ? userSessionsKey(tenantId, "*")
                : cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.AUTH_USER_SESSIONS + "*";
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<LoginSession> sessions = new ArrayList<>();
        for (String key : keys) {
            Set<String> sessionIds = stringRedisTemplate.opsForSet().members(key);
            if (sessionIds == null) {
                continue;
            }
            for (String sessionId : sessionIds) {
                getSession(sessionId).ifPresent(sessions::add);
            }
        }
        sessions.sort(Comparator.comparing(LoginSession::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return sessions;
    }

    @Override
    public void saveSession(LoginSession session) {
        if (session == null || !StringUtils.hasText(session.getSessionId())) {
            return;
        }
        stringRedisTemplate.opsForValue().set(sessionKey(session.getSessionId()), serialize(session), sessionTtl());
        if (StringUtils.hasText(session.getTenantId()) && StringUtils.hasText(session.getUserId())) {
            stringRedisTemplate.expire(userSessionsKey(session.getTenantId(), session.getUserId()), sessionTtl());
        }
    }

    private void deleteRefreshKey(String refreshTokenHash) {
        if (StringUtils.hasText(refreshTokenHash)) {
            stringRedisTemplate.delete(
                    cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.AUTH_REFRESH + refreshTokenHash);
        }
    }

    private String sessionKey(String sessionId) {
        return cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.AUTH_LOGIN_SESSION + sessionId;
    }

    private String userSessionsKey(String tenantId, String userId) {
        return cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.AUTH_USER_SESSIONS + tenantId + ":" + userId;
    }

    private Duration sessionTtl() {
        long refresh = jwtProperties.getRefreshTokenExpireSeconds();
        if (refresh > 0) {
            return Duration.ofSeconds(refresh);
        }
        return Duration.ofSeconds(cacheTtlProperties.getTtl().getAuthSession());
    }

    private String serialize(LoginSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize login session", ex);
        }
    }
}
