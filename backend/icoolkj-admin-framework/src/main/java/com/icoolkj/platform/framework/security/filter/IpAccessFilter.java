package com.icoolkj.platform.framework.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.IpAccessDeniedException;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.framework.security.IpAccessGuard;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.web.HttpRequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Enforces ADMIN/ALL IP rules after JWT authentication is established.
 */
public class IpAccessFilter extends OncePerRequestFilter {

    private final IpAccessGuard ipAccessGuard;
    private final ObjectMapper objectMapper;
    private final I18nMessageResolver i18n;
    private final List<String> ignoreUrls;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public IpAccessFilter(IpAccessGuard ipAccessGuard,
                          ObjectMapper objectMapper,
                          I18nMessageResolver i18n,
                          List<String> ignoreUrls) {
        this.ipAccessGuard = ipAccessGuard;
        this.objectMapper = objectMapper;
        this.i18n = i18n;
        this.ignoreUrls = ignoreUrls;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (matchIgnore(resolveLookupPath(request))) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            try {
                ipAccessGuard.ensureAdminAccessAllowed(
                        loginUser.getTenantId(),
                        HttpRequestUtils.getClientIp(request)
                );
            } catch (IpAccessDeniedException ex) {
                writeDenied(response, ex);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeDenied(HttpServletResponse response, IpAccessDeniedException ex) throws IOException {
        String message = i18n.resolve(ex.getMessage(), ex.getArgs());
        ApiResponse<Void> body = ApiResponse.fail(ex.getCode(), message);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private String resolveLookupPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (request.getPathInfo() != null) {
            return servletPath + request.getPathInfo();
        }
        return servletPath;
    }

    private boolean matchIgnore(String requestPath) {
        if (ignoreUrls == null || ignoreUrls.isEmpty()) {
            return false;
        }
        for (String pattern : ignoreUrls) {
            if (pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }
}
