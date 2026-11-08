package com.icoolkj.platform.common.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Unified API response wrapper.
 * <p>
 * Success: {@code code = 0}. Aligns with platform API conventions.
 */
@Data
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final int SUCCESS_CODE = 0;
    public static final String SUCCESS_MESSAGE = "ok";

    private Integer code;
    private String message;
    /** Stable i18n key when {@link #message} was localized (e.g. {@code error.async.disabled}). */
    private String messageKey;
    private T data;
    private Long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(Integer code, String message, T data) {
        this(code, message, null, data);
    }

    public ApiResponse(Integer code, String message, String messageKey, T data) {
        this.code = code;
        this.message = message;
        this.messageKey = messageKey;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(SUCCESS_CODE, message, data);
    }

    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> fail(Integer code, String message, String messageKey) {
        return new ApiResponse<>(code, message, messageKey, null);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(500, message, null);
    }

    public boolean isSuccess() {
        return SUCCESS_CODE == code;
    }
}
