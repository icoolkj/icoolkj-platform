package com.icoolkj.platform.system.user.excel;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.icoolkj.platform.framework.excel.ExcelI18nProperty;
import com.icoolkj.platform.framework.excel.ExcelMask;
import lombok.Data;

@Data
public class UserExportRow {

    @ExcelI18nProperty("gen.user.username.label")
    @ColumnWidth(16)
    private String username;

    @ExcelI18nProperty("gen.user.nickname.label")
    @ExcelMask(ExcelMask.Type.NAME)
    @ColumnWidth(16)
    private String nickname;

    @ExcelI18nProperty("gen.user.realName.label")
    @ExcelMask(ExcelMask.Type.NAME)
    @ColumnWidth(14)
    private String realName;

    @ExcelI18nProperty("gen.user.deptName.label")
    @ColumnWidth(18)
    private String deptName;

    @ExcelI18nProperty("gen.user.phone.label")
    @ExcelMask(ExcelMask.Type.PHONE)
    @ColumnWidth(14)
    private String phone;

    @ExcelI18nProperty("gen.user.email.label")
    @ExcelMask(ExcelMask.Type.EMAIL)
    @ColumnWidth(22)
    private String email;

    @ExcelI18nProperty("gen.user.status.label")
    @ColumnWidth(8)
    private String status;

    @ExcelI18nProperty("gen.user.createdAt.label")
    @ColumnWidth(20)
    private String createdAt;
}
