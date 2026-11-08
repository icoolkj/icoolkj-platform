package com.icoolkj.platform.framework.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/** Blocks mutating HTTP methods when {@code icoolkj.demo.enabled=true}. */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 15)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "icoolkj.demo", name = "enabled", havingValue = "true")
public class DemoModeFilter extends OncePerRequestFilter {

    private static final String ERROR_DEMO_OPERATION_FORBIDDEN = "error.demo.operation_forbidden";

    private static final Set<String> MUTATING_METHODS = Set.of(
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.DELETE.name()
    );

    private final DemoModeProperties properties;
    private final ObjectMapper objectMapper;
    private final I18nMessageResolver i18n;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public DemoModeFilter(DemoModeProperties properties,
                          ObjectMapper objectMapper,
                          I18nMessageResolver i18n) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.i18n = i18n;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!properties.isEnabled() || !isMutatingMethod(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String lookupPath = resolveLookupPath(request);
        if (matchAllowWrite(lookupPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        writeForbidden(response);
    }

    private boolean isMutatingMethod(String method) {
        return method != null && MUTATING_METHODS.contains(method.toUpperCase());
    }

    private boolean matchAllowWrite(String requestPath) {
        for (String pattern : properties.getAllowWriteUrls()) {
            if (pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    private String resolveLookupPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (request.getPathInfo() != null) {
            return servletPath + request.getPathInfo();
        }
        return servletPath;
    }

    private void writeForbidden(HttpServletResponse response) throws IOException {
        String message = i18n.resolve(ERROR_DEMO_OPERATION_FORBIDDEN);
        ApiResponse<Void> body = ApiResponse.fail(403, message);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
