package com.icoolkj.platform.worker.job.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.framework.job.JobContext;
import com.icoolkj.platform.framework.job.JobHandler;
import com.icoolkj.platform.framework.job.ManagedJobCatalog;
import com.icoolkj.platform.infra.async.AsyncProperties;
import com.icoolkj.platform.infra.async.AsyncTaskReconciliationResult;
import com.icoolkj.platform.infra.async.AsyncTaskReconciliationService;
import com.icoolkj.platform.infra.excel.ExcelImportStagingCleanupService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AsyncTaskPendingReconcileJobHandler implements JobHandler {

    private final AsyncTaskReconciliationService reconciliationService;
    private final ExcelImportStagingCleanupService stagingCleanupService;
    private final AsyncProperties asyncProperties;
    private final ObjectMapper objectMapper;

    public AsyncTaskPendingReconcileJobHandler(AsyncTaskReconciliationService reconciliationService,
                                               ExcelImportStagingCleanupService stagingCleanupService,
                                               AsyncProperties asyncProperties,
                                               ObjectMapper objectMapper) {
        this.reconciliationService = reconciliationService;
        this.stagingCleanupService = stagingCleanupService;
        this.asyncProperties = asyncProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String jobCode() {
        return ManagedJobCatalog.ASYNC_TASK_PENDING_RECONCILE;
    }

    @Override
    public void execute(JobContext context) throws Exception {
        AsyncProperties.Reconciliation cfg = asyncProperties.getReconciliation();
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("enabled", cfg != null && cfg.isEnabled());
        request.put("pendingTimeoutSeconds", cfg == null ? null : cfg.getPendingTimeoutSeconds());
        request.put("runningTimeoutSeconds", cfg == null ? null : cfg.getRunningTimeoutSeconds());
        request.put("maxRequeueAttempts", cfg == null ? null : cfg.getMaxRequeueAttempts());
        request.put("batchSize", cfg == null ? null : cfg.getBatchSize());
        request.put("excelImportStagingRetentionDays",
                cfg == null ? null : cfg.getExcelImportStagingRetentionDays());
        request.put("excelImportOrphanStagingHours",
                cfg == null ? null : cfg.getExcelImportOrphanStagingHours());
        context.setRequestParam(objectMapper.writeValueAsString(request));

        AsyncTaskReconciliationResult result = reconciliationService.reconcile();
        int stagingReleased = stagingCleanupService.releaseExpiredStaging();
        int orphanReleased = stagingCleanupService.releaseOrphanStaging();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("pendingScanned", result.pendingScanned());
        summary.put("requeued", result.requeued());
        summary.put("pendingExhausted", result.pendingExhausted());
        summary.put("runningTimedOut", result.runningTimedOut());
        summary.put("skipped", result.skipped());
        summary.put("excelImportStagingReleased", stagingReleased);
        summary.put("excelImportOrphanStagingReleased", orphanReleased);
        context.setResultSummary(objectMapper.writeValueAsString(summary));
    }
}
