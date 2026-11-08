package com.icoolkj.platform.framework.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Blocks APIs until the user completes MFA enrollment ({@code mustEnrollMfa}).
 * Runs after {@link ForcePasswordChangeFilter} so password change takes priority.
 */
public class ForceMfaEnrollFilter extends OncePerRequestFilter {

    private static final String ERROR_MUST_ENROLL = "error.auth.must_enroll_mfa";

    private static final List<String> ALLOWED_PATTERNS = List.of(
            "/auth/me",
            "/auth/logout",
            "/auth/password",
            "/auth/profile",
            "/auth/preference",
            "/auth/mfa/**",
            "/system/i18n/**",
            "/system/config/bootstrap"
    );

    private final LoginSessionService loginSessionService;
    private final ObjectMapper objectMapper;
    private final I18nMessageResolver i18n;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ForceMfaEnrollFilter(LoginSessionService loginSessionService,
                                ObjectMapper objectMapper,
                                I18nMessageResolver i18n) {
        this.loginSessionService = loginSessionService;
        this.objectMapper = objectMapper;
        this.i18n = i18n;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!StringUtils.hasText(loginUser.getSessionId())) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean mustEnroll = loginSessionService.getSession(loginUser.getSessionId())
                .map(s -> s.isMustEnrollMfa())
                .orElse(false);
        if (!mustEnroll) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = resolveLookupPath(request);
        if (isAllowed(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        ApiResponse<Void> body = ApiResponse.fail(403, i18n.resolve(ERROR_MUST_ENROLL));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private boolean isAllowed(String path) {
        for (String pattern : ALLOWED_PATTERNS) {
            if (pathMatcher.match(pattern, path)) {
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
}
