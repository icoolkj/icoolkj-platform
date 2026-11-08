package com.icoolkj.platform.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * L3 list filter. Place only on {@code Service} interface {@code page(...)}.
 * Do not put on Controller or ServiceImpl. See {@code .cursor/rules/row-access-l3.mdc}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataScope {

    String value() default "";

    String deptAlias() default "";

    String userAlias() default "";

    String deptColumn() default "dept_id";

    String userColumn() default "user_id";

    String sqlField() default "dataScopeSql";
}
