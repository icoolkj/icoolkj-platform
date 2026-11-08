package com.icoolkj.platform.framework.excel;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies Excel data-validation dropdowns for import template columns.
 * Short lists use explicit constraints; longer lists use a hidden {@code _refs} sheet.
 */
final class ExcelImportDropdownHandler implements SheetWriteHandler {

    private static final int EXPLICIT_MAX_ITEMS = 20;
    private static final int EXPLICIT_MAX_CHARS = 200;
    private static final int DROPDOWN_ROWS = 5_000;
    private static final String REFS_SHEET = "_refs";

    private final Map<Integer, List<String>> optionsByColumn;

    ExcelImportDropdownHandler(Map<Integer, List<String>> optionsByColumn) {
        this.optionsByColumn = optionsByColumn == null ? Map.of() : Map.copyOf(optionsByColumn);
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        if (optionsByColumn.isEmpty()) {
            return;
        }
        Sheet sheet = writeSheetHolder.getSheet();
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        DataValidationHelper helper = sheet.getDataValidationHelper();
        int refCol = 0;
        for (Map.Entry<Integer, List<String>> entry : optionsByColumn.entrySet()) {
            List<String> options = sanitize(entry.getValue());
            if (options.isEmpty()) {
                continue;
            }
            int col = entry.getKey();
            DataValidationConstraint constraint;
            if (useExplicit(options)) {
                constraint = helper.createExplicitListConstraint(options.toArray(String[]::new));
            } else {
                constraint = helper.createFormulaListConstraint(writeRefColumn(workbook, refCol++, options));
            }
            CellRangeAddressList range = new CellRangeAddressList(1, DROPDOWN_ROWS, col, col);
            DataValidation validation = helper.createValidation(constraint, range);
            validation.setShowErrorBox(true);
            validation.setSuppressDropDownArrow(true);
            validation.setShowPromptBox(true);
            sheet.addValidationData(validation);
        }
    }

    private static boolean useExplicit(List<String> options) {
        if (options.size() > EXPLICIT_MAX_ITEMS) {
            return false;
        }
        int chars = 0;
        for (String o : options) {
            chars += o.length() + 1;
        }
        return chars <= EXPLICIT_MAX_CHARS;
    }

    private static String writeRefColumn(Workbook workbook, int colIndex, List<String> options) {
        Sheet refs = workbook.getSheet(REFS_SHEET);
        if (refs == null) {
            refs = workbook.createSheet(REFS_SHEET);
            workbook.setSheetHidden(workbook.getSheetIndex(refs), true);
        }
        for (int i = 0; i < options.size(); i++) {
            Row row = refs.getRow(i);
            if (row == null) {
                row = refs.createRow(i);
            }
            row.createCell(colIndex).setCellValue(options.get(i));
        }
        String colLetter = columnLetter(colIndex);
        String range = "'" + REFS_SHEET + "'!$" + colLetter + "$1:$" + colLetter + "$" + options.size();
        String name = "excel_ref_" + colIndex;
        Name named = workbook.getName(name);
        if (named == null) {
            named = workbook.createName();
            named.setNameName(name);
        }
        named.setRefersToFormula(range);
        return name;
    }

    private static String columnLetter(int index) {
        int n = index + 1;
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            int rem = (n - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }

    private static List<String> sanitize(List<String> raw) {
        if (CollectionUtils.isEmpty(raw)) {
            return List.of();
        }
        LinkedHashMap<String, Boolean> uniq = new LinkedHashMap<>();
        for (String item : raw) {
            if (!StringUtils.hasText(item)) {
                continue;
            }
            String s = item.trim().replace(",", "，");
            uniq.putIfAbsent(s, Boolean.TRUE);
        }
        return new ArrayList<>(uniq.keySet());
    }
}
