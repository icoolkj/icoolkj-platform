package com.icoolkj.platform.framework.logging;

import com.icoolkj.platform.common.model.log.ApiAccessLogRecord;
import com.icoolkj.platform.framework.web.HttpCharsetUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 20)
public class AccessLogFilter extends OncePerRequestFilter {

    private final LoggingProperties properties;
    private final LogRecordFactory logRecordFactory;
    private final AuditLogEventPublisher auditLogEventPublisher;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AccessLogFilter(LoggingProperties properties,
                           LogRecordFactory logRecordFactory,
                           AuditLogEventPublisher auditLogEventPublisher) {
        this.properties = properties;
        this.logRecordFactory = logRecordFactory;
        this.auditLogEventPublisher = auditLogEventPublisher;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.getAccessLog().isEnabled()) {
            return true;
        }
        String path = request.getRequestURI();
        for (String pattern : properties.getAccessLog().getExcludeUrls()) {
            if (pathMatcher.match(pattern, path) || pathMatcher.match("/api" + pattern, path)) {
                return true;
            }
        }
        return path.startsWith("/api/swagger-ui")
                || path.startsWith("/api/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper requestWrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (requestWrapper == null) {
            requestWrapper = new ContentCachingRequestWrapper(request);
        }
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            String responseBody = readResponseBody(responseWrapper);
            ApiAccessLogRecord record = logRecordFactory.newAccessRecord(
                    requestWrapper,
                    responseWrapper.getStatus(),
                    responseBody,
                    start
            );
            auditLogEventPublisher.publishAccessLog(record);
            responseWrapper.copyBodyToResponse();
        }
    }

    private String readResponseBody(ContentCachingResponseWrapper responseWrapper) {
        return HttpCharsetUtils.decodeCachedBytes(
                responseWrapper.getContentAsByteArray(),
                responseWrapper.getContentType(),
                responseWrapper.getCharacterEncoding());
    }
}
