package com.icoolkj.platform.framework.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an ImportRow field as a reference typed for humans (name), resolved to id on import.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelImportRef {

    Kind value();

    enum Kind {
        /**
         * Cell shows department name (dropdown of assignable depts). Import resolves to dept id;
         * raw id still accepted when it is in scope.
         */
        DEPT_NAME,
        /**
         * Cell shows post name (dropdown). Import resolves to post id; raw id accepted when present.
         */
        POST_NAME,
        /**
         * Cell shows role name (dropdown). Import resolves to role id; raw id accepted when present.
         */
        ROLE_NAME
    }
}
