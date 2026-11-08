package com.icoolkj.platform.framework.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnnotatedColumnWidthHandlerTest {

    @Test
    void appliesColumnWidthFromFieldAnnotations() throws Exception {
        List<Field> columns = new ArrayList<>();
        for (Field field : WidthRow.class.getDeclaredFields()) {
            if (field.getAnnotation(ExcelI18nProperty.class) != null) {
                field.setAccessible(true);
                columns.add(field);
            }
        }

        List<List<String>> head = List.of(List.of("A"), List.of("B"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out)
                .head(head)
                .registerWriteHandler(new AnnotatedColumnWidthHandler(columns))
                .sheet("t")
                .doWrite(List.of());

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals(16 * 256, sheet.getColumnWidth(0));
            assertEquals(24 * 256, sheet.getColumnWidth(1));
        }
    }

    static final class WidthRow {
        @ExcelI18nProperty("a")
        @ColumnWidth(16)
        String a;

        @ExcelI18nProperty("b")
        @ColumnWidth(24)
        String b;
    }
}
