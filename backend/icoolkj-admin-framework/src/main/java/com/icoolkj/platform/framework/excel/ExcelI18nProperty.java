package com.icoolkj.platform.framework.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel column header resolved via {@link com.icoolkj.platform.framework.i18n.I18nMessageResolver}
 * (typically {@code gen.{business}.{field}.label}).
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelI18nProperty {

    /** Message key, e.g. {@code gen.user.username.label}. */
    String value();
}
