package com.icoolkj.platform.framework.aspect;

import com.icoolkj.platform.common.model.page.PageQuery;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Clears client-supplied {@code dataScopeSql} before {@link DataScopeAspect} injects server SQL. */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class DataScopeSqlScrubAspect {

    @Around("execution(* com.icoolkj.platform.api.controller..*(..))")
    public Object scrub(ProceedingJoinPoint joinPoint) throws Throwable {
        clearClientSql(joinPoint.getArgs());
        return joinPoint.proceed();
    }

    static void clearClientSql(Object[] args) {
        if (args == null) {
            return;
        }
        for (Object arg : args) {
            if (arg instanceof PageQuery pageQuery) {
                pageQuery.setDataScopeSql(null);
            } else if (arg instanceof Map<?, ?> map) {
                try {
                    map.remove("dataScopeSql");
                } catch (UnsupportedOperationException ignored) {
                    // immutable request maps
                }
            }
        }
    }
}
