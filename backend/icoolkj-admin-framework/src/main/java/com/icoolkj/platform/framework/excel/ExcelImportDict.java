package com.icoolkj.platform.framework.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an ImportRow field as a dictionary column. Template cells get a dropdown of
 * localized labels; import accepts <strong>label or code</strong> and must resolve to code
 * via {@link ExcelImportLookup#resolveDictCode(String, String)}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelImportDict {

    /** Dict type key, e.g. {@code sys_gender}. */
    String value();
}
