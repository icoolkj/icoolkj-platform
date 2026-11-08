package com.icoolkj.platform.framework.excel;

import com.icoolkj.platform.common.enums.StatusEnum;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExcelValueFormattersTest {

    @Test
    void dateTimeFormatsOrNull() {
        assertNull(ExcelValueFormatters.dateTime(null));
        assertEquals(
                "2026-07-23 09:30:00",
                ExcelValueFormatters.dateTime(LocalDateTime.of(2026, 7, 23, 9, 30, 0)));
    }

    @Test
    void commonStatusMapsCodes() {
        assertEquals("正常", ExcelValueFormatters.commonStatus(StatusEnum.NORMAL.getCode(), "正常", "停用"));
        assertEquals("停用", ExcelValueFormatters.commonStatus(StatusEnum.DISABLE.getCode(), "正常", "停用"));
        assertEquals("9", ExcelValueFormatters.commonStatus("9", "正常", "停用"));
    }
}
