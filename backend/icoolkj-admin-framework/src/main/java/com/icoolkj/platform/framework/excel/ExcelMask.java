package com.icoolkj.platform.framework.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Masks ExportRow cell values when writing Excel (PII allowlist projection).
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelMask {

    Type value();

    enum Type {
        PHONE,
        EMAIL,
        NAME,
        IP,
        ID_CARD,
        BANK_CARD
    }
}
