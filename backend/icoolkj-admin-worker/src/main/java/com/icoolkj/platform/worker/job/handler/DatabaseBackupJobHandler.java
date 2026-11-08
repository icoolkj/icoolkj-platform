package com.icoolkj.platform.worker.job.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.framework.job.JobContext;
import com.icoolkj.platform.framework.job.JobHandler;
import com.icoolkj.platform.framework.job.ManagedJobCatalog;
import com.icoolkj.platform.infra.backup.DatabaseBackupService;
import com.icoolkj.platform.infra.vo.DatabaseBackupRunResultVO;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DatabaseBackupJobHandler implements JobHandler {

    private final DatabaseBackupService databaseBackupService;
    private final ObjectMapper objectMapper;

    public DatabaseBackupJobHandler(DatabaseBackupService databaseBackupService, ObjectMapper objectMapper) {
        this.databaseBackupService = databaseBackupService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String jobCode() {
        return ManagedJobCatalog.DATABASE_BACKUP;
    }

    @Override
    public void execute(JobContext context) throws Exception {
        DatabaseBackupRunResultVO result = databaseBackupService.runManualBackup();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("success", result.isSuccess());
        summary.put("message", result.getMessage());
        summary.put("fileName", result.getFileName());
        summary.put("sizeBytes", result.getSizeBytes());
        context.setResultSummary(objectMapper.writeValueAsString(summary));
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage() == null ? "backup failed" : result.getMessage());
        }
    }
}
