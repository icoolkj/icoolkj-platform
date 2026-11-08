package com.icoolkj.platform.infra.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.excel.ExcelImportSupport;
import com.icoolkj.platform.infra.async.AsyncTaskRuntime;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.vo.AsyncTaskSubmitVO;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Pre-allocate {@code taskId} → upload with {@code bizId=taskId} (ref bound) → insert/publish task.
 * Task row and file refs share the same business anchor ({@code taskId}); {@code fileId} lives in request_param only.
 */
@Component
public class ExcelImportAsyncSubmitFacade {

    private static final Logger log = LoggerFactory.getLogger(ExcelImportAsyncSubmitFacade.class);

    private final InfraFileService infraFileService;
    private final AsyncTaskRuntime asyncTaskRuntime;
    private final ObjectMapper objectMapper;

    public ExcelImportAsyncSubmitFacade(InfraFileService infraFileService,
                                        AsyncTaskRuntime asyncTaskRuntime,
                                        ObjectMapper objectMapper) {
        this.infraFileService = infraFileService;
        this.asyncTaskRuntime = asyncTaskRuntime;
        this.objectMapper = objectMapper;
    }

    public AsyncTaskSubmitVO submit(MultipartFile file,
                                    String taskType,
                                    String taskName,
                                    String uploadRemark) throws Exception {
        ExcelImportSupport.assertXlsxUpload(file);
        String taskId = IdGeneratorUtils.nextIdStr();
        InfraFileVO uploaded = infraFileService.uploadExcelImportStaging(file, taskId, uploadRemark);
        String requestParam = objectMapper.writeValueAsString(Map.of("fileId", uploaded.getId()));
        try {
            asyncTaskRuntime.submitWithId(
                    taskId,
                    taskType,
                    taskName,
                    ExcelImportAsyncConstants.BIZ_TYPE,
                    taskId,
                    requestParam);
            return new AsyncTaskSubmitVO(taskId);
        } catch (Exception ex) {
            try {
                infraFileService.releaseBizFiles(
                        uploaded.getTenantId(),
                        ExcelImportAsyncConstants.BIZ_TYPE,
                        taskId);
            } catch (Exception cleanupEx) {
                log.warn("Excel import staging cleanup failed taskId={} fileId={}",
                        taskId, uploaded.getId(), cleanupEx);
            }
            throw ex;
        }
    }
}
