package com.icoolkj.platform.api.controller.infra;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.infra.backup.DatabaseBackupService;
import com.icoolkj.platform.infra.dto.DatabaseBackupPageQuery;
import com.icoolkj.platform.infra.vo.DatabaseBackupFileVO;
import com.icoolkj.platform.infra.vo.DatabaseBackupRunResultVO;
import com.icoolkj.platform.system.backup.dto.DatabaseBackupConfigUpdateRequest;
import com.icoolkj.platform.system.backup.service.DatabaseBackupConfigAdminService;
import com.icoolkj.platform.system.backup.vo.DatabaseBackupConfigAdminVO;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Database backup ops (manual trigger + file list). Scheduled runs stay in worker. */
@RestController
@RequestMapping("/infra/databaseBackup")
public class DatabaseBackupController {

    private final DatabaseBackupService databaseBackupService;
    private final DatabaseBackupConfigAdminService databaseBackupConfigAdminService;

    public DatabaseBackupController(DatabaseBackupService databaseBackupService,
                                    DatabaseBackupConfigAdminService databaseBackupConfigAdminService) {
        this.databaseBackupService = databaseBackupService;
        this.databaseBackupConfigAdminService = databaseBackupConfigAdminService;
    }

    @PreAuthorize("@authz.hasPermission('monitor:dbBackup:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<DatabaseBackupFileVO>> page(DatabaseBackupPageQuery query) {
        return ApiResponse.ok(databaseBackupService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('monitor:dbBackup:view')")
    @GetMapping("/config")
    public ApiResponse<DatabaseBackupConfigAdminVO> config() {
        return ApiResponse.ok(databaseBackupConfigAdminService.getForAdmin());
    }

    @PreAuthorize("@authz.hasPermission('monitor:dbBackup:edit')")
    @OperationLog(module = "DatabaseBackup", businessType = "UPDATE")
    @Idempotent("monitor:dbBackup:edit")
    @PutMapping("/config")
    public ApiResponse<Void> updateConfig(@Valid @RequestBody DatabaseBackupConfigUpdateRequest request) {
        databaseBackupConfigAdminService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('monitor:dbBackup:run')")
    @OperationLog(module = "DatabaseBackup", businessType = "OTHER")
    @Idempotent("monitor:dbBackup:run")
    @PostMapping("/run")
    public ApiResponse<DatabaseBackupRunResultVO> run() {
        DatabaseBackupRunResultVO result = databaseBackupService.runManualBackup();
        if (!result.isSuccess()) {
            throw new BusinessException("error.backup.run_failed", result.getMessage());
        }
        return ApiResponse.ok(result);
    }

    @PreAuthorize("@authz.hasPermission('monitor:dbBackup:list')")
    @GetMapping("/{fileName}/download")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        Resource resource = databaseBackupService.loadFileResource(fileName);
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PreAuthorize("@authz.hasPermission('monitor:dbBackup:delete')")
    @OperationLog(module = "DatabaseBackup", businessType = "DELETE")
    @Idempotent("monitor:dbBackup:delete")
    @DeleteMapping("/{fileName}")
    public ApiResponse<Void> delete(@PathVariable String fileName) {
        databaseBackupService.deleteFile(fileName);
        return ApiResponse.ok();
    }
}
