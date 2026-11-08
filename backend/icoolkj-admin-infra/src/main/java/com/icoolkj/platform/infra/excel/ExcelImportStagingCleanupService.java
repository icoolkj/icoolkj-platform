package com.icoolkj.platform.infra.excel;

import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.async.AsyncProperties;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import com.icoolkj.platform.infra.mapper.InfraFileMapper;
import com.icoolkj.platform.infra.service.InfraFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Release excel-import / excel-export staging for terminal tasks past retention, and orphan files
 * whose task row never landed (upload-before-insert crash).
 */
@Service
public class ExcelImportStagingCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ExcelImportStagingCleanupService.class);

    private final AsyncTaskMapper asyncTaskMapper;
    private final InfraFileMapper infraFileMapper;
    private final InfraFileService infraFileService;
    private final AsyncProperties asyncProperties;

    public ExcelImportStagingCleanupService(AsyncTaskMapper asyncTaskMapper,
                                            InfraFileMapper infraFileMapper,
                                            InfraFileService infraFileService,
                                            AsyncProperties asyncProperties) {
        this.asyncTaskMapper = asyncTaskMapper;
        this.infraFileMapper = infraFileMapper;
        this.infraFileService = infraFileService;
        this.asyncProperties = asyncProperties;
    }

    /** @return number of tasks whose staging refs were released */
    public int releaseExpiredStaging() {
        if (!asyncProperties.isEnabled()) {
            return 0;
        }
        AsyncProperties.Reconciliation cfg = asyncProperties.getReconciliation();
        if (cfg == null || !cfg.isEnabled()) {
            return 0;
        }
        long days = cfg.getExcelImportStagingRetentionDays();
        if (days <= 0) {
            return 0;
        }
        int batch = Math.max(1, cfg.getBatchSize());
        LocalDateTime before = LocalDateTime.now().minusDays(days);
        return TenantScopeRunner.runIgnoringTenant(() -> {
            List<AsyncTaskEntity> rows =
                    asyncTaskMapper.selectTerminalExcelImportFinishedBefore(before, batch);
            if (CollectionUtils.isEmpty(rows)) {
                return 0;
            }
            int released = 0;
            for (AsyncTaskEntity row : rows) {
                if (!StringUtils.hasText(row.getTenantId())
                        || !StringUtils.hasText(row.getId())
                        || !ExcelImportAsyncConstants.isReservedBizType(row.getBizType())) {
                    continue;
                }
                try {
                    infraFileService.releaseBizFiles(row.getTenantId(), row.getBizType(), row.getId());
                    released++;
                } catch (Exception ex) {
                    log.warn("Excel staging release failed taskId={}", row.getId(), ex);
                }
            }
            if (released > 0) {
                log.info("Excel staging released for {} terminal task(s)", released);
            }
            return released;
        });
    }

    /** @return number of orphan staging files released (no matching async task) */
    public int releaseOrphanStaging() {
        if (!asyncProperties.isEnabled()) {
            return 0;
        }
        AsyncProperties.Reconciliation cfg = asyncProperties.getReconciliation();
        if (cfg == null || !cfg.isEnabled()) {
            return 0;
        }
        long hours = cfg.getExcelImportOrphanStagingHours();
        if (hours <= 0) {
            return 0;
        }
        int batch = Math.max(1, cfg.getBatchSize());
        LocalDateTime before = LocalDateTime.now().minusHours(hours);
        return TenantScopeRunner.runIgnoringTenant(() -> {
            List<InfraFileEntity> rows =
                    infraFileMapper.selectOrphanExcelImportStagingBefore(before, batch);
            if (CollectionUtils.isEmpty(rows)) {
                return 0;
            }
            int released = 0;
            for (InfraFileEntity row : rows) {
                if (!StringUtils.hasText(row.getTenantId())
                        || !StringUtils.hasText(row.getBizId())
                        || !ExcelImportAsyncConstants.isReservedBizType(row.getBizType())) {
                    continue;
                }
                try {
                    infraFileService.releaseBizFiles(row.getTenantId(), row.getBizType(), row.getBizId());
                    released++;
                } catch (Exception ex) {
                    log.warn("Excel orphan staging release failed bizId={}", row.getBizId(), ex);
                }
            }
            if (released > 0) {
                log.info("Excel orphan staging released for {} file group(s)", released);
            }
            return released;
        });
    }
}
