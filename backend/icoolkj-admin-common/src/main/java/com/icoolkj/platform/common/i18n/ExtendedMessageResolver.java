package com.icoolkj.platform.common.i18n;

import java.util.Locale;

/**
 * Optional extension for {@code MessageSource} resolution (e.g. low-code gen i18n JSON).
 */
public interface ExtendedMessageResolver {

    /**
     * @return resolved message, or {@code null} if this resolver does not handle the code
     */
    String resolve(String code, Object[] args, Locale locale);
}
