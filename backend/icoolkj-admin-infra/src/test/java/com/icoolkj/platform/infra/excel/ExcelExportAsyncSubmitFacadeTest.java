package com.icoolkj.platform.infra.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.infra.async.AsyncTaskRuntime;
import com.icoolkj.platform.infra.vo.AsyncTaskSubmitVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelExportAsyncSubmitFacadeTest {

    @Mock
    private AsyncTaskRuntime asyncTaskRuntime;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void submitPersistsQueryJsonAndReturnsTaskId() throws Exception {
        when(asyncTaskRuntime.submitWithId(
                        anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenAnswer(inv -> inv.getArgument(0));
        ExcelExportAsyncSubmitFacade facade =
                new ExcelExportAsyncSubmitFacade(asyncTaskRuntime, objectMapper);

        AsyncTaskSubmitVO vo = facade.submit("excel.export.demo", "Demo Export", Map.of("name", "a"));

        ArgumentCaptor<String> paramCap = ArgumentCaptor.forClass(String.class);
        verify(asyncTaskRuntime).submitWithId(
                eq(vo.taskId()),
                eq("excel.export.demo"),
                eq("Demo Export"),
                eq(ExcelExportAsyncConstants.BIZ_TYPE),
                eq(vo.taskId()),
                paramCap.capture());
        assertEquals("{\"name\":\"a\"}", paramCap.getValue());
    }

    @Test
    void submitRejectsBlankTaskName() {
        ExcelExportAsyncSubmitFacade facade =
                new ExcelExportAsyncSubmitFacade(asyncTaskRuntime, objectMapper);
        assertThrows(BusinessException.class, () -> facade.submit("excel.export.demo", " ", Map.of()));
    }
}
