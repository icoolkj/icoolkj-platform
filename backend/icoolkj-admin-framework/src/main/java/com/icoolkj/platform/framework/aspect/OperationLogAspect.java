package com.icoolkj.platform.framework.aspect;

import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.common.model.log.OperationLogRecord;
import com.icoolkj.platform.framework.logging.AuditLogEventPublisher;
import com.icoolkj.platform.framework.logging.LogRecordFactory;
import com.icoolkj.platform.framework.logging.LoggingProperties;
import com.icoolkj.platform.framework.web.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Aspect
@Component
public class OperationLogAspect {

    private final LogRecordFactory logRecordFactory;
    private final AuditLogEventPublisher auditLogEventPublisher;
    private final LoggingProperties loggingProperties;

    public OperationLogAspect(LogRecordFactory logRecordFactory,
                              AuditLogEventPublisher auditLogEventPublisher,
                              LoggingProperties loggingProperties) {
        this.logRecordFactory = logRecordFactory;
        this.auditLogEventPublisher = auditLogEventPublisher;
        this.loggingProperties = loggingProperties;
    }

    @Around("@within(com.icoolkj.platform.framework.annotation.OperationLog) || @annotation(com.icoolkj.platform.framework.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingProperties.getOperationLog().isEnabled()) {
            return joinPoint.proceed();
        }
        OperationLog operationLog = findOperationLog(joinPoint);
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            publish(joinPoint, operationLog, result, error, start);
        }
    }

    private void publish(ProceedingJoinPoint joinPoint,
                         OperationLog operationLog,
                         Object result,
                         Throwable error,
                         long start) {
        HttpServletRequest request = HttpRequestUtils.getCurrentRequest();
        String module = resolveModule(joinPoint, operationLog);
        String businessType = operationLog == null ? "" : operationLog.businessType();
        OperationLogRecord record = logRecordFactory.newOperationRecord(
                request,
                joinPoint,
                module,
                businessType,
                operationLog == null || operationLog.recordResponse() ? result : null,
                error,
                start
        );
        if (operationLog != null && !operationLog.recordRequest()) {
            record.setRequestParam(null);
        }
        auditLogEventPublisher.publishOperationLog(record);
    }

    private OperationLog findOperationLog(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        OperationLog annotation = AnnotationUtils.findAnnotation(method, OperationLog.class);
        if (annotation != null) {
            return annotation;
        }
        return AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), OperationLog.class);
    }

    private String resolveModule(ProceedingJoinPoint joinPoint, OperationLog operationLog) {
        if (operationLog != null) {
            if (StringUtils.hasText(operationLog.module())) {
                return operationLog.module();
            }
            if (StringUtils.hasText(operationLog.value())) {
                return operationLog.value();
            }
        }
        return joinPoint.getSignature().getDeclaringType().getSimpleName();
    }
}
