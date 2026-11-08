package com.icoolkj.platform.framework.security.service;

/**
 * Concurrent login policy applied when creating a Redis session.
 */
public record ConcurrentLoginOptions(boolean allowMultiLogin, boolean kickoutOldest) {

    public static ConcurrentLoginOptions allowAll() {
        return new ConcurrentLoginOptions(true, true);
    }
}
