package com.icoolkj.platform.framework.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.enums.OperationStatusEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.log.ApiAccessLogRecord;
import com.icoolkj.platform.common.model.log.OperationLogRecord;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.framework.web.HttpCharsetUtils;
import com.icoolkj.platform.framework.web.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LogRecordFactory {

    private final ObjectMapper objectMapper;
    private final SensitiveDataMasker masker;
    private final LoggingProperties properties;
    private final I18nMessageResolver i18n;

    public LogRecordFactory(ObjectMapper objectMapper,
                            SensitiveDataMasker masker,
                            LoggingProperties properties,
                            I18nMessageResolver i18n) {
        this.objectMapper = objectMapper;
        this.masker = masker;
        this.properties = properties;
        this.i18n = i18n;
    }

    public String buildRequestParam(HttpServletRequest request) {
        String query = properties.getAccessLog().isIncludeQueryString() ? request.getQueryString() : null;
        if (StringUtils.hasText(query)) {
            query = masker.maskQueryString(query);
        }
        String body = readCachedBody(request);
        Map<String, Object> data = new LinkedHashMap<>();
        if (StringUtils.hasText(query)) {
            data.put("query", query);
        }
        if (StringUtils.hasText(body) && !isMultipart(request)) {
            // Parse JSON body as tree so field-level masking applies (avoid nesting raw JSON string).
            data.put("body", parseBodyForMasking(body));
        }
        if (properties.getAccessLog().isIncludeHeaders()) {
            data.put("headers", headerMap(request));
        }
        if (data.isEmpty()) {
            return null;
        }
        return masker.maskObject(data, properties.getAccessLog().getMaxBodyLength());
    }

    private Object parseBodyForMasking(String body) {
        String trimmed = body.trim();
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) {
            return body;
        }
        try {
            return objectMapper.readTree(trimmed);
        } catch (Exception ex) {
            return body;
        }
    }

    public String buildMethodArguments(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }
        return masker.maskObject(args, properties.getAccessLog().getMaxBodyLength());
    }

    public ApiAccessLogRecord newAccessRecord(HttpServletRequest request,
                                              int httpStatus,
                                              String responseBody,
                                              long startMillis) {
        ApiAccessLogRecord record = new ApiAccessLogRecord();
        record.setTenantId(TenantContextHolder.getTenantId());
        record.setTraceId(LogContext.getTraceId());
        record.setRequestUri(request.getRequestURI());
        record.setRequestMethod(request.getMethod());
        record.setClientIp(HttpRequestUtils.getClientIp(request));
        record.setUserId(SecurityContextHelper.getUserId());
        record.setUsername(SecurityContextHelper.getUsername());
        record.setRequestParam(buildRequestParam(request));
        fillResponse(record, httpStatus, responseBody);
        record.setCostTime(toCostMillis(startMillis));
        record.setAccessedAt(LocalDateTime.now());
        return record;
    }

    public OperationLogRecord newOperationRecord(HttpServletRequest request,
                                                 ProceedingJoinPoint joinPoint,
                                                 String moduleName,
                                                 String businessType,
                                                 Object result,
                                                 Throwable error,
                                                 long startMillis) {
        OperationLogRecord record = new OperationLogRecord();
        record.setTenantId(TenantContextHolder.getTenantId());
        record.setTraceId(LogContext.getTraceId());
        record.setModuleName(moduleName);
        record.setBusinessType(businessType);
        record.setMethod(formatOperationMethod(joinPoint));
        record.setRequestMethod(request == null ? null : request.getMethod());
        record.setOperatorId(SecurityContextHelper.getUserId());
        record.setOperatorName(SecurityContextHelper.getUsername());
        record.setOperationUrl(request == null ? null : request.getRequestURI());
        record.setOperationIp(request == null ? null : HttpRequestUtils.getClientIp(request));
        record.setRequestParam(buildMethodArguments(joinPoint));
        record.setResponseResult(error == null ? masker.maskObject(result, properties.getAccessLog().getMaxBodyLength()) : null);
        record.setOperationStatus(error == null
                ? OperationStatusEnum.SUCCESS.getCode()
                : OperationStatusEnum.FAIL.getCode());
        record.setErrorMessage(error == null ? null : masker.mask(resolveErrorMessage(error), 1024));
        record.setCostTime(toCostMillis(startMillis));
        record.setOperationAt(LocalDateTime.now());
        return record;
    }

    public String readCachedBody(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper == null) {
            return null;
        }
        return HttpCharsetUtils.decodeCachedBytes(
                wrapper.getContentAsByteArray(),
                wrapper.getContentType(),
                wrapper.getCharacterEncoding());
    }

    private void fillResponse(ApiAccessLogRecord record, int httpStatus, String responseBody) {
        record.setResponseCode(httpStatus);
        if (!StringUtils.hasText(responseBody)) {
            record.setResponseMessage(null);
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            if (node.has("code")) {
                record.setResponseCode(node.get("code").asInt(httpStatus));
            }
            if (node.has("message")) {
                record.setResponseMessage(node.get("message").asText());
            }
        } catch (Exception ex) {
            record.setResponseMessage(null);
        }
    }

    private String resolveErrorMessage(Throwable error) {
        if (error instanceof BusinessException businessException) {
            return i18n.resolve(businessException.getMessage(), businessException.getArgs());
        }
        return error.getMessage();
    }

    private Map<String, String> headerMap(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name -> headers.put(name, request.getHeader(name)));
        return headers;
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith(MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    private int toCostMillis(long startMillis) {
        long cost = System.currentTimeMillis() - startMillis;
        return cost > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) cost;
    }

    /** 操作日志仅存短方法名，避免长签名超出 sys_operation_log.method(255)。 */
    private String formatOperationMethod(ProceedingJoinPoint joinPoint) {
        if (!(joinPoint.getSignature() instanceof MethodSignature signature)) {
            return joinPoint.getSignature().toShortString();
        }
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }
}
