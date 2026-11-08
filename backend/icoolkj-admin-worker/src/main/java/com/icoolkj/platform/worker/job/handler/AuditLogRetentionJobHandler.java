package com.icoolkj.platform.worker.job.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.framework.job.JobContext;
import com.icoolkj.platform.framework.job.JobHandler;
import com.icoolkj.platform.framework.job.ManagedJobCatalog;
import com.icoolkj.platform.framework.logging.LoggingProperties;
import com.icoolkj.platform.infra.service.ApiAccessLogService;
import com.icoolkj.platform.system.loginlog.service.LoginLogService;
import com.icoolkj.platform.system.operationlog.service.OperationLogService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AuditLogRetentionJobHandler implements JobHandler {

    private final LoggingProperties loggingProperties;
    private final LoginLogService loginLogService;
    private final OperationLogService operationLogService;
    private final ApiAccessLogService apiAccessLogService;
    private final ObjectMapper objectMapper;

    public AuditLogRetentionJobHandler(LoggingProperties loggingProperties,
                                       LoginLogService loginLogService,
                                       OperationLogService operationLogService,
                                       ApiAccessLogService apiAccessLogService,
                                       ObjectMapper objectMapper) {
        this.loggingProperties = loggingProperties;
        this.loginLogService = loginLogService;
        this.operationLogService = operationLogService;
        this.apiAccessLogService = apiAccessLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String jobCode() {
        return ManagedJobCatalog.AUDIT_LOG_RETENTION;
    }

    @Override
    public void execute(JobContext context) throws Exception {
        LoggingProperties.Retention retention = loggingProperties.getRetention();
        if (retention == null) {
            retention = new LoggingProperties.Retention();
        }
        LocalDateTime now = LocalDateTime.now();
        int loginArchived = loginLogService.archiveThenPurge(now.minusDays(retention.getLoginLogDays()));
        int operationArchived = operationLogService.archiveThenPurge(now.minusDays(retention.getOperationLogDays()));
        int apiArchived = apiAccessLogService.archiveThenPurge(now.minusDays(retention.getApiAccessLogDays()));
        int loginArchivePurged = loginLogService.purgeArchiveBefore(now.minusDays(retention.getArchiveLoginLogDays()));
        int operationArchivePurged = operationLogService.purgeArchiveBefore(now.minusDays(retention.getArchiveOperationLogDays()));
        int apiArchivePurged = apiAccessLogService.purgeArchiveBefore(now.minusDays(retention.getArchiveApiAccessLogDays()));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("loginArchived", loginArchived);
        summary.put("operationArchived", operationArchived);
        summary.put("apiArchived", apiArchived);
        summary.put("loginArchivePurged", loginArchivePurged);
        summary.put("operationArchivePurged", operationArchivePurged);
        summary.put("apiArchivePurged", apiArchivePurged);
        context.setResultSummary(objectMapper.writeValueAsString(summary));
    }
}
