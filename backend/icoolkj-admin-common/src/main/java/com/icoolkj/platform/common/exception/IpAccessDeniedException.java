package com.icoolkj.platform.common.exception;

/**
 * IP access denied (HTTP 403 semantic).
 */
public class IpAccessDeniedException extends ForbiddenException {

    public IpAccessDeniedException(String message) {
        super(message);
    }

    public IpAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
