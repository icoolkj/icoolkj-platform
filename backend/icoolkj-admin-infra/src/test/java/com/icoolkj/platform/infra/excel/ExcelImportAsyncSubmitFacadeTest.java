package com.icoolkj.platform.infra.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.infra.async.AsyncTaskRuntime;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.vo.AsyncTaskSubmitVO;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelImportAsyncSubmitFacadeTest {

    @Mock
    private InfraFileService infraFileService;
    @Mock
    private AsyncTaskRuntime asyncTaskRuntime;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void submitReleasesStagingWhenTaskInsertFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "a.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{0x50, 0x4B, 0x03, 0x04, 0, 0, 0, 0});
        InfraFileVO uploaded = new InfraFileVO();
        uploaded.setId("f1");
        uploaded.setTenantId("t1");
        when(infraFileService.uploadExcelImportStaging(any(), anyString(), anyString())).thenReturn(uploaded);
        doThrow(new BusinessException("error.async.disabled"))
                .when(asyncTaskRuntime)
                .submitWithId(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

        ExcelImportAsyncSubmitFacade facade =
                new ExcelImportAsyncSubmitFacade(infraFileService, asyncTaskRuntime, objectMapper);
        assertThrows(BusinessException.class,
                () -> facade.submit(file, "excel.import.demo", "Demo", "remark"));
        verify(infraFileService).releaseBizFiles(eq("t1"), eq(ExcelImportAsyncConstants.BIZ_TYPE), anyString());
    }

    @Test
    void submitReturnsTaskIdOnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "a.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{0x50, 0x4B, 0x03, 0x04, 0, 0, 0, 0});
        InfraFileVO uploaded = new InfraFileVO();
        uploaded.setId("f1");
        uploaded.setTenantId("t1");
        when(infraFileService.uploadExcelImportStaging(any(), anyString(), anyString())).thenReturn(uploaded);
        when(asyncTaskRuntime.submitWithId(
                        anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenAnswer(inv -> inv.getArgument(0));

        ExcelImportAsyncSubmitFacade facade =
                new ExcelImportAsyncSubmitFacade(infraFileService, asyncTaskRuntime, objectMapper);
        AsyncTaskSubmitVO vo = facade.submit(file, "excel.import.demo", "Demo", "remark");
        assertNotNull(vo.taskId());
        verify(infraFileService, never()).releaseBizFiles(anyString(), anyString(), anyString());
    }
}
