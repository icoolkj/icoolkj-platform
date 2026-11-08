package com.icoolkj.platform.system.user.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.system.user.service.PasswordPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.icoolkj.platform.system.auth.service.impl.LoginSecurityConfigService;

/**
 * Enforces tenant {@code security.password.*} complexity (等保口令基线：长度 + 大小写 + 数字 + 特殊字符).
 */
@Service
public class PasswordPolicyServiceImpl implements PasswordPolicyService {

    /** Platform fallback when {@code security.password.min-length} is absent. */
    public static final int DEFAULT_PASSWORD_MIN_LENGTH = 10;

    private final LoginSecurityConfigService loginSecurityConfigService;

    public PasswordPolicyServiceImpl(LoginSecurityConfigService loginSecurityConfigService) {
        this.loginSecurityConfigService = loginSecurityConfigService;
    }

    @Override
    public void validate(String tenantId, String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new BusinessException("error.user.password_required");
        }
        int minLength = loginSecurityConfigService.getPasswordMinLength(tenantId, DEFAULT_PASSWORD_MIN_LENGTH);
        if (rawPassword.length() < minLength) {
            throw new BusinessException("error.user.password_too_short", minLength);
        }
        if (loginSecurityConfigService.isPasswordRequireUpper(tenantId)
                && rawPassword.chars().noneMatch(Character::isUpperCase)) {
            throw new BusinessException("error.user.password_require_upper");
        }
        if (loginSecurityConfigService.isPasswordRequireLower(tenantId)
                && rawPassword.chars().noneMatch(Character::isLowerCase)) {
            throw new BusinessException("error.user.password_require_lower");
        }
        if (loginSecurityConfigService.isPasswordRequireNumber(tenantId)
                && rawPassword.chars().noneMatch(Character::isDigit)) {
            throw new BusinessException("error.user.password_require_number");
        }
        if (loginSecurityConfigService.isPasswordRequireSpecial(tenantId)
                && rawPassword.chars().allMatch(Character::isLetterOrDigit)) {
            throw new BusinessException("error.user.password_require_special");
        }
    }
}
