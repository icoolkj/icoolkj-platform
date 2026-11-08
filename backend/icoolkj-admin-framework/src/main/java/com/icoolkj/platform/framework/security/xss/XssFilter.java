package com.icoolkj.platform.framework.security.xss;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Escapes request parameter/header text. JSON bodies are not rewritten (rely on
 * validation + notice-specific HTML sanitize).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new XssHttpServletRequestWrapper(request), response);
    }
}
