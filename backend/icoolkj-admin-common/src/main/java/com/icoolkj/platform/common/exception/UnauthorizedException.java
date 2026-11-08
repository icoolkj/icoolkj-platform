package com.icoolkj.platform.common.exception;

/**
 * Unauthorized access (HTTP 401 semantic).
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(401, message);
    }

    public UnauthorizedException(String message, Object... args) {
        super(401, message, args);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(401, message, cause);
    }
}
