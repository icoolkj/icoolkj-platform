package com.icoolkj.platform.infra.excel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.excel.ExcelImportResult;
import com.icoolkj.platform.framework.excel.ExcelImportSupport;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.infra.async.AsyncTaskContext;
import com.icoolkj.platform.infra.async.AsyncTaskHandler;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.vo.InfraFileRefVO;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.List;

/**
 * Shared async Excel import: read {@code fileId} from request_param → assert staging anchor →
 * re-validate payload → parse rows → {@link #importRows}.
 */
public abstract class AbstractExcelImportAsyncHandler<T> implements AsyncTaskHandler {

    private final InfraFileService infraFileService;
    private final ObjectMapper objectMapper;

    protected AbstractExcelImportAsyncHandler(InfraFileService infraFileService, ObjectMapper objectMapper) {
        this.infraFileService = infraFileService;
        this.objectMapper = objectMapper;
    }

    protected abstract Class<T> importRowType();

    protected abstract ExcelImportResult importRows(List<T> rows);

    @Override
    public String handle(AsyncTaskContext context) throws Exception {
        String fileId = readFileId(context.requestParam());
        if (!StringUtils.hasText(fileId)) {
            throw new BusinessException("error.async.file_required");
        }
        assertStagingFileBelongsToTask(fileId.trim(), context);
        Resource resource = infraFileService.loadDownloadResource(fileId.trim());
        if (resource == null || !resource.exists()) {
            throw new BusinessException("error.async.file_required");
        }
        ExcelImportSupport.assertXlsxResource(resource);
        List<T> rows;
        try (InputStream in = resource.getInputStream()) {
            rows = ExcelImportSupport.readRows(in, importRowType());
        }
        return objectMapper.writeValueAsString(importRows(rows));
    }

    /**
     * Staging must be Facade-minted: file row {@code bizType/bizId} match task, and an
     * {@code excel-import}/{@code import} ref for the same taskId.
     */
    private void assertStagingFileBelongsToTask(String fileId, AsyncTaskContext context) {
        String taskId = context.taskId();
        if (!StringUtils.hasText(taskId)) {
            throw new BusinessException("error.async.file_required");
        }
        InfraFileVO file = infraFileService.getById(fileId);
        if (file == null) {
            throw new BusinessException("error.async.file_required");
        }
        String taskTenant = TenantContextHolder.get() == null
                ? null
                : TenantContextHolder.get().getTenantId();
        if (!StringUtils.hasText(taskTenant)
                || !StringUtils.hasText(file.getTenantId())
                || !taskTenant.equals(file.getTenantId())) {
            throw new BusinessException("error.async.file_required");
        }
        boolean anchoredOnFile =
                ExcelImportAsyncConstants.BIZ_TYPE.equals(file.getBizType())
                        && taskId.equals(file.getBizId());
        if (!anchoredOnFile) {
            throw new BusinessException("error.async.file_required");
        }
        List<InfraFileRefVO> refs = infraFileService.listRefs(fileId);
        if (CollectionUtils.isEmpty(refs)) {
            throw new BusinessException("error.async.file_required");
        }
        for (InfraFileRefVO ref : refs) {
            if (ExcelImportAsyncConstants.BIZ_TYPE.equals(ref.getBizType())
                    && taskId.equals(ref.getBizId())
                    && ExcelImportAsyncConstants.REF_ROLE.equals(ref.getRefField())) {
                return;
            }
        }
        throw new BusinessException("error.async.file_required");
    }

    private String readFileId(String requestParam) throws Exception {
        if (!StringUtils.hasText(requestParam)) {
            return null;
        }
        JsonNode node = objectMapper.readTree(requestParam);
        JsonNode fileId = node.get("fileId");
        return fileId == null || fileId.isNull() ? null : fileId.asText();
    }
}
