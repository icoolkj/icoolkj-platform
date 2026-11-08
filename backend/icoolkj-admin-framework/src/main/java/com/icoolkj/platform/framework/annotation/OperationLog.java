package com.icoolkj.platform.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface OperationLog {

    String value() default "";

    String module() default "";

    String businessType() default "";

    boolean recordRequest() default true;

    boolean recordResponse() default true;
}
