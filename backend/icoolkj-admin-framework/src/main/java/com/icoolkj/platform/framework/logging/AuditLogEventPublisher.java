package com.icoolkj.platform.framework.logging;

import com.icoolkj.platform.common.model.log.ApiAccessLogRecord;
import com.icoolkj.platform.common.model.log.OperationLogRecord;
import com.icoolkj.platform.framework.config.AuditLogAsyncConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class AuditLogEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final Executor auditLogTaskExecutor;
    private final LoggingProperties loggingProperties;

    public AuditLogEventPublisher(ApplicationEventPublisher eventPublisher,
                                  @Qualifier(AuditLogAsyncConfig.AUDIT_LOG_TASK_EXECUTOR) Executor auditLogTaskExecutor,
                                  LoggingProperties loggingProperties) {
        this.eventPublisher = eventPublisher;
        this.auditLogTaskExecutor = auditLogTaskExecutor;
        this.loggingProperties = loggingProperties;
    }

    public void publishAccessLog(ApiAccessLogRecord record) {
        publish(record, loggingProperties.getAccessLog().isAsyncSave());
    }

    public void publishOperationLog(OperationLogRecord record) {
        publish(record, loggingProperties.getOperationLog().isAsyncSave());
    }

    private void publish(Object event, boolean async) {
        if (async) {
            auditLogTaskExecutor.execute(() -> eventPublisher.publishEvent(event));
            return;
        }
        eventPublisher.publishEvent(event);
    }
}
