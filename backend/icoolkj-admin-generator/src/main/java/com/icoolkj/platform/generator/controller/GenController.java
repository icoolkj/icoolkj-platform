package com.icoolkj.platform.generator.controller;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.generator.constant.GenPermissionConstants;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.dto.GenTablePageQuery;
import com.icoolkj.platform.generator.service.GenTableService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Code generator ? /tool/gen (multi-module output for iCoolKJ Platform).
 */
@RestController
@ConditionalOnProperty(prefix = "icoolkj.generator", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping("/tool/gen")
public class GenController {

    private final GenTableService genTableService;

    public GenController(GenTableService genTableService) {
        this.genTableService = genTableService;
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.LIST + "')")
    @GetMapping("/page")
    public ApiResponse<PageResult<GenTable>> page(GenTablePageQuery query) {
        return ApiResponse.ok(genTableService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.LIST + "')")
    @GetMapping("/list")
    public ApiResponse<List<GenTable>> list(GenTable query) {
        return ApiResponse.ok(genTableService.selectGenTableList(query));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.IMPORT + "')")
    @GetMapping("/db/page")
    public ApiResponse<PageResult<GenTable>> dbPage(GenTablePageQuery query) {
        return ApiResponse.ok(genTableService.pageDbTable(query));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.IMPORT + "')")
    @GetMapping("/db/list")
    public ApiResponse<List<GenTable>> dbList(GenTable query) {
        return ApiResponse.ok(genTableService.selectDbTableList(query));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.QUERY + "')")
    @GetMapping("/{id}")
    public ApiResponse<GenTable> getInfo(@PathVariable String id) {
        return ApiResponse.ok(genTableService.selectGenTableById(id));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.IMPORT + "')")
    @OperationLog(module = "Code Generator", businessType = "IMPORT")
    @Idempotent("tool:gen:import")
    @PostMapping("/import")
    public ApiResponse<Void> importTable(@RequestParam String tables) {
        genTableService.importTables(tables.split(","), "system");
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.EDIT + "')")
    @OperationLog(module = "Code Generator", businessType = "UPDATE")
    @Idempotent("tool:gen:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@RequestBody GenTable genTable) {
        genTableService.updateGenTable(genTable);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.REMOVE + "')")
    @OperationLog(module = "Code Generator", businessType = "DELETE")
    @Idempotent("tool:gen:delete")
    @DeleteMapping("/{ids}")
    public ApiResponse<Void> remove(@PathVariable String[] ids) {
        genTableService.deleteGenTableByIds(ids);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.PREVIEW + "')")
    @GetMapping("/preview/{id}")
    public ApiResponse<Map<String, String>> preview(@PathVariable String id) {
        return ApiResponse.ok(genTableService.previewCode(id));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.CODE + "')")
    @GetMapping("/download/{tableName}")
    public void download(@PathVariable String tableName, HttpServletResponse response) throws IOException {
        writeZip(response, tableName + "_code.zip", genTableService.downloadCode(tableName));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.CODE + "')")
    @GetMapping("/batchGenCode")
    public void batchGenCode(@RequestParam String tables, HttpServletResponse response) throws IOException {
        writeZip(response, "icoolkj_code.zip", genTableService.downloadCodeBatch(tables.split(",")));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.CODE + "')")
    @OperationLog(module = "Code Generator", businessType = "GENERATE_CODE")
    @Idempotent("tool:gen:generateCode")
    @PostMapping("/genCode/{tableName}")
    public ApiResponse<Void> genCode(@PathVariable String tableName) {
        genTableService.generateCode(tableName);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.EDIT + "')")
    @OperationLog(module = "Code Generator", businessType = "SYNC_DB")
    @Idempotent("tool:gen:syncDb")
    @PostMapping("/synchDb/{tableName}")
    public ApiResponse<Void> synchDb(@PathVariable String tableName) {
        genTableService.syncDb(tableName);
        return ApiResponse.ok();
    }

    private void writeZip(HttpServletResponse response, String fileName, byte[] data) throws IOException {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + encoded);
        response.getOutputStream().write(data);
    }
}
