package com.icoolkj.platform.framework.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageQuery;
import com.icoolkj.platform.common.model.page.PageResult;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelPagingSupportTest {

    @Test
    void loadRowsPagesUntilCompleteWithoutTruncate() {
        List<String> source = List.of("a", "b", "c", "d", "e");
        PageQuery query = new PageQuery();
        query.setPageNo(3L);
        query.setPageSize(20L);
        List<String> loaded = ExcelExportSupport.loadRows(query, q -> {
            long from = (q.getPageNo() - 1) * q.getPageSize();
            long to = Math.min(from + q.getPageSize(), source.size());
            if (from >= source.size()) {
                return PageResult.of(List.of(), (long) source.size(), q.getPageNo(), q.getPageSize());
            }
            return PageResult.of(source.subList((int) from, (int) to), (long) source.size(), q.getPageNo(), q.getPageSize());
        });
        assertEquals(source, loaded);
        assertEquals(3L, query.getPageNo());
        assertEquals(20L, query.getPageSize());
    }

    @Test
    void loadRowsFailsWhenTotalExceedsMax() {
        PageQuery query = new PageQuery();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                ExcelExportSupport.loadRows(query, q ->
                        PageResult.of(List.of("x"), (long) ExcelExportSupport.MAX_ROWS + 1, 1L, 500L)));
        assertEquals("error.excel.export_too_large", ex.getMessage());
    }

    @Test
    void readInBatchesInvokesHandlerPerBatch() {
        List<DemoImportRow> seed = List.of(
                row("a"), row("b"), row("c"), row("d"), row("e"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        com.alibaba.excel.EasyExcel.write(out, DemoImportRow.class).sheet("t").doWrite(seed);

        AtomicInteger batches = new AtomicInteger();
        AtomicInteger rows = new AtomicInteger();
        int total = ExcelImportSupport.readInBatches(
                new ByteArrayInputStream(out.toByteArray()),
                DemoImportRow.class,
                2,
                batch -> {
                    batches.incrementAndGet();
                    rows.addAndGet(batch.size());
                    assertTrue(batch.size() <= 2);
                });
        assertEquals(5, total);
        assertEquals(5, rows.get());
        assertEquals(3, batches.get());
    }

    private static DemoImportRow row(String name) {
        DemoImportRow r = new DemoImportRow();
        r.setName(name);
        return r;
    }

    public static class DemoImportRow {
        @ExcelProperty(index = 0)
        @ExcelI18nProperty("gen.demo.name.label")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
