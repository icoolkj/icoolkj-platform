package com.icoolkj.platform.system.operationlog.excel;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.icoolkj.platform.framework.excel.ExcelI18nProperty;
import com.icoolkj.platform.framework.excel.ExcelMask;
import lombok.Data;

@Data
public class OperationLogExportRow {

    @ExcelI18nProperty("gen.operationLog.moduleName.label")
    @ColumnWidth(16)
    private String moduleName;

    @ExcelI18nProperty("gen.operationLog.businessType.label")
    @ColumnWidth(14)
    private String businessType;

    @ExcelI18nProperty("gen.operationLog.operatorName.label")
    @ColumnWidth(14)
    private String operatorName;

    @ExcelI18nProperty("gen.operationLog.requestMethod.label")
    @ColumnWidth(10)
    private String requestMethod;

    @ExcelI18nProperty("gen.operationLog.operationUrl.label")
    @ColumnWidth(28)
    private String operationUrl;

    @ExcelI18nProperty("gen.operationLog.operationIp.label")
    @ExcelMask(ExcelMask.Type.IP)
    @ColumnWidth(16)
    private String operationIp;

    @ExcelI18nProperty("gen.operationLog.operationStatus.label")
    @ColumnWidth(8)
    private String operationStatus;

    @ExcelI18nProperty("gen.operationLog.costTime.label")
    @ColumnWidth(10)
    private Integer costTime;

    @ExcelI18nProperty("gen.operationLog.operationAt.label")
    @ColumnWidth(20)
    private String operationAt;
}
