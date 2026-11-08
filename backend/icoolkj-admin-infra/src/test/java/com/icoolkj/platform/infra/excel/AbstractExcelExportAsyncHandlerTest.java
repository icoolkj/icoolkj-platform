package com.icoolkj.platform.infra.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageQuery;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.excel.ExcelI18nProperty;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.infra.async.AsyncTaskContext;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.vo.ExcelExportAsyncResultVO;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractExcelExportAsyncHandlerTest {

    @Mock
    private InfraFileService infraFileService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void rejectsBlankRequestParam() {
        TestHandler handler = newHandler();
        AsyncTaskContext ctx = new AsyncTaskContext("t1", "test", " ", "excel-export", "t1");
        BusinessException ex = assertThrows(BusinessException.class, () -> handler.handle(ctx));
        assertEquals("error.excel.export_failed", ex.getMessage());
    }

    @Test
    void stagesWorkbookAndReturnsFileMeta() throws Exception {
        InfraFileVO uploaded = new InfraFileVO();
        uploaded.setId("file-1");
        when(infraFileService.storeExcelExportStaging(
                        any(Path.class), eq("t1"), anyString(), anyString()))
                .thenReturn(uploaded);

        TestHandler handler = newHandler();
        AsyncTaskContext ctx = new AsyncTaskContext(
                "t1", "test", "{\"pageNo\":1,\"pageSize\":20}", "excel-export", "t1");
        String json = handler.handle(ctx);

        ExcelExportAsyncResultVO result = objectMapper.readValue(json, ExcelExportAsyncResultVO.class);
        assertEquals("file-1", result.getFileId());
        verify(infraFileService).storeExcelExportStaging(
                any(Path.class),
                eq("t1"),
                ArgumentMatchers.argThat(name -> name != null && name.endsWith(".xlsx")),
                eq("demo excel export"));
    }

    private TestHandler newHandler() {
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage("gen.demo.page.exportFileName", Locale.SIMPLIFIED_CHINESE, "Demo");
        messages.addMessage("gen.demo.page.exportSheetName", Locale.SIMPLIFIED_CHINESE, "Demo");
        messages.addMessage("gen.demo.name.label", Locale.SIMPLIFIED_CHINESE, "Name");
        messages.setUseCodeAsDefaultMessage(true);
        return new TestHandler(infraFileService, objectMapper, new I18nMessageResolver(messages));
    }

    static final class TestHandler extends AbstractExcelExportAsyncHandler<PageQuery, String, ProbeRow> {
        TestHandler(InfraFileService files, ObjectMapper mapper, I18nMessageResolver i18n) {
            super(files, mapper, i18n);
        }

        @Override
        public String taskType() {
            return "test.excel.export";
        }

        @Override
        protected Class<PageQuery> queryType() {
            return PageQuery.class;
        }

        @Override
        protected String businessName() {
            return "demo";
        }

        @Override
        protected Class<ProbeRow> exportRowType() {
            return ProbeRow.class;
        }

        @Override
        protected PageResult<String> loadPage(PageQuery query) {
            return PageResult.of(List.of("a"), 1L, query.getPageNo(), query.getPageSize());
        }

        @Override
        protected ProbeRow toExportRow(String vo) {
            ProbeRow row = new ProbeRow();
            row.name = vo;
            return row;
        }
    }

    public static class ProbeRow {
        @ExcelI18nProperty("gen.demo.name.label")
        private String name;
    }
}
