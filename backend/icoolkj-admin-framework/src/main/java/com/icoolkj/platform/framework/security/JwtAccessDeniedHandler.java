package com.icoolkj.platform.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes unified {@link ApiResponse} for forbidden requests.
 */
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final String ERROR_FORBIDDEN = "error.forbidden";

    private final ObjectMapper objectMapper;
    private final I18nMessageResolver i18n;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper, I18nMessageResolver i18n) {
        this.objectMapper = objectMapper;
        this.i18n = i18n;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ApiResponse<Void> body = ApiResponse.fail(403, i18n.resolve(ERROR_FORBIDDEN));

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
