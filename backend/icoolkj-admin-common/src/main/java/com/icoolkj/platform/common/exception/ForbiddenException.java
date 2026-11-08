package com.icoolkj.platform.common.exception;

/**
 * Forbidden access (HTTP 403 semantic).
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(403, message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(403, message, cause);
    }
}
