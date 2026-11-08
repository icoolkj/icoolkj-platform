package com.icoolkj.platform.infra.excel;

import com.icoolkj.platform.infra.async.AsyncProperties;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import com.icoolkj.platform.infra.mapper.InfraFileMapper;
import com.icoolkj.platform.infra.service.InfraFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelImportStagingCleanupServiceTest {

    @Mock
    private AsyncTaskMapper asyncTaskMapper;
    @Mock
    private InfraFileMapper infraFileMapper;
    @Mock
    private InfraFileService infraFileService;

    private AsyncProperties asyncProperties;
    private ExcelImportStagingCleanupService service;

    @BeforeEach
    void setUp() {
        asyncProperties = new AsyncProperties();
        asyncProperties.setEnabled(true);
        AsyncProperties.Reconciliation reconciliation = new AsyncProperties.Reconciliation();
        reconciliation.setEnabled(true);
        reconciliation.setExcelImportStagingRetentionDays(7);
        reconciliation.setExcelImportOrphanStagingHours(6);
        reconciliation.setBatchSize(10);
        asyncProperties.setReconciliation(reconciliation);
        service = new ExcelImportStagingCleanupService(
                asyncTaskMapper, infraFileMapper, infraFileService, asyncProperties);
    }

    @Test
    void releaseExpiredStagingReleasesBothBizTypes() {
        AsyncTaskEntity importTask = new AsyncTaskEntity();
        importTask.setId("t-import");
        importTask.setTenantId("ten");
        importTask.setBizType(ExcelImportAsyncConstants.BIZ_TYPE);
        AsyncTaskEntity exportTask = new AsyncTaskEntity();
        exportTask.setId("t-export");
        exportTask.setTenantId("ten");
        exportTask.setBizType(ExcelExportAsyncConstants.BIZ_TYPE);
        when(asyncTaskMapper.selectTerminalExcelImportFinishedBefore(any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(importTask, exportTask));

        assertEquals(2, service.releaseExpiredStaging());
        verify(infraFileService).releaseBizFiles("ten", ExcelImportAsyncConstants.BIZ_TYPE, "t-import");
        verify(infraFileService).releaseBizFiles("ten", ExcelExportAsyncConstants.BIZ_TYPE, "t-export");
    }

    @Test
    void releaseOrphanStagingReleasesFileGroups() {
        InfraFileEntity orphan = new InfraFileEntity();
        orphan.setTenantId("ten");
        orphan.setBizType(ExcelExportAsyncConstants.BIZ_TYPE);
        orphan.setBizId("missing-task");
        when(infraFileMapper.selectOrphanExcelImportStagingBefore(any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(orphan));

        assertEquals(1, service.releaseOrphanStaging());
        verify(infraFileService).releaseBizFiles(
                eq("ten"), eq(ExcelExportAsyncConstants.BIZ_TYPE), eq("missing-task"));
    }
}
