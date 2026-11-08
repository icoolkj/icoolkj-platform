package com.icoolkj.platform.worker.job.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.framework.job.JobContext;
import com.icoolkj.platform.framework.job.JobHandler;
import com.icoolkj.platform.framework.job.ManagedJobCatalog;
import com.icoolkj.platform.infra.service.InfraFileGcService;
import com.icoolkj.platform.infra.storage.StorageProperties;
import com.icoolkj.platform.infra.vo.InfraFileGcResult;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InfraFileGcJobHandler implements JobHandler {

    private final StorageProperties storageProperties;
    private final InfraFileGcService infraFileGcService;
    private final ObjectMapper objectMapper;

    public InfraFileGcJobHandler(StorageProperties storageProperties,
                                 InfraFileGcService infraFileGcService,
                                 ObjectMapper objectMapper) {
        this.storageProperties = storageProperties;
        this.infraFileGcService = infraFileGcService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String jobCode() {
        return ManagedJobCatalog.INFRA_FILE_GC;
    }

    @Override
    public void execute(JobContext context) throws Exception {
        StorageProperties.Gc gc = storageProperties.getGc();
        if (gc == null) {
            gc = new StorageProperties.Gc();
        }
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("retentionDays", gc.getRetentionDays());
        request.put("batchSize", gc.getBatchSize());
        request.put("maxBatchesPerRun", gc.getMaxBatchesPerRun());
        context.setRequestParam(objectMapper.writeValueAsString(request));

        int maxBatches = Math.max(gc.getMaxBatchesPerRun(), 1);
        int totalPurged = 0;
        int totalSkipped = 0;
        int totalFailed = 0;
        int batchesRun = 0;
        for (int i = 0; i < maxBatches; i++) {
            InfraFileGcResult batch = infraFileGcService.purgeExpiredBatch();
            batchesRun++;
            totalPurged += batch.getPurged();
            totalSkipped += batch.getSkipped();
            totalFailed += batch.getFailed();
            if (batch.processed() == 0) {
                break;
            }
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("purged", totalPurged);
        summary.put("skipped", totalSkipped);
        summary.put("failed", totalFailed);
        summary.put("batches", batchesRun);
        context.setResultSummary(objectMapper.writeValueAsString(summary));
    }
}
