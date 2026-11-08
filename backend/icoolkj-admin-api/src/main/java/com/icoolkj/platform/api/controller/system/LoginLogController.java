package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.enums.LoginStatusEnum;
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
import com.icoolkj.platform.system.loginlog.dto.LoginLogPageQuery;
import com.icoolkj.platform.system.loginlog.excel.LoginLogExcelExportAsyncHandler;
import com.icoolkj.platform.system.loginlog.excel.LoginLogExportRow;
import com.icoolkj.platform.system.loginlog.service.LoginLogService;
import com.icoolkj.platform.system.audit.service.impl.AuditChainVerifyService;
import com.icoolkj.platform.system.loginlog.vo.LoginLogVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 登录日志管理 */
@RestController
@RequestMapping("/system/loginLog")
public class LoginLogController {

    private final LoginLogService loginLogService;
    private final AuditChainVerifyService auditChainVerifyService;
    private final I18nMessageResolver i18nMessageResolver;
    private final ExcelExportAsyncSubmitFacade excelExportAsyncSubmitFacade;

    public LoginLogController(LoginLogService loginLogService,
                              AuditChainVerifyService auditChainVerifyService,
                              I18nMessageResolver i18nMessageResolver,
                              ExcelExportAsyncSubmitFacade excelExportAsyncSubmitFacade) {
        this.loginLogService = loginLogService;
        this.auditChainVerifyService = auditChainVerifyService;
        this.i18nMessageResolver = i18nMessageResolver;
        this.excelExportAsyncSubmitFacade = excelExportAsyncSubmitFacade;
    }

    @PreAuthorize("@authz.hasPermission('monitor:loginLog:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<LoginLogVO>> page(LoginLogPageQuery query) {
        return ApiResponse.ok(loginLogService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('monitor:loginLog:export')")
    @OperationLog(module = "Login Log", businessType = "EXPORT")
    @GetMapping("/export")
    public void export(LoginLogPageQuery query, HttpServletResponse response) {
        String success = ExcelValueFormatters.pageText(i18nMessageResolver, "loginLog", "loginStatusSuccess");
        String fail = ExcelValueFormatters.pageText(i18nMessageResolver, "loginLog", "loginStatusFail");
        ExcelExportSupport.writeLocalizedPaged(
                response,
                "loginLog",
                LoginLogExportRow.class,
                query,
                loginLogService::page,
                vo -> {
                    LoginLogExportRow row = new LoginLogExportRow();
                    row.setUsername(vo.getUsername());
                    row.setLoginType(vo.getLoginType());
                    row.setLoginStatus(ExcelValueFormatters.binaryStatus(
                            vo.getLoginStatus(),
                            LoginStatusEnum.SUCCESS.getCode(),
                            LoginStatusEnum.FAIL.getCode(),
                            success,
                            fail));
                    row.setMessage(vo.getMessage());
                    row.setIp(vo.getIp());
                    row.setBrowser(vo.getBrowser());
                    row.setOs(vo.getOs());
                    row.setLoginAt(ExcelValueFormatters.dateTime(vo.getLoginAt()));
                    return row;
                },
                i18nMessageResolver);
    }

    @PreAuthorize("@authz.hasPermission('monitor:loginLog:export')")
    @OperationLog(module = "Login Log", businessType = "EXPORT")
    @Idempotent("monitor:loginLog:export-async")
    @PostMapping("/export-async")
    public ApiResponse<AsyncTaskSubmitVO> exportAsync(LoginLogPageQuery query) throws Exception {
        return ApiResponse.ok(excelExportAsyncSubmitFacade.submit(
                LoginLogExcelExportAsyncHandler.TASK_TYPE,
                ExcelValueFormatters.pageText(i18nMessageResolver, "loginLog", "exportAsyncTaskName"),
                query));
    }

    @PreAuthorize("@authz.hasPermission('monitor:loginLog:list')")
    @GetMapping("/integrity/verify")
    public ApiResponse<AuditChainVerifyVO> verifyIntegrity() {
        return ApiResponse.ok(auditChainVerifyService.verifyLoginChain());
    }

    @PreAuthorize("@authz.hasPermission('monitor:loginLog:list')")
    @GetMapping("/{id}")
    public ApiResponse<LoginLogVO> getById(@PathVariable String id) {
        return ApiResponse.ok(loginLogService.getById(id));
    }
}
