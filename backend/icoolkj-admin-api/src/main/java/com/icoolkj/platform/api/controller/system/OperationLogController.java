package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.enums.OperationStatusEnum;
import com.icoolkj.platform.common.model.audit.AuditChainVerifyVO;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.framework.excel.ExcelExportSupport;
import com.icoolkj.platform.framework.excel.ExcelValueFormatters;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.infra.excel.ExcelExportAsyncSubmitFacade;
import com.icoolkj.platform.infra.vo.AsyncTaskSubmitVO;
import com.icoolkj.platform.system.operationlog.dto.OperationLogPageQuery;
import com.icoolkj.platform.system.operationlog.excel.OperationLogExcelExportAsyncHandler;
import com.icoolkj.platform.system.operationlog.excel.OperationLogExportRow;
import com.icoolkj.platform.system.operationlog.service.OperationLogService;
import com.icoolkj.platform.system.audit.service.impl.AuditChainVerifyService;
import com.icoolkj.platform.system.operationlog.vo.OperationLogVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 操作日志管理 */
@RestController
@RequestMapping("/system/operationLog")
public class OperationLogController {

    private final OperationLogService operationLogService;
    private final AuditChainVerifyService auditChainVerifyService;
    private final I18nMessageResolver i18nMessageResolver;
    private final ExcelExportAsyncSubmitFacade excelExportAsyncSubmitFacade;

    public OperationLogController(OperationLogService operationLogService,
                                  AuditChainVerifyService auditChainVerifyService,
                                  I18nMessageResolver i18nMessageResolver,
                                  ExcelExportAsyncSubmitFacade excelExportAsyncSubmitFacade) {
        this.operationLogService = operationLogService;
        this.auditChainVerifyService = auditChainVerifyService;
        this.i18nMessageResolver = i18nMessageResolver;
        this.excelExportAsyncSubmitFacade = excelExportAsyncSubmitFacade;
    }

    @PreAuthorize("@authz.hasPermission('monitor:operationLog:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<OperationLogVO>> page(OperationLogPageQuery query) {
        return ApiResponse.ok(operationLogService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('monitor:operationLog:export')")
    @OperationLog(module = "Operation Log", businessType = "EXPORT")
    @GetMapping("/export")
    public void export(OperationLogPageQuery query, HttpServletResponse response) {
        String statusSuccess = ExcelValueFormatters.pageText(i18nMessageResolver, "operationLog", "statusSuccess");
        String statusFail = ExcelValueFormatters.pageText(i18nMessageResolver, "operationLog", "statusFail");
        ExcelExportSupport.writeLocalizedPaged(
                response,
                "operationLog",
                OperationLogExportRow.class,
                query,
                operationLogService::page,
                vo -> {
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
                },
                i18nMessageResolver);
    }

    @PreAuthorize("@authz.hasPermission('monitor:operationLog:export')")
    @OperationLog(module = "Operation Log", businessType = "EXPORT")
    @Idempotent("monitor:operationLog:export-async")
    @PostMapping("/export-async")
    public ApiResponse<AsyncTaskSubmitVO> exportAsync(OperationLogPageQuery query) throws Exception {
        return ApiResponse.ok(excelExportAsyncSubmitFacade.submit(
                OperationLogExcelExportAsyncHandler.TASK_TYPE,
                ExcelValueFormatters.pageText(i18nMessageResolver, "operationLog", "exportAsyncTaskName"),
                query));
    }

    @PreAuthorize("@authz.hasPermission('monitor:operationLog:list')")
    @GetMapping("/integrity/verify")
    public ApiResponse<AuditChainVerifyVO> verifyIntegrity() {
        return ApiResponse.ok(auditChainVerifyService.verifyOperationChain());
    }

    @PreAuthorize("@authz.hasPermission('monitor:operationLog:list')")
    @GetMapping("/{id}")
    public ApiResponse<OperationLogVO> getById(@PathVariable String id) {
        return ApiResponse.ok(operationLogService.getById(id));
    }
}
