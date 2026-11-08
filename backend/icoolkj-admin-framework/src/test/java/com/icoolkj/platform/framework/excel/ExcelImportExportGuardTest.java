package com.icoolkj.platform.framework.excel;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageQuery;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelImportExportGuardTest {

    @Test
    void rejectedReportsUncappedErrorTotal() {
        List<ExcelImportRowError> errors = java.util.stream.IntStream.range(0, 60)
                .mapToObj(i -> new ExcelImportRowError(i + 2, "e" + i))
                .toList();
        ExcelImportResult result = ExcelImportResult.rejected(60, 0, errors);
        assertEquals(60, result.errorTotal());
        assertEquals(ExcelImportResult.MAX_REPORTED_ERRORS, result.errors().size());
        assertTrue(result.hasErrors());
    }

    @Test
    void assertXlsxUploadRejectsWrongExtensionAndMagic() {
        MultipartFile badExt = new MockMultipartFile(
                "file", "a.xls", "application/vnd.ms-excel", new byte[]{0x50, 0x4B, 0x03, 0x04});
        assertEquals(
                "error.excel.import_xlsx_only",
                assertThrows(BusinessException.class, () -> ExcelImportSupport.assertXlsxUpload(badExt)).getMessage());

        MultipartFile badMagic = new MockMultipartFile(
                "file", "a.xlsx", "application/octet-stream", "not-a-zip".getBytes(StandardCharsets.UTF_8));
        assertEquals(
                "error.excel.import_xlsx_only",
                assertThrows(BusinessException.class, () -> ExcelImportSupport.assertXlsxUpload(badMagic)).getMessage());
    }

    @Test
    void writeLocalizedPagedFailsBeforeHeadersWhenTooLarge() {
        PageQuery query = new PageQuery();
        MockHttpServletResponse response = new MockHttpServletResponse();
        StaticMessageSource messages = new StaticMessageSource();
        messages.setUseCodeAsDefaultMessage(true);
        I18nMessageResolver i18n = new I18nMessageResolver(messages);
        AtomicInteger loads = new AtomicInteger();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                ExcelExportSupport.writeLocalizedPaged(
                        response,
                        "demo",
                        ProbeRow.class,
                        query,
                        q -> {
                            loads.incrementAndGet();
                            return PageResult.of(List.of(), (long) ExcelExportSupport.MAX_ROWS + 1, 1L, 500L);
                        },
                        vo -> {
                            ProbeRow row = new ProbeRow();
                            row.name = String.valueOf(vo);
                            return row;
                        },
                        i18n));
        assertEquals("error.excel.export_too_large", ex.getMessage());
        assertFalse(response.containsHeader(ExcelFileNames.HEADER_DOWNLOAD_FILENAME));
        assertEquals(1, loads.get());
        assertEquals(1L, query.getPageNo()); // restored default after finally
    }

    @Test
    void writeLocalizedPagedToFailsBeforeWritingWhenTooLarge() throws Exception {
        PageQuery query = new PageQuery();
        query.setPageNo(2L);
        query.setPageSize(50L);
        StaticMessageSource messages = new StaticMessageSource();
        messages.setUseCodeAsDefaultMessage(true);
        I18nMessageResolver i18n = new I18nMessageResolver(messages);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        AtomicInteger loads = new AtomicInteger();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                ExcelExportSupport.writeLocalizedPagedTo(
                        out,
                        "demo",
                        ProbeRow.class,
                        query,
                        q -> {
                            loads.incrementAndGet();
                            return PageResult.of(List.of(), (long) ExcelExportSupport.MAX_ROWS + 1, 1L, 500L);
                        },
                        vo -> {
                            ProbeRow row = new ProbeRow();
                            row.name = String.valueOf(vo);
                            return row;
                        },
                        i18n));
        assertEquals("error.excel.export_too_large", ex.getMessage());
        assertEquals(0, out.size());
        assertEquals(1, loads.get());
        assertEquals(2L, query.getPageNo());
        assertEquals(50L, query.getPageSize());
    }

    @Test
    void writeLocalizedPagedToWritesXlsxMeta() {
        PageQuery query = new PageQuery();
        StaticMessageSource messages = new StaticMessageSource();
        messages.setUseCodeAsDefaultMessage(true);
        I18nMessageResolver i18n = new I18nMessageResolver(messages);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        ExcelExportSupport.ExcelExportFileMeta meta = ExcelExportSupport.writeLocalizedPagedTo(
                out,
                "demo",
                ProbeRow.class,
                query,
                q -> PageResult.of(List.of("n1"), 1L, 1L, 500L),
                vo -> {
                    ProbeRow row = new ProbeRow();
                    row.name = vo;
                    return row;
                },
                i18n);
        assertTrue(out.size() > 4);
        byte[] bytes = out.toByteArray();
        assertEquals(0x50, bytes[0] & 0xFF); // PK zip magic
        assertEquals(0x4B, bytes[1] & 0xFF);
        assertTrue(meta.displayName().endsWith(".xlsx"));
        assertTrue(meta.asciiName().startsWith("demo_"));
    }

    public static class ProbeRow {
        @ExcelI18nProperty("gen.demo.name.label")
        private String name;
    }
}
