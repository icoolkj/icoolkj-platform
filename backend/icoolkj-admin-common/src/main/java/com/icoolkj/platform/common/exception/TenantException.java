package com.icoolkj.platform.common.exception;

/**
 * Tenant resolution or isolation violation.
 */
public class TenantException extends BusinessException {

    public TenantException(String message) {
        super(400, message);
    }

    public TenantException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
