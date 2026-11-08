package com.icoolkj.platform.framework.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.icoolkj.platform.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExcelImportIndexValidationTest {

    @Test
    void rejectsDuplicateIndex() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> ExcelImportSupport.listImportFields(DupIndexRow.class));
        assertEquals("error.excel.import_column_index_duplicate", ex.getMessage());
    }

    @Test
    void rejectsIndexGap() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> ExcelImportSupport.listImportFields(GapIndexRow.class));
        assertEquals("error.excel.import_column_index_gap", ex.getMessage());
    }

    @Test
    void maskCellAppliesNameIpIdCardBankCard() throws Exception {
        Field name = MaskProbe.class.getDeclaredField("realName");
        Field ip = MaskProbe.class.getDeclaredField("ip");
        Field idCard = MaskProbe.class.getDeclaredField("idCard");
        Field bankCard = MaskProbe.class.getDeclaredField("bankCard");
        assertEquals("张**", ExcelExportSupport.maskCell(name, "张伟"));
        assertEquals("10.0.0.***", ExcelExportSupport.maskCell(ip, "10.0.0.8"));
        assertEquals("1101**********101X", ExcelExportSupport.maskCell(idCard, "11010119900101101X"));
        assertEquals("6222********1234", ExcelExportSupport.maskCell(bankCard, "6222021234561234"));
    }

    public static class DupIndexRow {
        @ExcelProperty(index = 0)
        @ExcelI18nProperty("a")
        private String a;
        @ExcelProperty(index = 0)
        @ExcelI18nProperty("b")
        private String b;
    }

    public static class GapIndexRow {
        @ExcelProperty(index = 0)
        @ExcelI18nProperty("a")
        private String a;
        @ExcelProperty(index = 2)
        @ExcelI18nProperty("b")
        private String b;
    }

    public static class MaskProbe {
        @ExcelI18nProperty("n")
        @ExcelMask(ExcelMask.Type.NAME)
        @ColumnWidth(10)
        private String realName;
        @ExcelI18nProperty("i")
        @ExcelMask(ExcelMask.Type.IP)
        @ColumnWidth(10)
        private String ip;
        @ExcelI18nProperty("id")
        @ExcelMask(ExcelMask.Type.ID_CARD)
        @ColumnWidth(18)
        private String idCard;
        @ExcelI18nProperty("bc")
        @ExcelMask(ExcelMask.Type.BANK_CARD)
        @ColumnWidth(18)
        private String bankCard;
    }
}
