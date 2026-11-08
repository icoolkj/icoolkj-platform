package com.icoolkj.platform.framework.excel;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.Sheet;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Applies {@link ColumnWidth} from row fields when writing with a custom {@code .head(List)}.
 *
 * <p>EasyExcel only honors {@code @ColumnWidth} for {@code EasyExcel.write(..., Class)} —
 * the localized head path used by {@link ExcelExportSupport} / {@link ExcelImportSupport}
 * would otherwise fall back to Excel's default narrow column width.
 */
final class AnnotatedColumnWidthHandler implements SheetWriteHandler {

    private final int[] widths;

    AnnotatedColumnWidthHandler(List<Field> columns) {
        this.widths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            ColumnWidth ann = columns.get(i).getAnnotation(ColumnWidth.class);
            widths[i] = ann != null ? ann.value() : -1;
        }
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();
        for (int i = 0; i < widths.length; i++) {
            if (widths[i] > 0) {
                // POI unit = 1/256 of a character width; matches EasyExcel @ColumnWidth semantics
                sheet.setColumnWidth(i, widths[i] * 256);
            }
        }
    }
}
