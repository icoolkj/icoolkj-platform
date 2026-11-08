package com.icoolkj.platform.system.user.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.framework.excel.ExcelImportResult;
import com.icoolkj.platform.infra.excel.AbstractExcelImportAsyncHandler;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.system.user.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Async Excel import for users ({@code taskType=excel.import.user}).
 * {@code request_param} JSON: {@code {"fileId":"..."}}.
 */
@Component
public class UserExcelImportAsyncHandler extends AbstractExcelImportAsyncHandler<UserImportRow> {

    public static final String TASK_TYPE = "excel.import.user";

    private final UserService userService;

    public UserExcelImportAsyncHandler(InfraFileService infraFileService,
                                       UserService userService,
                                       ObjectMapper objectMapper) {
        super(infraFileService, objectMapper);
        this.userService = userService;
    }

    @Override
    public String taskType() {
        return TASK_TYPE;
    }

    @Override
    protected Class<UserImportRow> importRowType() {
        return UserImportRow.class;
    }

    @Override
    protected ExcelImportResult importRows(List<UserImportRow> rows) {
        return userService.importRows(rows);
    }
}
