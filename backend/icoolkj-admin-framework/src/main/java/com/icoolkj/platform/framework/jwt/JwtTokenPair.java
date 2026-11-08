package com.icoolkj.platform.framework.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Access + opaque refresh pair returned after login / refresh rotation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenPair {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expireSeconds;
    private long refreshExpireSeconds;
}
