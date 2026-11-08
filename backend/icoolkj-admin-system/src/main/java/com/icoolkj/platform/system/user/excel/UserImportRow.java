package com.icoolkj.platform.system.user.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.icoolkj.platform.framework.excel.ExcelI18nProperty;
import com.icoolkj.platform.framework.excel.ExcelImportDict;
import com.icoolkj.platform.framework.excel.ExcelImportRef;
import lombok.Data;

/**
 * User Excel import allowlist (Gen reference sample).
 * <p>Bind by {@link ExcelProperty#index()}. Humans pick dict <strong>labels</strong> and
 * department <strong>names</strong> (template dropdowns); Service resolves to codes/ids.
 */
@Data
public class UserImportRow {

    @ExcelProperty(index = 0)
    @ExcelI18nProperty("gen.user.username.label")
    @ColumnWidth(16)
    private String username;

    @ExcelProperty(index = 1)
    @ExcelI18nProperty("gen.user.nickname.label")
    @ColumnWidth(16)
    private String nickname;

    @ExcelProperty(index = 2)
    @ExcelI18nProperty("gen.user.realName.label")
    @ColumnWidth(14)
    private String realName;

    @ExcelProperty(index = 3)
    @ExcelI18nProperty("gen.user.deptName.label")
    @ExcelImportRef(ExcelImportRef.Kind.DEPT_NAME)
    @ColumnWidth(22)
    private String deptName;

    @ExcelProperty(index = 4)
    @ExcelI18nProperty("gen.user.phone.label")
    @ColumnWidth(14)
    private String phone;

    @ExcelProperty(index = 5)
    @ExcelI18nProperty("gen.user.email.label")
    @ColumnWidth(22)
    private String email;

    @ExcelProperty(index = 6)
    @ExcelI18nProperty("gen.user.gender.label")
    @ExcelImportDict("sys_gender")
    @ColumnWidth(10)
    private String gender;

    @ExcelProperty(index = 7)
    @ExcelI18nProperty("gen.user.status.label")
    @ExcelImportDict("sys_common_status")
    @ColumnWidth(10)
    private String status;
}
