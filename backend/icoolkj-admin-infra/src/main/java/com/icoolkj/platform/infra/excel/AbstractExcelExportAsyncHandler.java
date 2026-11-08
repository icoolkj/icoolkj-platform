package com.icoolkj.platform.infra.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageQuery;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.excel.ExcelExportSupport;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.infra.async.AsyncTaskContext;
import com.icoolkj.platform.infra.async.AsyncTaskHandler;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.vo.ExcelExportAsyncResultVO;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import org.springframework.util.StringUtils;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Shared async Excel export: deserialize query → write paged xlsx → stage under taskId →
 * return {@link ExcelExportAsyncResultVO} JSON.
 */
public abstract class AbstractExcelExportAsyncHandler<Q extends PageQuery, VO, ROW>
        implements AsyncTaskHandler {

    private final InfraFileService infraFileService;
    private final ObjectMapper objectMapper;
    private final I18nMessageResolver i18nMessageResolver;

    protected AbstractExcelExportAsyncHandler(InfraFileService infraFileService,
                                              ObjectMapper objectMapper,
                                              I18nMessageResolver i18nMessageResolver) {
        this.infraFileService = infraFileService;
        this.objectMapper = objectMapper;
        this.i18nMessageResolver = i18nMessageResolver;
    }

    protected abstract Class<Q> queryType();

    protected abstract String businessName();

    protected abstract Class<ROW> exportRowType();

    protected abstract PageResult<VO> loadPage(Q query);

    protected abstract ROW toExportRow(VO vo);

    @Override
    public String handle(AsyncTaskContext context) throws Exception {
        if (!StringUtils.hasText(context.requestParam())) {
            throw new BusinessException("error.excel.export_failed");
        }
        Q query = objectMapper.readValue(context.requestParam(), queryType());
        Path temp = Files.createTempFile("excel-export-", ".xlsx");
        try {
            ExcelExportSupport.ExcelExportFileMeta meta;
            try (OutputStream out = Files.newOutputStream(temp)) {
                meta = ExcelExportSupport.writeLocalizedPagedTo(
                        out,
                        businessName(),
                        exportRowType(),
                        query,
                        this::loadPage,
                        this::toExportRow,
                        i18nMessageResolver);
            }
            InfraFileVO uploaded = infraFileService.storeExcelExportStaging(
                    temp,
                    context.taskId(),
                    meta.displayName(),
                    businessName() + " excel export");
            return objectMapper.writeValueAsString(
                    new ExcelExportAsyncResultVO(uploaded.getId(), meta.displayName()));
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
