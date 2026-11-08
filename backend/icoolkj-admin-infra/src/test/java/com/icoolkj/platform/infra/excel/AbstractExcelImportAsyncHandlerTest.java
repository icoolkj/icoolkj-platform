package com.icoolkj.platform.infra.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.excel.ExcelImportResult;
import com.icoolkj.platform.framework.tenant.TenantContext;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.infra.async.AsyncTaskContext;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.vo.InfraFileRefVO;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractExcelImportAsyncHandlerTest {

    @Mock
    private InfraFileService infraFileService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setTenant() {
        TenantContextHolder.set(TenantContext.tenant("t1", null));
    }

    @AfterEach
    void clearTenant() {
        TenantContextHolder.clear();
    }

    @Test
    void rejectsMissingFileId() {
        TestHandler handler = new TestHandler(infraFileService, objectMapper);
        AsyncTaskContext ctx = new AsyncTaskContext("t1", "test", "{}", "excel-import", "t1");
        BusinessException ex = assertThrows(BusinessException.class, () -> handler.handle(ctx));
        assertEquals("error.async.file_required", ex.getMessage());
    }

    @Test
    void rejectsWrongBizIdOnFileRow() {
        InfraFileVO file = new InfraFileVO();
        file.setId("f1");
        file.setTenantId("t1");
        file.setBizType(ExcelImportAsyncConstants.BIZ_TYPE);
        file.setBizId("other");
        when(infraFileService.getById("f1")).thenReturn(file);

        TestHandler handler = new TestHandler(infraFileService, objectMapper);
        AsyncTaskContext ctx = new AsyncTaskContext(
                "t1", "test", "{\"fileId\":\"f1\"}", "excel-import", "t1");
        assertThrows(BusinessException.class, () -> handler.handle(ctx));
    }

    @Test
    void rejectsRefOnlyWithoutFileRowAnchor() {
        InfraFileVO file = new InfraFileVO();
        file.setId("f1");
        file.setTenantId("t1");
        file.setBizType("notice");
        file.setBizId("n1");
        when(infraFileService.getById("f1")).thenReturn(file);

        TestHandler handler = new TestHandler(infraFileService, objectMapper);
        AsyncTaskContext ctx = new AsyncTaskContext(
                "t1", "test", "{\"fileId\":\"f1\"}", "excel-import", "t1");
        assertThrows(BusinessException.class, () -> handler.handle(ctx));
    }

    @Test
    void rejectsMissingImportRefRole() {
        InfraFileVO file = new InfraFileVO();
        file.setId("f1");
        file.setTenantId("t1");
        file.setBizType(ExcelImportAsyncConstants.BIZ_TYPE);
        file.setBizId("t1");
        when(infraFileService.getById("f1")).thenReturn(file);
        InfraFileRefVO ref = new InfraFileRefVO();
        ref.setBizType(ExcelImportAsyncConstants.BIZ_TYPE);
        ref.setBizId("t1");
        ref.setRefField("other");
        when(infraFileService.listRefs("f1")).thenReturn(List.of(ref));

        TestHandler handler = new TestHandler(infraFileService, objectMapper);
        AsyncTaskContext ctx = new AsyncTaskContext(
                "t1", "test", "{\"fileId\":\"f1\"}", "excel-import", "t1");
        assertThrows(BusinessException.class, () -> handler.handle(ctx));
    }

    @Test
    void rejectsTenantMismatchOnFileRow() {
        InfraFileVO file = new InfraFileVO();
        file.setId("f1");
        file.setTenantId("other-tenant");
        file.setBizType(ExcelImportAsyncConstants.BIZ_TYPE);
        file.setBizId("t1");
        when(infraFileService.getById("f1")).thenReturn(file);

        TestHandler handler = new TestHandler(infraFileService, objectMapper);
        AsyncTaskContext ctx = new AsyncTaskContext(
                "t1", "test", "{\"fileId\":\"f1\"}", "excel-import", "t1");
        assertThrows(BusinessException.class, () -> handler.handle(ctx));
    }

    static final class TestHandler extends AbstractExcelImportAsyncHandler<Object> {
        TestHandler(InfraFileService files, ObjectMapper mapper) {
            super(files, mapper);
        }

        @Override
        public String taskType() {
            return "test.excel.import";
        }

        @Override
        protected Class<Object> importRowType() {
            return Object.class;
        }

        @Override
        protected ExcelImportResult importRows(List<Object> rows) {
            return ExcelImportResult.ok(0, 0);
        }
    }
}
