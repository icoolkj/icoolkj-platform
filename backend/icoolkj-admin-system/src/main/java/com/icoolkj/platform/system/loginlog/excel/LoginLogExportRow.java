package com.icoolkj.platform.system.loginlog.excel;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.icoolkj.platform.framework.excel.ExcelI18nProperty;
import com.icoolkj.platform.framework.excel.ExcelMask;
import lombok.Data;

@Data
public class LoginLogExportRow {

    @ExcelI18nProperty("gen.loginLog.username.label")
    @ColumnWidth(16)
    private String username;

    @ExcelI18nProperty("gen.loginLog.loginType.label")
    @ColumnWidth(12)
    private String loginType;

    @ExcelI18nProperty("gen.loginLog.loginStatus.label")
    @ColumnWidth(8)
    private String loginStatus;

    @ExcelI18nProperty("gen.loginLog.message.label")
    @ColumnWidth(28)
    private String message;

    @ExcelI18nProperty("gen.loginLog.ip.label")
    @ExcelMask(ExcelMask.Type.IP)
    @ColumnWidth(16)
    private String ip;

    @ExcelI18nProperty("gen.loginLog.browser.label")
    @ColumnWidth(16)
    private String browser;

    @ExcelI18nProperty("gen.loginLog.os.label")
    @ColumnWidth(14)
    private String os;

    @ExcelI18nProperty("gen.loginLog.loginAt.label")
    @ColumnWidth(20)
    private String loginAt;
}
