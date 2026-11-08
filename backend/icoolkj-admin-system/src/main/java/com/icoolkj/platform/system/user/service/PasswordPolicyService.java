package com.icoolkj.platform.system.user.service;

/**
 * Validates raw passwords against tenant {@code security.password.*} config.
 */
public interface PasswordPolicyService {

    void validate(String tenantId, String rawPassword);
}
