package com.icoolkj.platform.framework.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login request payload.
 */
@Data
public class AuthLoginRequest {

    @NotBlank(message = "{validation.username.required}")
    private String username;

    @NotBlank(message = "{validation.password.required}")
    private String password;

    @NotBlank(message = "{validation.tenant_code.required}")
    private String tenantCode;

    /**
     * Optional captcha code when {@code security.login.require-captcha=true}.
     */
    private String captchaCode;

    /**
     * Captcha challenge id returned by {@code GET /auth/captcha}.
     */
    private String captchaKey;
}
