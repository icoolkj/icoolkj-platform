package com.icoolkj.platform.system.auth.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.framework.auth.AuthService;
import com.icoolkj.platform.framework.auth.AuthTokenIssuer;
import com.icoolkj.platform.framework.auth.PermissionService;
import com.icoolkj.platform.framework.auth.dto.AuthLoginContext;
import com.icoolkj.platform.framework.auth.dto.AuthLoginRequest;
import com.icoolkj.platform.framework.auth.dto.AuthLoginResponse;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.framework.security.crypto.PasswordTransportCipher;
import com.icoolkj.platform.framework.security.model.LoginSession;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.security.service.RefreshTokenService;
import com.icoolkj.platform.framework.tenant.TenantContext;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.auth.service.CaptchaService;
import com.icoolkj.platform.system.iprule.service.IpRuleMatchService;
import com.icoolkj.platform.system.loginlog.service.LoginLogService;
import com.icoolkj.platform.system.auth.service.LoginSecurityPolicyService;
import com.icoolkj.platform.system.tenant.service.TenantQueryService;
import com.icoolkj.platform.system.user.service.UserQueryService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Database-backed authentication.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String ERROR_AUTH_INVALID = "error.auth.invalid_credentials";
    private static final String ERROR_AUTH_DISABLED = "error.auth.account_disabled";
    private static final String ERROR_AUTH_PASSWORD_EXPIRED = "error.auth.password_expired";

    private final TenantQueryService tenantQueryService;
    private final UserQueryService userQueryService;
    private final UserMapper userMapper;
    private final PermissionService permissionService;
    private final LoginSecurityPolicyService loginSecurityPolicyService;
    private final LoginSecurityConfigService loginSecurityConfigService;
    private final CaptchaService captchaService;
    private final IpRuleMatchService ipRuleMatchService;
    private final LoginLogService loginLogService;
    private final PasswordEncoder passwordEncoder;
    private final I18nMessageResolver i18n;
    private final RefreshTokenService refreshTokenService;
    private final AuthTokenIssuer authTokenIssuer;
    private final PasswordTransportCipher passwordTransportCipher;

    public AuthServiceImpl(TenantQueryService tenantQueryService,
                           UserQueryService userQueryService,
                           UserMapper userMapper,
                           PermissionService permissionService,
                           LoginSecurityPolicyService loginSecurityPolicyService,
                           LoginSecurityConfigService loginSecurityConfigService,
                           CaptchaService captchaService,
                           IpRuleMatchService ipRuleMatchService,
                           LoginLogService loginLogService,
                           PasswordEncoder passwordEncoder,
                           I18nMessageResolver i18n,
                           RefreshTokenService refreshTokenService,
                           AuthTokenIssuer authTokenIssuer,
                           PasswordTransportCipher passwordTransportCipher) {
        this.tenantQueryService = tenantQueryService;
        this.userQueryService = userQueryService;
        this.userMapper = userMapper;
        this.permissionService = permissionService;
        this.loginSecurityPolicyService = loginSecurityPolicyService;
        this.loginSecurityConfigService = loginSecurityConfigService;
        this.captchaService = captchaService;
        this.ipRuleMatchService = ipRuleMatchService;
        this.loginLogService = loginLogService;
        this.passwordEncoder = passwordEncoder;
        this.i18n = i18n;
        this.refreshTokenService = refreshTokenService;
        this.authTokenIssuer = authTokenIssuer;
        this.passwordTransportCipher = passwordTransportCipher;
    }

    @Override
    public LoginUser login(AuthLoginRequest request, AuthLoginContext context) {
        AuthLoginContext loginContext = context == null ? new AuthLoginContext() : context;

        String tenantCode = request.getTenantCode();
        if (!StringUtils.hasText(tenantCode)) {
            throw new UnauthorizedException("validation.tenant_code.required");
        }

        String normalizedTenantCode = tenantCode.trim();
        String tenantId = tenantQueryService.resolveLoginTenantId(normalizedTenantCode);
        String username = request.getUsername().trim();

        if (TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)) {
            TenantContextHolder.set(TenantContext.platform(normalizedTenantCode));
        } else {
            TenantContextHolder.set(TenantContext.tenant(tenantId, normalizedTenantCode));
        }

        try {
            captchaService.validateIfRequired(tenantId, request.getCaptchaKey(), request.getCaptchaCode());
            ipRuleMatchService.ensureLoginAllowed(tenantId, loginContext.getClientIp());
            loginSecurityPolicyService.ensureIpRateAllowed(tenantId, loginContext.getClientIp());

            Optional<UserEntity> userOptional = userQueryService.findLoginUser(tenantId, username);
            UserEntity user = userOptional.orElse(null);

            loginSecurityPolicyService.ensureLoginAllowed(tenantId, username, user);

            if (user == null) {
                loginSecurityPolicyService.onLoginFailure(
                        tenantId, username, null, ERROR_AUTH_INVALID, loginContext);
            }

            String rawPassword = passwordTransportCipher.unwrap(request.getPassword());
            if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
                loginSecurityPolicyService.onLoginFailure(
                        tenantId, username, user, ERROR_AUTH_INVALID, loginContext);
            }

            if (!StatusEnum.isNormal(user.getStatus())) {
                loginLogService.recordFailure(
                        tenantId, username, i18n.resolve(ERROR_AUTH_DISABLED),
                        loginContext.getClientIp(), loginContext.getUserAgent());
                throw new UnauthorizedException(ERROR_AUTH_DISABLED);
            }

            ensurePasswordNotExpired(tenantId, user);
            loginSecurityPolicyService.onLoginSuccess(user, loginContext);
            return toLoginUser(user, normalizedTenantCode, tenantId);
        } finally {
            TenantContextHolder.clear();
        }
    }

    @Override
    public LoginUser loadSessionUser(String tenantId, String userId, String tenantCode) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(userId)) {
            throw new UnauthorizedException("error.auth.session_invalid");
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new UnauthorizedException("error.auth.session_invalid");
        }
        if (!tenantId.equals(user.getTenantId())) {
            throw new UnauthorizedException("error.auth.session_invalid");
        }
        if (!StatusEnum.isNormal(user.getStatus())) {
            throw new UnauthorizedException(ERROR_AUTH_DISABLED);
        }
        String code = StringUtils.hasText(tenantCode) ? tenantCode.trim() : "";
        return toLoginUser(user, code, tenantId);
    }

    @Override
    public AuthLoginResponse refresh(String refreshToken) {
        RefreshTokenService.RotatedRefresh rotated = refreshTokenService.rotate(refreshToken);
        LoginSession session = rotated.session();
        LoginUser loginUser = loadSessionUser(session.getTenantId(), session.getUserId(), session.getTenantCode());
        loginUser.setSessionId(session.getSessionId());
        loginUser.setMustEnrollMfa(session.isMustEnrollMfa());
        if (session.isMustChangePassword()) {
            loginUser.setCredentialsNonExpired(false);
        }
        AuthLoginResponse response = authTokenIssuer.issueAccess(loginUser, rotated.refreshToken());
        return response;
    }

    private void ensurePasswordNotExpired(String tenantId, UserEntity user) {
        if (!isPasswordExpired(user, tenantId)) {
            return;
        }
        throw new UnauthorizedException(ERROR_AUTH_PASSWORD_EXPIRED);
    }

    private LoginUser toLoginUser(UserEntity user, String tenantCode, String tenantId) {
        boolean superAdmin = YesNoEnum.isYes(user.getIsSuperAdmin());
        List<String> permissions = permissionService.loadPermissionCodes(
                user.getTenantId(),
                user.getId(),
                superAdmin
        );

        boolean mustChangePassword = loginSecurityConfigService.isMustChangePasswordRequired(
                tenantId, user.getMustChangePassword());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setPassword(null);
        loginUser.setTenantId(user.getTenantId());
        loginUser.setTenantCode(tenantCode);
        loginUser.setEnabled(StatusEnum.isNormal(user.getStatus()));
        loginUser.setAccountNonLocked(true);
        loginUser.setAccountNonExpired(true);
        loginUser.setCredentialsNonExpired(!mustChangePassword && !isPasswordExpired(user, tenantId));
        loginUser.setPermissions(permissions);
        return loginUser;
    }

    private boolean isPasswordExpired(UserEntity user, String tenantId) {
        int expireDays = loginSecurityConfigService.getPasswordExpireDays(tenantId);
        if (expireDays <= 0 || user.getPwdUpdatedAt() == null) {
            return false;
        }
        return user.getPwdUpdatedAt().plusDays(expireDays).isBefore(LocalDateTime.now());
    }
}
