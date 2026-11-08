package com.icoolkj.platform.framework.config;

import com.icoolkj.platform.framework.logging.LoggingProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AuditLogAsyncConfig {

    public static final String AUDIT_LOG_TASK_EXECUTOR = "auditLogTaskExecutor";

    @Bean(AUDIT_LOG_TASK_EXECUTOR)
    @ConditionalOnMissingBean(name = AUDIT_LOG_TASK_EXECUTOR)
    public Executor auditLogTaskExecutor(LoggingProperties loggingProperties) {
        LoggingProperties.AsyncExecutor properties = loggingProperties.getAsyncExecutor();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Math.max(1, properties.getCorePoolSize()));
        executor.setMaxPoolSize(Math.max(executor.getCorePoolSize(), properties.getMaxPoolSize()));
        executor.setQueueCapacity(Math.max(0, properties.getQueueCapacity()));
        executor.setThreadNamePrefix(StringUtils.hasText(properties.getThreadNamePrefix())
                ? properties.getThreadNamePrefix()
                : "audit-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(Math.max(0, properties.getAwaitTerminationSeconds()));
        executor.initialize();
        return executor;
    }
}
