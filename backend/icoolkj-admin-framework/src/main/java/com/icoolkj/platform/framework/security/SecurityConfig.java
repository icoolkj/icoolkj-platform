package com.icoolkj.platform.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.framework.jwt.JwtProperties;
import com.icoolkj.platform.framework.jwt.JwtTokenService;
import com.icoolkj.platform.framework.security.filter.ForceMfaEnrollFilter;
import com.icoolkj.platform.framework.security.filter.ForcePasswordChangeFilter;
import com.icoolkj.platform.framework.security.filter.IpAccessFilter;
import com.icoolkj.platform.framework.security.filter.JwtAuthenticationFilter;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.framework.security.token.TokenBlacklistService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Stateless JWT security configuration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(prefix = "icoolkj.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(SecurityProperties securityProperties) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        SecurityProperties.Cors cors = securityProperties.getCors();
        List<String> origins = cors.getAllowedOrigins() == null
                ? List.of()
                : cors.getAllowedOrigins().stream()
                .filter(origin -> origin != null && !origin.isBlank())
                .map(String::trim)
                .toList();
        if (origins.isEmpty()) {
            // No whitelist: do not register a rejecting CorsConfiguration.
            // Vite proxy / same-origin still send Origin; empty allowedOrigins would 403 them.
            // True cross-origin browsers fail without ACA-O (never use *).
            return source;
        }
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowCredentials(cors.isAllowCredentials());
        config.setAllowedMethods(cors.getAllowedMethods());
        config.setAllowedHeaders(cors.getAllowedHeaders());
        List<String> exposed = cors.getExposedHeaders() == null || cors.getExposedHeaders().isEmpty()
                ? List.of("Content-Disposition", "X-Download-Filename", "X-Export-Filename")
                : cors.getExposedHeaders();
        config.setExposedHeaders(exposed);
        config.setMaxAge(cors.getMaxAgeSeconds());
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(ObjectMapper objectMapper,
                                                                   I18nMessageResolver i18n) {
        return new JwtAuthenticationEntryPoint(objectMapper, i18n);
    }

    @Bean
    public JwtAccessDeniedHandler jwtAccessDeniedHandler(ObjectMapper objectMapper,
                                                         I18nMessageResolver i18n) {
        return new JwtAccessDeniedHandler(objectMapper, i18n);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtTokenService jwtTokenService,
                                                   JwtProperties jwtProperties,
                                                   LoginSessionService loginSessionService,
                                                   TokenBlacklistService tokenBlacklistService,
                                                   IpAccessGuard ipAccessGuard,
                                                   ObjectMapper objectMapper,
                                                   I18nMessageResolver i18n,
                                                   SecurityProperties securityProperties,
                                                   JwtAuthenticationEntryPoint authenticationEntryPoint,
                                                   JwtAccessDeniedHandler accessDeniedHandler) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
                jwtTokenService,
                jwtProperties,
                loginSessionService,
                tokenBlacklistService,
                securityProperties.getIgnoreUrls()
        );
        IpAccessFilter ipAccessFilter = new IpAccessFilter(
                ipAccessGuard,
                objectMapper,
                i18n,
                securityProperties.getIgnoreUrls()
        );
        ForcePasswordChangeFilter forcePasswordChangeFilter = new ForcePasswordChangeFilter(
                loginSessionService,
                objectMapper,
                i18n
        );
        ForceMfaEnrollFilter forceMfaEnrollFilter = new ForceMfaEnrollFilter(
                loginSessionService,
                objectMapper,
                i18n
        );

        String[] permitAllUrls = securityProperties.getIgnoreUrls()
                .toArray(new String[0]);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(frame -> frame.sameOrigin())
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicy(policy -> policy.policy("geolocation=(), microphone=(), camera=()")))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permitAllUrls).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(ipAccessFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(forcePasswordChangeFilter, IpAccessFilter.class)
                .addFilterAfter(forceMfaEnrollFilter, ForcePasswordChangeFilter.class);

        return http.build();
    }
}
