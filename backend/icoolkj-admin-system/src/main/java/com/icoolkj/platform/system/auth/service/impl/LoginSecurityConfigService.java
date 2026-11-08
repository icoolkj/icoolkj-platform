package com.icoolkj.platform.system.auth.service.impl;

import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.SecurityProperties;
import com.icoolkj.platform.system.config.service.ConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Typed login/security settings resolved from {@code sys_config} with YAML fallback.
 */
@Service
public class LoginSecurityConfigService {

    private static final String DEFAULT_INITIAL_PASSWORD = "StudyOnly@2026";

    private final ConfigService configService;
    private final SecurityProperties securityProperties;

    public LoginSecurityConfigService(ConfigService configService, SecurityProperties securityProperties) {
        this.configService = configService;
        this.securityProperties = securityProperties;
    }

    public int getMaxFailCount(String tenantId) {
        return configService.getInt(
                tenantId,
                ConfigKeys.LOGIN_MAX_FAIL_COUNT,
                securityProperties.getLogin().getMaxRetryCount()
        );
    }

    public int getLockMinutes(String tenantId) {
        return configService.getInt(
                tenantId,
                ConfigKeys.LOGIN_LOCK_MINUTES,
                securityProperties.getLogin().getLockMinutes()
        );
    }

    /** Max failed login attempts from one client IP (0 disables IP throttle). Default 20. */
    public int getMaxFailCountPerIp(String tenantId) {
        return configService.getInt(tenantId, ConfigKeys.LOGIN_MAX_FAIL_COUNT_PER_IP, 20);
    }

    /** Lock window in minutes for IP throttle. Default 15. */
    public int getIpLockMinutes(String tenantId) {
        return configService.getInt(tenantId, ConfigKeys.LOGIN_IP_LOCK_MINUTES, 15);
    }

    public boolean isCaptchaRequired(String tenantId) {
        return configService.getBoolean(
                tenantId,
                ConfigKeys.LOGIN_REQUIRE_CAPTCHA,
                securityProperties.getLogin().isRequireCaptcha()
        );
    }

    public boolean isAllowMultiLogin(String tenantId) {
        return configService.getBoolean(
                tenantId,
                ConfigKeys.LOGIN_ALLOW_MULTI_LOGIN,
                securityProperties.getLogin().isAllowMultiLogin()
        );
    }

    public boolean isSingleLoginKickoutOldest(String tenantId) {
        return configService.getBoolean(
                tenantId,
                ConfigKeys.LOGIN_SINGLE_KICKOUT_OLDEST,
                securityProperties.getLogin().isSingleLoginKickoutOldest()
        );
    }

    public int getPasswordExpireDays(String tenantId) {
        return configService.getInt(tenantId, ConfigKeys.PASSWORD_EXPIRE_DAYS, 90);
    }

    /**
     * 是否强制修改初始密码：租户 {@code sys_config} → 平台默认。
     * 缺省与 V2 种子一致为 {@code false}。
     */
    public boolean isForceChangeInitial(String tenantId) {
        return configService.getBoolean(tenantId, ConfigKeys.PASSWORD_FORCE_CHANGE_INITIAL, false);
    }

    /**
     * 运行时是否要求改密：用户标志 {@code must_change_password=1}
     * 且租户策略 {@link ConfigKeys#PASSWORD_FORCE_CHANGE_INITIAL} 开启（无租户覆盖则回落平台）。
     */
    public boolean isMustChangePasswordRequired(String tenantId, String mustChangePasswordFlag) {
        if (!YesNoEnum.isYes(mustChangePasswordFlag)) {
            return false;
        }
        return isForceChangeInitial(tenantId);
    }

    public int getPasswordMinLength(String tenantId, int defaultMinLength) {
        return configService.getInt(tenantId, ConfigKeys.PASSWORD_MIN_LENGTH, defaultMinLength);
    }

    public boolean isPasswordRequireUpper(String tenantId) {
        return configService.getBoolean(tenantId, ConfigKeys.PASSWORD_REQUIRE_UPPER, true);
    }

    public boolean isPasswordRequireLower(String tenantId) {
        return configService.getBoolean(tenantId, ConfigKeys.PASSWORD_REQUIRE_LOWER, true);
    }

    public boolean isPasswordRequireNumber(String tenantId) {
        return configService.getBoolean(tenantId, ConfigKeys.PASSWORD_REQUIRE_NUMBER, true);
    }

    public boolean isPasswordRequireSpecial(String tenantId) {
        return configService.getBoolean(tenantId, ConfigKeys.PASSWORD_REQUIRE_SPECIAL, true);
    }

    /**
     * 标准初始密码（新建留空 / 重置密码）。缺省与 V2 种子一致：{@code StudyOnly@2026}。
     */
    public String getInitialPassword(String tenantId) {
        String value = configService.getString(tenantId, ConfigKeys.PASSWORD_INITIAL, DEFAULT_INITIAL_PASSWORD);
        return value == null ? "" : value.trim();
    }

    /** 读取并校验非空的标准初始密码。 */
    public String requireInitialPassword(String tenantId) {
        String value = getInitialPassword(tenantId);
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("error.config.password_initial_missing");
        }
        return value;
    }

    public String getIpDefaultPolicy(String tenantId) {
        return configService.getString(tenantId, ConfigKeys.IP_DEFAULT_POLICY, "allow");
    }

    public boolean isMfaEnabled(String tenantId) {
        return configService.getBoolean(tenantId, ConfigKeys.MFA_ENABLED, true);
    }

    public boolean isMfaForceAdmin(String tenantId) {
        return configService.getBoolean(tenantId, ConfigKeys.MFA_FORCE_ADMIN, true);
    }
}
