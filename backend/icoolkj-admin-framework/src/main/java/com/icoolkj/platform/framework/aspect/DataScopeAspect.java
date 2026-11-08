package com.icoolkj.platform.framework.aspect;

import com.icoolkj.platform.framework.annotation.DataScope;
import com.icoolkj.platform.framework.auth.DataPermissionService;
import com.icoolkj.platform.framework.datascope.DataScopeColumns;
import java.lang.reflect.Method;
import java.util.Map;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Injects L3 {@code dataScopeSql}. Uses a programmatic advisor (not AspectJ {@code @annotation})
 * so annotations on Service <em>interface</em> methods still match under CGLIB
 * ({@code spring.aop.proxy-target-class=true}).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class DataScopeAspect implements MethodInterceptor, Ordered {

    private final DataPermissionService dataPermissionService;

    public DataScopeAspect(DataPermissionService dataPermissionService) {
        this.dataPermissionService = dataPermissionService;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        DataScope dataScope = findDataScope(invocation);
        if (dataScope == null) {
            return invocation.proceed();
        }
        DataScopeColumns.assertLegal(dataScope);
        String sql = dataPermissionService.buildDataScopeSql(dataScope);
        if (sql == null) {
            sql = "";
        }
        if (!injectDataScopeSql(invocation.getArguments(), dataScope.sqlField(), sql)) {
            Method method = invocation.getMethod();
            throw new IllegalStateException(
                    "@DataScope on " + method.getDeclaringClass().getSimpleName() + "." + method.getName()
                            + " requires an injectable argument property '" + dataScope.sqlField()
                            + "' (typically PageQuery.dataScopeSql)");
        }
        return invocation.proceed();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    static DataScope findDataScope(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        Object target = invocation.getThis();
        Class<?> targetClass = (target != null) ? AopUtils.getTargetClass(target) : method.getDeclaringClass();
        Method specific = AopUtils.getMostSpecificMethod(method, targetClass);
        DataScope dataScope = AnnotationUtils.findAnnotation(specific, DataScope.class);
        if (dataScope == null && specific != method) {
            dataScope = AnnotationUtils.findAnnotation(method, DataScope.class);
        }
        return dataScope;
    }

    private boolean injectDataScopeSql(Object[] args, String fieldName, String sql) {
        if (args == null || args.length == 0) {
            return false;
        }
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> writable = (Map<String, Object>) map;
                writable.put(fieldName, sql);
                return true;
            }
            BeanWrapper wrapper = new BeanWrapperImpl(arg);
            if (wrapper.isWritableProperty(fieldName)) {
                wrapper.setPropertyValue(fieldName, sql);
                return true;
            }
        }
        return false;
    }
}
