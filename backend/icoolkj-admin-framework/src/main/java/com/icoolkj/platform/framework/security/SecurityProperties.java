package com.icoolkj.platform.framework.security;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "icoolkj.security")
public class SecurityProperties {

    private boolean enabled = true;
    private List<String> ignoreUrls = new ArrayList<>();
    private Password password = new Password();
    private Login login = new Login();
    private Cors cors = new Cors();
    private PasswordTransport passwordTransport = new PasswordTransport();

    @Data
    public static class Password {

        private int bcryptStrength = 10;
    }

    @Data
    public static class PasswordTransport {

        private boolean enabled = true;
        private boolean allowPlaintext = false;
        private long maxSkewSeconds = 120L;
    }

    @Data
    public static class Login {

        private int maxRetryCount = 5;
        private int lockMinutes = 15;
        private boolean allowMultiLogin = true;
        /**
         * When {@code allowMultiLogin=false}: {@code true} kicks existing sessions on new login;
         * {@code false} rejects the new login while another session is active.
         */
        private boolean singleLoginKickoutOldest = true;
        /** YAML fallback when {@code security.login.require-captcha} is absent from sys_config. */
        private boolean requireCaptcha = false;
    }

    /**
     * Browser CORS whitelist. Empty {@code allowedOrigins} means no CORS policy bean entries
     * (same-origin / Vite proxy / gateway reverse-proxy). Never falls back to {@code *}.
     * Cross-origin frontends must set explicit origins (e.g. prod {@code CORS_ALLOWED_ORIGINS}).
     */
    @Data
    public static class Cors {

        private List<String> allowedOrigins = new ArrayList<>();
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private List<String> exposedHeaders = new ArrayList<>(List.of(
                "Content-Disposition",
                "X-Download-Filename",
                "X-Export-Filename"));
        private boolean allowCredentials = true;
        private long maxAgeSeconds = 3600L;
    }
}
