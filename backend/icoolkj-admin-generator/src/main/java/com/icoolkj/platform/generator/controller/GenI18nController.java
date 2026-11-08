package com.icoolkj.platform.generator.controller;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.generator.constant.GenPermissionConstants;
import com.icoolkj.platform.generator.domain.GenI18nMessage;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.dto.GenBaselineJsonResponse;
import com.icoolkj.platform.generator.dto.GenI18nMessageSaveRequest;
import com.icoolkj.platform.generator.service.GenI18nMessageService;
import com.icoolkj.platform.generator.service.GenTableService;
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

import java.util.List;

/**
 * Low-code CRUD UI i18n — admin + runtime load.
 */
@RestController
@ConditionalOnProperty(prefix = "icoolkj.generator", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping
public class GenI18nController {

    private final GenI18nMessageService genI18nMessageService;
    private final GenTableService genTableService;

    public GenI18nController(GenI18nMessageService genI18nMessageService, GenTableService genTableService) {
        this.genI18nMessageService = genI18nMessageService;
        this.genTableService = genTableService;
    }

    /** Runtime: deprecated — CRUD pages load Git JSON from the frontend bundle (P0 Git-only). */
    @Deprecated
    @GetMapping("/gen/i18n/messages/{tableId}")
    public ApiResponse<GenI18nMessage> runtimeMessages(@PathVariable String tableId,
                                                       @RequestParam(required = false) String locale) {
        String resolvedLocale = locale != null ? locale : "zh-CN";
        GenI18nMessage row = genI18nMessageService.getRuntimeMessages(tableId, resolvedLocale);
        return ApiResponse.ok(row);
    }

    /** Runtime: deprecated — CRUD pages load Git JSON from the frontend bundle (P0 Git-only). */
    @Deprecated
    @GetMapping("/gen/i18n/messages/by-business/{businessName}")
    public ApiResponse<GenI18nMessage> runtimeMessagesByBusiness(@PathVariable String businessName,
                                                                 @RequestParam String apiModule,
                                                                 @RequestParam(required = false) String locale) {
        String resolvedLocale = locale != null ? locale : "zh-CN";
        GenI18nMessage row = genI18nMessageService.getRuntimeMessagesByBusiness(
                businessName, apiModule, resolvedLocale);
        return ApiResponse.ok(row);
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.QUERY + "')")
    @GetMapping("/tool/gen/i18n/list/{tableId}")
    public ApiResponse<List<GenI18nMessage>> list(@PathVariable String tableId) {
        return ApiResponse.ok(genI18nMessageService.listByTableId(tableId));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.QUERY + "')")
    @GetMapping("/tool/gen/i18n/messages")
    public ApiResponse<GenI18nMessage> getByLocale(@RequestParam String tableId,
                                                     @RequestParam String locale) {
        return ApiResponse.ok(genI18nMessageService.getByTableIdAndLocale(tableId, locale));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.EDIT + "')")
    @OperationLog(module = "Code Generator I18n", businessType = "SAVE")
    @Idempotent("tool:gen:i18n:save")
    @PutMapping("/tool/gen/i18n/messages")
    public ApiResponse<Void> save(@RequestBody GenI18nMessageSaveRequest request) {
        GenI18nMessage row = new GenI18nMessage();
        row.setId(request.getId());
        row.setTableId(request.getTableId());
        row.setLocale(request.getLocale());
        row.setMessagesJson(request.getMessagesJson());
        row.setRemark(request.getRemark());
        genI18nMessageService.save(row);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.EDIT + "')")
    @OperationLog(module = "Code Generator I18n", businessType = "SYNC")
    @Idempotent("tool:gen:i18n:sync")
    @PostMapping("/tool/gen/i18n/sync/{tableId}")
    public ApiResponse<Void> sync(@PathVariable String tableId) {
        GenTable table = genTableService.selectGenTableById(tableId);
        genI18nMessageService.syncFromTable(table);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.QUERY + "')")
    @GetMapping("/tool/gen/i18n/baseline")
    public ApiResponse<GenBaselineJsonResponse> i18nBaseline(@RequestParam String tableId,
                                                               @RequestParam String locale) {
        GenTable table = genTableService.selectGenTableById(tableId);
        String json = genI18nMessageService.getGitBaselineJson(table.getBusinessName(), locale);
        return ApiResponse.ok(new GenBaselineJsonResponse(json));
    }

    @PreAuthorize("@authz.hasPermission('" + GenPermissionConstants.EDIT + "')")
    @OperationLog(module = "Code Generator I18n", businessType = "RESTORE_BASELINE")
    @Idempotent("tool:gen:i18n:restoreBaseline")
    @PostMapping("/tool/gen/i18n/restore-baseline")
    public ApiResponse<Void> restoreI18nBaseline(@RequestParam String tableId,
                                                 @RequestParam String locale) {
        GenTable table = genTableService.selectGenTableById(tableId);
        genI18nMessageService.restoreGitBaseline(tableId, table.getBusinessName(), locale);
        return ApiResponse.ok();
    }
}
