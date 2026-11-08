package com.icoolkj.platform.generator.controller;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.generator.constant.GenPermissionConstants;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenViewConfig;
import com.icoolkj.platform.generator.dto.GenBaselineJsonResponse;
import com.icoolkj.platform.generator.dto.GenViewConfigSaveRequest;
import com.icoolkj.platform.generator.service.GenTableService;
import com.icoolkj.platform.generator.service.GenViewConfigService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Low-code CRUD UI layout (view.json) — admin + runtime load.
 */
@RestController
@ConditionalOnProperty(prefix = "icoolkj.generator", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping
public class GenViewController {

    private final GenViewConfigService genViewConfigService;
    private final GenTableService genTableService;

    public GenViewController(GenViewConfigService genViewConfigService, GenTableService genTableService) {
        this.genViewConfigService = genViewConfigService;
        this.genTableService = genTableService;
    }

    /** Runtime: deprecated — CRUD pages load Git view.json from the frontend bundle (P0 Git-only). */
    @Deprecated
    @GetMapping("/gen/view/by-business/{businessName}")
    public ApiResponse<GenViewConfig> runtimeByBusiness(@PathVariable String businessName,
                                                        @RequestParam String apiModule) {
        return ApiResponse.ok(genViewConfigService.getRuntimeByBusiness(businessName, apiModule));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.QUERY + "')")
    @GetMapping("/tool/gen/view/{tableId}")
    public ApiResponse<GenViewConfig> getByTableId(@PathVariable String tableId) {
        return ApiResponse.ok(genViewConfigService.getByTableId(tableId));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.EDIT + "')")
    @OperationLog(module = "Code Generator View", businessType = "SAVE")
    @Idempotent("tool:gen:view:save")
    @PutMapping("/tool/gen/view")
    public ApiResponse<Void> save(@RequestBody GenViewConfigSaveRequest request) {
        GenViewConfig row = new GenViewConfig();
        row.setId(request.getId());
        row.setTableId(request.getTableId());
        row.setViewJson(request.getViewJson());
        row.setRemark(request.getRemark());
        genViewConfigService.save(row);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.EDIT + "')")
    @OperationLog(module = "Code Generator View", businessType = "SYNC")
    @Idempotent("tool:gen:view:sync")
    @PostMapping("/tool/gen/view/sync/{tableId}")
    public ApiResponse<Void> sync(@PathVariable String tableId) {
        GenTable table = genTableService.selectGenTableById(tableId);
        genViewConfigService.syncFromTable(table);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.QUERY + "')")
    @GetMapping("/tool/gen/view/baseline/{tableId}")
    public ApiResponse<GenBaselineJsonResponse> viewBaseline(@PathVariable String tableId) {
        GenTable table = genTableService.selectGenTableById(tableId);
        String json = genViewConfigService.getGitBaselineJson(table.getBusinessName());
        return ApiResponse.ok(new GenBaselineJsonResponse(json));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.EDIT + "')")
    @OperationLog(module = "Code Generator View", businessType = "RESTORE_BASELINE")
    @Idempotent("tool:gen:view:restoreBaseline")
    @PostMapping("/tool/gen/view/restore-baseline/{tableId}")
    public ApiResponse<Void> restoreViewBaseline(@PathVariable String tableId) {
        GenTable table = genTableService.selectGenTableById(tableId);
        genViewConfigService.restoreGitBaseline(tableId, table.getBusinessName());
        return ApiResponse.ok();
    }
}
