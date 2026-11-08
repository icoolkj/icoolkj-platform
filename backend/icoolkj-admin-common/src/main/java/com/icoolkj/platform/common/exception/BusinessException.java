package com.icoolkj.platform.common.exception;

/**
 * Business-level exception with API error code.
 * {@code message} may be an i18n key (e.g. {@code error.menu.not_found}) or a literal fallback.
 */
public class BusinessException extends RuntimeException {

    private final int code;
    private final transient Object[] args;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.args = null;
    }

    public BusinessException(String message, Object... args) {
        super(message);
        this.code = 400;
        this.args = normalizeArgs(args);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.args = null;
    }

    public BusinessException(int code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = normalizeArgs(args);
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.args = null;
    }

    public int getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }

    private static Object[] normalizeArgs(Object[] args) {
        return args == null || args.length == 0 ? null : args;
    }
}
