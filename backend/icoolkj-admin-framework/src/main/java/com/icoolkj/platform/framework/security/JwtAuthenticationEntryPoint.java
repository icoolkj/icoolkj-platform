package com.icoolkj.platform.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes unified {@link ApiResponse} for unauthenticated requests.
 */
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String ERROR_UNAUTHORIZED = "error.unauthorized";

    private final ObjectMapper objectMapper;
    private final I18nMessageResolver i18n;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper, I18nMessageResolver i18n) {
        this.objectMapper = objectMapper;
        this.i18n = i18n;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ApiResponse<Void> body = ApiResponse.fail(401, i18n.resolve(ERROR_UNAUTHORIZED));

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
