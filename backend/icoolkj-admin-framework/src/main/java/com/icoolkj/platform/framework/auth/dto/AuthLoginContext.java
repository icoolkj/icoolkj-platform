package com.icoolkj.platform.framework.auth.dto;

import com.icoolkj.platform.framework.web.HttpRequestUtils;
import lombok.Data;

/**
 * Client metadata captured during login.
 */
@Data
public class AuthLoginContext {

    private String clientIp;
    private String userAgent;

    public AuthLoginContext() {
    }

    public AuthLoginContext(String clientIp, String userAgent) {
        this.clientIp = clientIp;
        this.userAgent = userAgent;
    }

    public static AuthLoginContext fromCurrentRequest() {
        return new AuthLoginContext(
                HttpRequestUtils.getClientIp(),
                HttpRequestUtils.getUserAgent()
        );
    }
}
