package com.icoolkj.platform.framework.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "icoolkj.jwt")
public class JwtProperties {

    private String issuer = "icoolkj-platform";
    private String secret;
    private long accessTokenExpireSeconds = 7200L;
    private long refreshTokenExpireSeconds = 604800L;
    private long clockSkewSeconds = 60L;
    private String tokenPrefix = "Bearer ";
    private String headerName = "Authorization";
}
