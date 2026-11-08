package com.icoolkj.platform.system.operationlog.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.enums.OperationStatusEnum;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.excel.ExcelValueFormatters;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.infra.excel.AbstractExcelExportAsyncHandler;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.system.operationlog.dto.OperationLogPageQuery;
import com.icoolkj.platform.system.operationlog.service.OperationLogService;
import com.icoolkj.platform.system.operationlog.vo.OperationLogVO;
import org.springframework.stereotype.Component;

/** Async Excel export for operation logs ({@code taskType=excel.export.operationLog}). */
@Component
public class OperationLogExcelExportAsyncHandler
        extends AbstractExcelExportAsyncHandler<OperationLogPageQuery, OperationLogVO, OperationLogExportRow> {

    public static final String TASK_TYPE = "excel.export.operationLog";

    private final OperationLogService operationLogService;
    private final I18nMessageResolver i18nMessageResolver;

    public OperationLogExcelExportAsyncHandler(InfraFileService infraFileService,
                                               ObjectMapper objectMapper,
                                               I18nMessageResolver i18nMessageResolver,
                                               OperationLogService operationLogService) {
        super(infraFileService, objectMapper, i18nMessageResolver);
        this.operationLogService = operationLogService;
        this.i18nMessageResolver = i18nMessageResolver;
    }

    @Override
    public String taskType() {
        return TASK_TYPE;
    }

    @Override
    protected Class<OperationLogPageQuery> queryType() {
        return OperationLogPageQuery.class;
    }

    @Override
    protected String businessName() {
        return "operationLog";
    }

    @Override
    protected Class<OperationLogExportRow> exportRowType() {
        return OperationLogExportRow.class;
    }

    @Override
    protected PageResult<OperationLogVO> loadPage(OperationLogPageQuery query) {
        return operationLogService.page(query);
    }

    @Override
    protected OperationLogExportRow toExportRow(OperationLogVO vo) {
        String statusSuccess = ExcelValueFormatters.pageText(i18nMessageResolver, "operationLog", "statusSuccess");
        String statusFail = ExcelValueFormatters.pageText(i18nMessageResolver, "operationLog", "statusFail");
        OperationLogExportRow row = new OperationLogExportRow();
        row.setModuleName(ExcelValueFormatters.audit(
                i18nMessageResolver, "operationLog", "modules", vo.getModuleName()));
        row.setBusinessType(ExcelValueFormatters.audit(
                i18nMessageResolver, "operationLog", "businessTypes", vo.getBusinessType()));
        row.setOperatorName(vo.getOperatorName());
        row.setRequestMethod(vo.getRequestMethod());
        row.setOperationUrl(vo.getOperationUrl());
        row.setOperationIp(vo.getOperationIp());
        row.setOperationStatus(ExcelValueFormatters.binaryStatus(
                vo.getOperationStatus(),
                OperationStatusEnum.SUCCESS.getCode(),
                OperationStatusEnum.FAIL.getCode(),
                statusSuccess,
                statusFail));
        row.setCostTime(vo.getCostTime());
        row.setOperationAt(ExcelValueFormatters.dateTime(vo.getOperationAt()));
        return row;
    }
}
