package com.icoolkj.platform.framework.security.filter;

import com.icoolkj.platform.framework.jwt.JwtProperties;
import com.icoolkj.platform.framework.jwt.JwtTokenService;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.framework.security.token.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Parses JWT from the Authorization header, validates blacklist + Redis session, and establishes the security context.
 * <p>Ignore-urls remain permitAll，但若请求携带有效 Token（如登录后刷新 {@code /system/config/bootstrap}），
 * 仍会建立 SecurityContext，以便按租户解析品牌等匿名接口上的可选鉴权数据。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final LoginSessionService loginSessionService;
    private final TokenBlacklistService tokenBlacklistService;
    private final List<String> ignoreUrls;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   JwtProperties jwtProperties,
                                   LoginSessionService loginSessionService,
                                   TokenBlacklistService tokenBlacklistService,
                                   List<String> ignoreUrls) {
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.loginSessionService = loginSessionService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.ignoreUrls = ignoreUrls;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        boolean ignored = matchIgnore(resolveLookupPath(request));
        // ignore 路径仍尝试可选鉴权（有 Token 则写入上下文；无 Token 保持匿名）
        tryAuthenticate(request, ignored);
        filterChain.doFilter(request, response);
    }

    /**
     * @param ignoredPath true 时仅 Header Token；false 时另允许受控下载路径的 query access_token
     */
    private void tryAuthenticate(HttpServletRequest request, boolean ignoredPath) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        String headerValue = request.getHeader(jwtProperties.getHeaderName());
        String token = jwtTokenService.resolveToken(headerValue);
        if (!StringUtils.hasText(token) && !ignoredPath && isGetLike(request)
                && allowQueryAccessToken(resolveLookupPath(request))) {
            token = normalizeQueryToken(request.getParameter("access_token"));
        }
        if (!StringUtils.hasText(token)) {
            return;
        }
        if (tokenBlacklistService.isBlacklisted(token)) {
            return;
        }
        LoginUser loginUser = jwtTokenService.parseTokenOrNull(token);
        if (loginUser == null || !isSessionActive(loginUser)) {
            return;
        }
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        authentication.setDetails(loginUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isSessionActive(LoginUser loginUser) {
        if (!StringUtils.hasText(loginUser.getSessionId()) || !StringUtils.hasText(loginUser.getUserId())) {
            return false;
        }
        return loginSessionService.isSessionValid(loginUser.getSessionId(), loginUser.getUserId());
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

    private boolean isGetLike(HttpServletRequest request) {
        String method = request.getMethod();
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
    }

    private boolean allowQueryAccessToken(String requestPath) {
        return pathMatcher.match("/infra/file/*/download", requestPath)
                || pathMatcher.match("/infra/file/**/download", requestPath)
                || pathMatcher.match("/system/notice/content-file/*/download", requestPath)
                || pathMatcher.match("/infra/databaseBackup/*/download", requestPath);
    }

    private String normalizeQueryToken(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return null;
        }
        String token = rawToken.trim();
        String prefix = jwtProperties.getTokenPrefix();
        if (StringUtils.hasText(prefix) && token.startsWith(prefix)) {
            token = token.substring(prefix.length()).trim();
        }
        return StringUtils.hasText(token) ? token : null;
    }
}
