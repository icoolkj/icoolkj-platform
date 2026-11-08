package com.icoolkj.platform.framework.aspect;

import com.icoolkj.platform.framework.annotation.DataScope;
import java.lang.reflect.Method;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Matches methods that declare {@link DataScope}, including on implemented interfaces
 * (required for CGLIB proxies when the annotation lives only on the Service interface).
 */
public final class DataScopePointcut extends StaticMethodMatcherPointcut {

    public DataScopePointcut() {
        setClassFilter(clazz -> AnnotationUtils.isCandidateClass(clazz, DataScope.class));
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        Method specific = AopUtils.getMostSpecificMethod(method, targetClass);
        return AnnotationUtils.findAnnotation(specific, DataScope.class) != null
                || (specific != method && AnnotationUtils.findAnnotation(method, DataScope.class) != null);
    }
}
