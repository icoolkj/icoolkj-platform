package com.icoolkj.platform.framework.jwt;

import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.framework.security.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * Issues and validates JWT access tokens.
 */
@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = buildSecretKey(jwtProperties.getSecret());
    }

    public String createToken(LoginUser loginUser) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(jwtProperties.getAccessTokenExpireSeconds(), ChronoUnit.SECONDS);

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(loginUser.getUserId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .claim(JwtClaimsConstants.USER_ID, loginUser.getUserId())
                .claim(JwtClaimsConstants.USERNAME, loginUser.getUsername())
                .claim(JwtClaimsConstants.NICKNAME, loginUser.getNickname())
                .claim(JwtClaimsConstants.TENANT_ID, loginUser.getTenantId())
                .claim(JwtClaimsConstants.TENANT_CODE, loginUser.getTenantCode())
                .claim(JwtClaimsConstants.SESSION_ID, loginUser.getSessionId())
                .claim(JwtClaimsConstants.TOKEN_TYPE, JwtClaimsConstants.ACCESS_TOKEN_TYPE)
                .claim(JwtClaimsConstants.PERMISSIONS, loginUser.getPermissions())
                .signWith(secretKey)
                .compact();
    }

    public LoginUser parseToken(String token) {
        try {
            return doParseToken(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException("error.auth.token_invalid", ex);
        }
    }

    public LoginUser parseTokenOrNull(String token) {
        try {
            return doParseToken(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    private LoginUser doParseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.getIssuer())
                .clockSkewSeconds(jwtProperties.getClockSkewSeconds())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(claims.get(JwtClaimsConstants.USER_ID, String.class));
        loginUser.setUsername(claims.get(JwtClaimsConstants.USERNAME, String.class));
        loginUser.setNickname(claims.get(JwtClaimsConstants.NICKNAME, String.class));
        loginUser.setTenantId(claims.get(JwtClaimsConstants.TENANT_ID, String.class));
        loginUser.setTenantCode(claims.get(JwtClaimsConstants.TENANT_CODE, String.class));
        loginUser.setSessionId(claims.get(JwtClaimsConstants.SESSION_ID, String.class));

        Object permissions = claims.get(JwtClaimsConstants.PERMISSIONS);
        if (permissions instanceof List<?> permissionList) {
            loginUser.setPermissions(permissionList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList());
        }

        return loginUser;
    }

    public String resolveToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }
        String prefix = jwtProperties.getTokenPrefix();
        if (!authorizationHeader.startsWith(prefix)) {
            return null;
        }
        String token = authorizationHeader.substring(prefix.length()).trim();
        return StringUtils.hasText(token) ? token : null;
    }

    public long getAccessTokenExpireSeconds() {
        return jwtProperties.getAccessTokenExpireSeconds();
    }

    public String getTokenPrefix() {
        return jwtProperties.getTokenPrefix();
    }

    /**
     * Remaining life of a still-valid access token; {@code null} if expired/unparseable.
     */
    public Duration remainingTtl(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(jwtProperties.getIssuer())
                    .clockSkewSeconds(jwtProperties.getClockSkewSeconds())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return Duration.ofSeconds(jwtProperties.getAccessTokenExpireSeconds());
            }
            long seconds = Duration.between(Instant.now(), expiration.toInstant()).getSeconds();
            if (seconds <= 0) {
                return null;
            }
            return Duration.ofSeconds(seconds);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    private SecretKey buildSecretKey(String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("icoolkj.jwt.secret must not be empty");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            try {
                keyBytes = MessageDigest.getInstance("SHA-256").digest(keyBytes);
            } catch (NoSuchAlgorithmException ex) {
                throw new IllegalStateException("Failed to derive JWT secret key", ex);
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
