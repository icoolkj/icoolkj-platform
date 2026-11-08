package com.icoolkj.platform.framework.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password encoder for credential verification.
 */
@Configuration
public class PasswordEncoderConfig {

    private final SecurityProperties securityProperties;

    public PasswordEncoderConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(securityProperties.getPassword().getBcryptStrength());
    }
}
