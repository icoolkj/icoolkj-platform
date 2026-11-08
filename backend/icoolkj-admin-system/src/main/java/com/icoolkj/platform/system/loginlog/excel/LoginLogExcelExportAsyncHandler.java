package com.icoolkj.platform.system.loginlog.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.enums.LoginStatusEnum;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.excel.ExcelValueFormatters;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.infra.excel.AbstractExcelExportAsyncHandler;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.system.loginlog.dto.LoginLogPageQuery;
import com.icoolkj.platform.system.loginlog.service.LoginLogService;
import com.icoolkj.platform.system.loginlog.vo.LoginLogVO;
import org.springframework.stereotype.Component;

/** Async Excel export for login logs ({@code taskType=excel.export.loginLog}). */
@Component
public class LoginLogExcelExportAsyncHandler
        extends AbstractExcelExportAsyncHandler<LoginLogPageQuery, LoginLogVO, LoginLogExportRow> {

    public static final String TASK_TYPE = "excel.export.loginLog";

    private final LoginLogService loginLogService;
    private final I18nMessageResolver i18nMessageResolver;

    public LoginLogExcelExportAsyncHandler(InfraFileService infraFileService,
                                           ObjectMapper objectMapper,
                                           I18nMessageResolver i18nMessageResolver,
                                           LoginLogService loginLogService) {
        super(infraFileService, objectMapper, i18nMessageResolver);
        this.loginLogService = loginLogService;
        this.i18nMessageResolver = i18nMessageResolver;
    }

    @Override
    public String taskType() {
        return TASK_TYPE;
    }

    @Override
    protected Class<LoginLogPageQuery> queryType() {
        return LoginLogPageQuery.class;
    }

    @Override
    protected String businessName() {
        return "loginLog";
    }

    @Override
    protected Class<LoginLogExportRow> exportRowType() {
        return LoginLogExportRow.class;
    }

    @Override
    protected PageResult<LoginLogVO> loadPage(LoginLogPageQuery query) {
        return loginLogService.page(query);
    }

    @Override
    protected LoginLogExportRow toExportRow(LoginLogVO vo) {
        String success = ExcelValueFormatters.pageText(i18nMessageResolver, "loginLog", "loginStatusSuccess");
        String fail = ExcelValueFormatters.pageText(i18nMessageResolver, "loginLog", "loginStatusFail");
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
    }
}
