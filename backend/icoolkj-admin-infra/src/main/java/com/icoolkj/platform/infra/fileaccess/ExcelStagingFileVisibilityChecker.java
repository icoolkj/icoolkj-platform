package com.icoolkj.platform.infra.fileaccess;

import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.excel.ExcelExportAsyncConstants;
import com.icoolkj.platform.infra.excel.ExcelImportAsyncConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Excel staging ({@code excel-import}/{@code excel-export}) is submitter-owned:
 * only the uploader / task owner may download.
 */
@Component
public class ExcelStagingFileVisibilityChecker implements FileAccessVisibilityChecker {

    @Override
    public boolean supports(InfraFileEntity file, FileAccessCaller caller) {
        if (file == null || !StringUtils.hasText(file.getBizType())) {
            return false;
        }
        return ExcelImportAsyncConstants.isReservedBizType(file.getBizType().trim());
    }

    @Override
    public boolean isVisible(InfraFileEntity file, FileAccessCaller caller) {
        if (file == null || caller == null || !caller.authenticated()) {
            return false;
        }
        if (!StringUtils.hasText(caller.userId()) || !StringUtils.hasText(file.getUserId())) {
            return false;
        }
        return caller.userId().trim().equals(file.getUserId().trim());
    }
}
