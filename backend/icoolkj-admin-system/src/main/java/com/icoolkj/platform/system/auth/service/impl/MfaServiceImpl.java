package com.icoolkj.platform.system.auth.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.CacheKeyConstants;
import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.SodRoleCodes;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.auth.AuthTokenIssuer;
import com.icoolkj.platform.framework.auth.dto.AuthLoginContext;
import com.icoolkj.platform.framework.auth.dto.AuthLoginResponse;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import com.icoolkj.platform.framework.security.crypto.AesGcmSecretCipher;
import com.icoolkj.platform.framework.security.mfa.MfaPreAuthTicket;
import com.icoolkj.platform.framework.security.mfa.TotpService;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.security.service.ConcurrentLoginOptions;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.auth.dto.MfaCodeRequest;
import com.icoolkj.platform.system.auth.dto.MfaVerifyRequest;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import com.icoolkj.platform.system.auth.service.LoginSecurityPolicyService;
import com.icoolkj.platform.system.auth.service.MfaService;
import com.icoolkj.platform.system.auth.vo.MfaEnrollStartVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MfaServiceImpl implements MfaService {

    private static final Duration TICKET_TTL = Duration.ofMinutes(5);
    private static final Duration ENROLL_TTL = Duration.ofMinutes(10);

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final LoginSecurityConfigService loginSecurityConfigService;
    private final LoginSecurityPolicyService loginSecurityPolicyService;
    private final LoginSessionService loginSessionService;
    private final AuthTokenIssuer authTokenIssuer;
    private final TotpService totpService;
    private final AesGcmSecretCipher secretCipher;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheTtlProperties cacheTtlProperties;

    public MfaServiceImpl(UserMapper userMapper,
                          UserRoleMapper userRoleMapper,
                          LoginSecurityConfigService loginSecurityConfigService,
                          LoginSecurityPolicyService loginSecurityPolicyService,
                          LoginSessionService loginSessionService,
                          AuthTokenIssuer authTokenIssuer,
                          TotpService totpService,
                          AesGcmSecretCipher secretCipher,
                          StringRedisTemplate stringRedisTemplate,
                          ObjectMapper objectMapper,
                          CacheTtlProperties cacheTtlProperties) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.loginSecurityConfigService = loginSecurityConfigService;
        this.loginSecurityPolicyService = loginSecurityPolicyService;
        this.loginSessionService = loginSessionService;
        this.authTokenIssuer = authTokenIssuer;
        this.totpService = totpService;
        this.secretCipher = secretCipher;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.cacheTtlProperties = cacheTtlProperties;
    }

    @Override
    public boolean requiresMfaChallenge(LoginUser loginUser) {
        if (loginUser == null || !loginSecurityConfigService.isMfaEnabled(loginUser.getTenantId())) {
            return false;
        }
        return isUserMfaEnabled(loginUser.getUserId());
    }

    @Override
    public String createPreAuthTicket(LoginUser loginUser, String tenantCode) {
        String ticket = UUID.randomUUID().toString().replace("-", "");
        MfaPreAuthTicket payload = new MfaPreAuthTicket();
        payload.setLoginUser(loginUser);
        payload.setTenantCode(tenantCode);
        try {
            stringRedisTemplate.opsForValue().set(
                    ticketKey(ticket),
                    objectMapper.writeValueAsString(payload),
                    TICKET_TTL
            );
        } catch (JsonProcessingException ex) {
            throw new BusinessException("error.auth.mfa_ticket_failed");
        }
        return ticket;
    }

    @Override
    public AuthLoginResponse verifyLogin(MfaVerifyRequest request) {
        AuthLoginContext context = AuthLoginContext.fromCurrentRequest();
        MfaPreAuthTicket ticket = consumeTicket(request.getMfaTicket());
        LoginUser loginUser = ticket.getLoginUser();
        UserEntity user = userMapper.selectById(loginUser.getUserId());
        if (user == null || !YesNoEnum.isYes(user.getMfaEnabled()) || !StringUtils.hasText(user.getMfaSecretEnc())) {
            throw new BusinessException("error.auth.mfa_not_enabled");
        }
        String secret = secretCipher.decrypt(user.getMfaSecretEnc());
        if (!totpService.verify(secret, request.getCode())) {
            loginSecurityPolicyService.onLoginFailure(
                    loginUser.getTenantId(),
                    loginUser.getUsername(),
                    user,
                    "error.auth.mfa_invalid_code",
                    context
            );
            throw new BusinessException("error.auth.mfa_invalid_code");
        }
        loginUser.setMustEnrollMfa(false);
        return issueToken(loginUser, ticket.getTenantCode());
    }

    @Override
    public boolean shouldForceEnroll(LoginUser loginUser) {
        if (loginUser == null || !loginSecurityConfigService.isMfaEnabled(loginUser.getTenantId())) {
            return false;
        }
        if (isUserMfaEnabled(loginUser.getUserId())) {
            return false;
        }
        if (!loginSecurityConfigService.isMfaForceAdmin(loginUser.getTenantId())) {
            return false;
        }
        UserEntity user = userMapper.selectById(loginUser.getUserId());
        if (user != null && YesNoEnum.isYes(user.getIsSuperAdmin())) {
            return true;
        }
        if (Boolean.TRUE.equals(userRoleMapper.existsSuperRoleByTenantAndUser(
                loginUser.getTenantId(), loginUser.getUserId()))) {
            return true;
        }
        List<String> codes = userRoleMapper.selectRoleCodesByTenantAndUser(
                loginUser.getTenantId(), loginUser.getUserId());
        if (codes == null) {
            return false;
        }
        for (String code : codes) {
            if (SodRoleCodes.ALL.contains(code)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MfaEnrollStartVO startEnroll(LoginUser loginUser) {
        if (!loginSecurityConfigService.isMfaEnabled(loginUser.getTenantId())) {
            throw new BusinessException("error.auth.mfa_disabled");
        }
        if (isUserMfaEnabled(loginUser.getUserId())) {
            throw new BusinessException("error.auth.mfa_already_enabled");
        }
        String secret = totpService.generateSecret();
        stringRedisTemplate.opsForValue().set(enrollKey(loginUser.getUserId()), secret, ENROLL_TTL);
        String account = loginUser.getTenantCode() + ":" + loginUser.getUsername();
        MfaEnrollStartVO vo = new MfaEnrollStartVO();
        vo.setOtpAuthUri(totpService.buildOtpAuthUri("iCoolKJ", account, secret));
        vo.setSecretHint(maskSecret(secret));
        return vo;
    }

    @Override
    public void confirmEnroll(LoginUser loginUser, MfaCodeRequest request) {
        String secret = stringRedisTemplate.opsForValue().get(enrollKey(loginUser.getUserId()));
        if (!StringUtils.hasText(secret)) {
            throw new BusinessException("error.auth.mfa_enroll_expired");
        }
        if (!totpService.verify(secret, request.getCode())) {
            throw new BusinessException("error.auth.mfa_invalid_code");
        }
        UserEntity user = userMapper.selectById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.user");
        }
        user.setMfaEnabled(CommonConstants.YES);
        user.setMfaSecretEnc(secretCipher.encrypt(secret));
        user.setMfaBoundAt(LocalDateTime.now());
        userMapper.updateById(user);
        stringRedisTemplate.delete(enrollKey(loginUser.getUserId()));
        if (StringUtils.hasText(loginUser.getSessionId())) {
            loginSessionService.clearMustEnrollMfa(loginUser.getSessionId());
        }
    }

    @Override
    public void disable(LoginUser loginUser, MfaCodeRequest request) {
        UserEntity user = userMapper.selectById(loginUser.getUserId());
        if (user == null || !YesNoEnum.isYes(user.getMfaEnabled())) {
            throw new BusinessException("error.auth.mfa_not_enabled");
        }
        String secret = secretCipher.decrypt(user.getMfaSecretEnc());
        if (!totpService.verify(secret, request.getCode())) {
            throw new BusinessException("error.auth.mfa_invalid_code");
        }
        if (shouldForceEnrollAfterDisable(loginUser, user)) {
            throw new BusinessException("error.auth.mfa_force_required");
        }
        user.setMfaEnabled(CommonConstants.NO);
        user.setMfaSecretEnc(null);
        user.setMfaBoundAt(null);
        userMapper.updateById(user);
    }

    @Override
    public boolean isUserMfaEnabled(String userId) {
        UserEntity user = userMapper.selectById(userId);
        return user != null && YesNoEnum.isYes(user.getMfaEnabled()) && StringUtils.hasText(user.getMfaSecretEnc());
    }

    private boolean shouldForceEnrollAfterDisable(LoginUser loginUser, UserEntity user) {
        if (!loginSecurityConfigService.isMfaEnabled(loginUser.getTenantId())
                || !loginSecurityConfigService.isMfaForceAdmin(loginUser.getTenantId())) {
            return false;
        }
        if (YesNoEnum.isYes(user.getIsSuperAdmin())) {
            return true;
        }
        if (Boolean.TRUE.equals(userRoleMapper.existsSuperRoleByTenantAndUser(
                loginUser.getTenantId(), loginUser.getUserId()))) {
            return true;
        }
        List<String> codes = userRoleMapper.selectRoleCodesByTenantAndUser(
                loginUser.getTenantId(), loginUser.getUserId());
        Set<String> set = codes == null ? Set.of() : new HashSet<>(codes);
        set.retainAll(SodRoleCodes.ALL);
        return !set.isEmpty();
    }

    private AuthLoginResponse issueToken(LoginUser loginUser, String tenantCode) {
        ConcurrentLoginOptions concurrency = new ConcurrentLoginOptions(
                loginSecurityConfigService.isAllowMultiLogin(loginUser.getTenantId()),
                loginSecurityConfigService.isSingleLoginKickoutOldest(loginUser.getTenantId())
        );
        String sessionId = loginSessionService.createSession(loginUser, tenantCode, concurrency);
        loginUser.setSessionId(sessionId);
        return authTokenIssuer.issue(loginUser);
    }

    private MfaPreAuthTicket consumeTicket(String ticket) {
        if (!StringUtils.hasText(ticket)) {
            throw new BusinessException("error.auth.mfa_ticket_invalid");
        }
        String key = ticketKey(ticket);
        String payload = stringRedisTemplate.opsForValue().get(key);
        stringRedisTemplate.delete(key);
        if (!StringUtils.hasText(payload)) {
            throw new BusinessException("error.auth.mfa_ticket_invalid");
        }
        try {
            return objectMapper.readValue(payload, MfaPreAuthTicket.class);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("error.auth.mfa_ticket_invalid");
        }
    }

    private String ticketKey(String ticket) {
        return cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.AUTH_MFA_TICKET + ticket;
    }

    private String enrollKey(String userId) {
        return cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.AUTH_MFA_ENROLL + userId;
    }

    private static String maskSecret(String base64Secret) {
        if (!StringUtils.hasText(base64Secret) || base64Secret.length() < 8) {
            return "****";
        }
        return base64Secret.substring(0, 4) + "…" + base64Secret.substring(base64Secret.length() - 4);
    }
}
