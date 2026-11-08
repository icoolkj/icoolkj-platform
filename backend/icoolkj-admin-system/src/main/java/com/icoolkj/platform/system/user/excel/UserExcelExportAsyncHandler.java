package com.icoolkj.platform.system.user.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.excel.ExcelValueFormatters;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.infra.excel.AbstractExcelExportAsyncHandler;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.system.user.dto.UserPageQuery;
import com.icoolkj.platform.system.user.service.UserService;
import com.icoolkj.platform.system.user.vo.UserVO;
import org.springframework.stereotype.Component;

/** Async Excel export for users ({@code taskType=excel.export.user}). */
@Component
public class UserExcelExportAsyncHandler
        extends AbstractExcelExportAsyncHandler<UserPageQuery, UserVO, UserExportRow> {

    public static final String TASK_TYPE = "excel.export.user";

    private final UserService userService;
    private final I18nMessageResolver i18nMessageResolver;

    public UserExcelExportAsyncHandler(InfraFileService infraFileService,
                                       ObjectMapper objectMapper,
                                       I18nMessageResolver i18nMessageResolver,
                                       UserService userService) {
        super(infraFileService, objectMapper, i18nMessageResolver);
        this.userService = userService;
        this.i18nMessageResolver = i18nMessageResolver;
    }

    @Override
    public String taskType() {
        return TASK_TYPE;
    }

    @Override
    protected Class<UserPageQuery> queryType() {
        return UserPageQuery.class;
    }

    @Override
    protected String businessName() {
        return "user";
    }

    @Override
    protected Class<UserExportRow> exportRowType() {
        return UserExportRow.class;
    }

    @Override
    protected PageResult<UserVO> loadPage(UserPageQuery query) {
        return userService.page(query);
    }

    @Override
    protected UserExportRow toExportRow(UserVO vo) {
        String statusNormal = ExcelValueFormatters.pageText(i18nMessageResolver, "user", "statusNormal");
        String statusDisabled = ExcelValueFormatters.pageText(i18nMessageResolver, "user", "statusDisabled");
        UserExportRow row = new UserExportRow();
        row.setUsername(vo.getUsername());
        row.setNickname(vo.getNickname());
        row.setRealName(vo.getRealName());
        row.setDeptName(vo.getDeptName());
        row.setPhone(vo.getPhone());
        row.setEmail(vo.getEmail());
        row.setStatus(ExcelValueFormatters.commonStatus(vo.getStatus(), statusNormal, statusDisabled));
        row.setCreatedAt(ExcelValueFormatters.dateTime(vo.getCreatedAt()));
        return row;
    }
}
