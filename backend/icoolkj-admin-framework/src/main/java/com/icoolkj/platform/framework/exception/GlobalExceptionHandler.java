package com.icoolkj.platform.framework.exception;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.exception.ForbiddenException;
import com.icoolkj.platform.common.exception.IpAccessDeniedException;
import com.icoolkj.platform.common.exception.TenantException;
import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps all controller exceptions to unified {@link ApiResponse}.
 * <p>
 * Domain uniqueness messages belong in service pre-checks ({@link BusinessException}).
 * {@link DuplicateKeyException} is only a safety net with a generic i18n message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String ERROR_FORBIDDEN = "error.forbidden";
    private static final String ERROR_DB_DUPLICATE = "error.db.duplicate";

    private final I18nMessageResolver i18n;

    public GlobalExceptionHandler(I18nMessageResolver i18n) {
        this.i18n = i18n;
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        String messageKey = ex.getMessage();
        String message = i18n.resolve(messageKey, ex.getArgs());
        log.warn("Business exception, uri={}, message={}", request.getRequestURI(), message);
        return ApiResponse.fail(ex.getCode(), message, messageKey);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<Void> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
        String messageKey = ex.getMessage();
        String message = i18n.resolve(messageKey, ex.getArgs());
        log.warn("Unauthorized exception, uri={}, message={}", request.getRequestURI(), message);
        return ApiResponse.fail(ex.getCode(), message, messageKey);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ApiResponse<Void> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        String messageKey = ex.getMessage();
        String message = i18n.resolve(messageKey, ex.getArgs());
        log.warn("Forbidden exception, uri={}, message={}", request.getRequestURI(), message);
        return ApiResponse.fail(ex.getCode(), message, messageKey);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        String message = i18n.resolve(ERROR_FORBIDDEN);
        log.warn("Access denied, uri={}, message={}", request.getRequestURI(), message);
        return ApiResponse.fail(403, message, ERROR_FORBIDDEN);
    }

    @ExceptionHandler(IpAccessDeniedException.class)
    public ApiResponse<Void> handleIpAccessDeniedException(IpAccessDeniedException ex, HttpServletRequest request) {
        String message = i18n.resolve(ex.getMessage(), ex.getArgs());
        log.warn("IP access denied, uri={}, message={}", request.getRequestURI(), message);
        return ApiResponse.fail(ex.getCode(), message);
    }

    @ExceptionHandler(TenantException.class)
    public ApiResponse<Void> handleTenantException(TenantException ex, HttpServletRequest request) {
        String message = i18n.resolve(ex.getMessage(), ex.getArgs());
        log.warn("Tenant exception, uri={}, message={}", request.getRequestURI(), message);
        return ApiResponse.fail(ex.getCode(), message);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicateKeyException(DuplicateKeyException ex, HttpServletRequest request) {
        // Specific messages must come from service-layer uniqueness checks; do not map UK names here.
        log.warn("Duplicate key, uri={}, detail={}",
                request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return ApiResponse.fail(400, i18n.resolve(ERROR_DB_DUPLICATE));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                   HttpServletRequest request) {
        String message = buildFieldErrorMessage(ex.getBindingResult().getFieldErrors());
        log.warn("Validation exception, uri={}, message={}", request.getRequestURI(), message);
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException ex, HttpServletRequest request) {
        String message = buildFieldErrorMessage(ex.getBindingResult().getFieldErrors());
        log.warn("Bind exception, uri={}, message={}", request.getRequestURI(), message);
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException ex,
                                                                HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(this::formatConstraintViolation)
                .collect(Collectors.joining("; "));
        log.warn("Constraint violation, uri={}, message={}", request.getRequestURI(), message);
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
                                                                       HttpServletRequest request) {
        String message = i18n.resolve("error.request.param.type_mismatch", ex.getName());
        log.warn("Type mismatch, uri={}, message={}", request.getRequestURI(), message);
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex,
                                                                   HttpServletRequest request) {
        log.warn("Request body unreadable, uri={}, message={}", request.getRequestURI(), ex.getMessage());
        return ApiResponse.fail(400, i18n.resolve("error.request.body.invalid"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Void> handleNoResourceFoundException(NoResourceFoundException ex,
                                                            HttpServletRequest request) {
        log.warn("Resource not found, uri={}", request.getRequestURI());
        return ApiResponse.fail(404, i18n.resolve("error.resource.not_found"));
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex, HttpServletRequest request) {
        log.error("System exception, uri=" + request.getRequestURI(), ex);
        return ApiResponse.fail(500, i18n.resolve("error.system.busy"));
    }

    private String buildFieldErrorMessage(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(error -> error.getField() + ": " + resolveValidationMessage(error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
    }

    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + resolveValidationMessage(violation.getMessage());
    }

    private String resolveValidationMessage(String message) {
        if (message == null) {
            return "";
        }
        String key = message;
        if (key.startsWith("{") && key.endsWith("}")) {
            key = key.substring(1, key.length() - 1);
        }
        return i18n.resolve(key);
    }
}
