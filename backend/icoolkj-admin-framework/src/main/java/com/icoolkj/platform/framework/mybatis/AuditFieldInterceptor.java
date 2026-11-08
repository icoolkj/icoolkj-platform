package com.icoolkj.platform.framework.mybatis;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import java.util.Map;

/**
 * Auto-fills audit columns before custom mapper {@code insert}/{@code updateById} executes.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class AuditFieldInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        BaseEntity entity = extractEntity(parameter);
        if (entity != null) {
            String statementId = mappedStatement.getId();
            if (statementId.endsWith(".insert")) {
                AuditFieldHelper.fillInsert(entity);
            } else if (statementId.endsWith(".updateById")) {
                AuditFieldHelper.fillUpdate(entity);
            }
        }
        return invocation.proceed();
    }

    private BaseEntity extractEntity(Object parameter) {
        if (parameter instanceof BaseEntity baseEntity) {
            return baseEntity;
        }
        if (parameter instanceof Map<?, ?> paramMap) {
            for (Object value : paramMap.values()) {
                if (value instanceof BaseEntity baseEntity) {
                    return baseEntity;
                }
            }
        }
        return null;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}
