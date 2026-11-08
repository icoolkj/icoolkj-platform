package com.icoolkj.platform.framework.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/** Permits all requests when {@code icoolkj.security.enabled=false} (e.g. worker process). */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "icoolkj.security", name = "enabled", havingValue = "false")
public class SecurityDisabledConfig {

    @Bean
    public SecurityFilterChain workerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
