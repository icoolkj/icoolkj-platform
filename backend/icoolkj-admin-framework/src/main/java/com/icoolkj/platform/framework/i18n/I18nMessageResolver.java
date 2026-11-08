package com.icoolkj.platform.framework.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves message keys using {@link MessageSource} and current request locale.
 */
@Component
public class I18nMessageResolver {

    private final MessageSource messageSource;

    public I18nMessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String resolve(String code, Object... args) {
        if (code == null || code.isBlank()) {
            return "";
        }
        return messageSource.getMessage(code, resolveArgs(args), code, LocaleContextHolder.getLocale());
    }

    /** Resolve nested message keys in exception placeholders (e.g. entity.name.user). */
    public Object[] resolveArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return args;
        }
        Object[] resolved = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            resolved[i] = resolveArg(args[i]);
        }
        return resolved;
    }

    private Object resolveArg(Object arg) {
        if (arg instanceof String str && isNestedMessageKey(str)) {
            return messageSource.getMessage(str, null, str, LocaleContextHolder.getLocale());
        }
        return arg;
    }

    private static boolean isNestedMessageKey(String value) {
        return value.startsWith("entity.name.");
    }
}
