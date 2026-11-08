package com.icoolkj.platform.generator.controller;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.generator.constant.AiPermissionConstants;
import com.icoolkj.platform.generator.dto.GenPageSpecCompileRequest;
import com.icoolkj.platform.generator.dto.GenPageSpecGenerateRequest;
import com.icoolkj.platform.generator.dto.GenPageSpecPublishRequest;
import com.icoolkj.platform.generator.dto.GenPageSpecValidateRequest;
import com.icoolkj.platform.generator.service.GenPageSpecService;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.generator.dto.GenTableAlterApplyRequest;
import com.icoolkj.platform.generator.dto.GenTableAlterDesignRequest;
import com.icoolkj.platform.generator.dto.GenTableCreateRequest;
import com.icoolkj.platform.generator.dto.GenTableDdlExecuteRequest;
import com.icoolkj.platform.generator.dto.GenTableDesignRequest;
import com.icoolkj.platform.generator.dto.GenTablePageQuery;
import com.icoolkj.platform.generator.dto.GenTablePrepareRequest;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.service.impl.PageSpecTableService;
import com.icoolkj.platform.generator.vo.GenPageSpecCompileResultVO;
import com.icoolkj.platform.generator.vo.GenPageSpecDraftVO;
import com.icoolkj.platform.generator.vo.GenPageSpecValidateResultVO;
import com.icoolkj.platform.generator.vo.GenModuleVO;
import com.icoolkj.platform.generator.vo.GenTableAlterDesignVO;
import com.icoolkj.platform.generator.vo.GenTableDdlDesignVO;
import com.icoolkj.platform.generator.vo.GenTableDdlExecuteResultVO;
import com.icoolkj.platform.generator.vo.GenTableDesignVO;
import com.icoolkj.platform.generator.vo.GenTablePrepareResultVO;
import com.icoolkj.platform.generator.vo.GenTableSchemaVO;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 页面规格 AI 生成 — /tool/page-spec */
@RestController
@ConditionalOnProperty(prefix = "icoolkj.generator", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping("/tool/page-spec")
public class GenPageSpecController {

    private final GenPageSpecService genPageSpecService;
    private final PageSpecTableService pageSpecTableService;

    public GenPageSpecController(GenPageSpecService genPageSpecService,
                                 PageSpecTableService pageSpecTableService) {
        this.genPageSpecService = genPageSpecService;
        this.pageSpecTableService = pageSpecTableService;
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.QUERY + "')")
    @GetMapping("/modules")
    public ApiResponse<List<GenModuleVO>> modules() {
        return ApiResponse.ok(pageSpecTableService.listModules());
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.QUERY + "')")
    @GetMapping("/db-tables")
    public ApiResponse<PageResult<GenTable>> dbTables(GenTablePageQuery query) {
        return ApiResponse.ok(pageSpecTableService.pageDbTables(query));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "') and @authz.hasPermission('tool:gen:import')")
    @OperationLog(module = "Page Spec AI", businessType = "PREPARE_TABLE")
    @Idempotent("tool:ai:prepareTable")
    @PostMapping("/prepare-table")
    public ApiResponse<GenTablePrepareResultVO> prepareTable(@Valid @RequestBody GenTablePrepareRequest request) {
        return ApiResponse.ok(pageSpecTableService.prepareTable(request.getTableName()));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @OperationLog(module = "Page Spec AI", businessType = "DESIGN_CREATE_DDL")
    @Idempotent("tool:ai:designCreateDdl")
    @PostMapping("/design-create-ddl")
    public ApiResponse<GenTableDdlDesignVO> designCreateDdl(@Valid @RequestBody GenTableDesignRequest request) {
        return ApiResponse.ok(pageSpecTableService.designCreateDdl(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @OperationLog(module = "Page Spec AI", businessType = "DESIGN_ALTER_DDL")
    @Idempotent("tool:ai:designAlterDdl")
    @PostMapping("/design-alter-ddl")
    public ApiResponse<GenTableDdlDesignVO> designAlterDdl(@Valid @RequestBody GenTableAlterDesignRequest request) {
        return ApiResponse.ok(pageSpecTableService.designAlterDdl(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "') and @authz.hasPermission('tool:gen:import')")
    @OperationLog(module = "Page Spec AI", businessType = "EXECUTE_DDL")
    @Idempotent("tool:ai:executeDdl")
    @PostMapping("/execute-ddl")
    public ApiResponse<GenTableDdlExecuteResultVO> executeDdl(@Valid @RequestBody GenTableDdlExecuteRequest request) {
        return ApiResponse.ok(pageSpecTableService.executeDdl(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @OperationLog(module = "Page Spec AI", businessType = "DESIGN_TABLE")
    @Idempotent("tool:ai:designTable")
    @PostMapping("/design-table")
    public ApiResponse<GenTableDesignVO> designTable(@Valid @RequestBody GenTableDesignRequest request) {
        return ApiResponse.ok(pageSpecTableService.designTable(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @PostMapping("/preview-create-table")
    public ApiResponse<String> previewCreateTable(@Valid @RequestBody GenTableCreateRequest request) {
        return ApiResponse.ok(pageSpecTableService.previewCreateTable(request.getDesign()));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @PostMapping("/preview-alter-table")
    public ApiResponse<String> previewAlterTable(@Valid @RequestBody GenTableAlterApplyRequest request) {
        return ApiResponse.ok(pageSpecTableService.previewAlterTable(request.getDesign()));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "') and @authz.hasPermission('tool:gen:import')")
    @OperationLog(module = "Page Spec AI", businessType = "CREATE_TABLE")
    @Idempotent("tool:ai:createTable")
    @PostMapping("/create-table")
    public ApiResponse<GenTablePrepareResultVO> createTable(@Valid @RequestBody GenTableCreateRequest request) {
        return ApiResponse.ok(pageSpecTableService.createTable(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.QUERY + "')")
    @GetMapping("/table-schema")
    public ApiResponse<GenTableSchemaVO> tableSchema(@RequestParam String tableName) {
        return ApiResponse.ok(pageSpecTableService.getTableSchema(tableName));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @OperationLog(module = "Page Spec AI", businessType = "DESIGN_ALTER_TABLE")
    @Idempotent("tool:ai:designAlterTable")
    @PostMapping("/design-alter-table")
    public ApiResponse<GenTableAlterDesignVO> designAlterTable(@Valid @RequestBody GenTableAlterDesignRequest request) {
        return ApiResponse.ok(pageSpecTableService.designAlterTable(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "') and @authz.hasPermission('tool:gen:import')")
    @OperationLog(module = "Page Spec AI", businessType = "APPLY_ALTER_TABLE")
    @Idempotent("tool:ai:applyAlterTable")
    @PostMapping("/apply-alter-table")
    public ApiResponse<GenTablePrepareResultVO> applyAlterTable(@Valid @RequestBody GenTableAlterApplyRequest request) {
        return ApiResponse.ok(pageSpecTableService.applyAlterTable(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.QUERY + "')")
    @GetMapping("/prompt-template")
    public ApiResponse<String> promptTemplate() {
        return ApiResponse.ok(genPageSpecService.getPromptTemplate());
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.QUERY + "')")
    @GetMapping("/draft")
    public ApiResponse<GenPageSpecDraftVO> draft(
            @RequestParam String tableId,
            @RequestParam(required = false) String pageKey,
            @RequestParam(required = false, defaultValue = "standard-crud") String layout) {
        return ApiResponse.ok(genPageSpecService.draftFromTable(tableId, pageKey, layout));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @OperationLog(module = "Page Spec AI", businessType = "ANALYZE")
    @Idempotent("tool:ai:analyze")
    @PostMapping("/analyze")
    public ApiResponse<GenPageSpecDraftVO> analyze(@Valid @RequestBody GenPageSpecGenerateRequest request) {
        return ApiResponse.ok(genPageSpecService.analyze(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "') and @authz.hasPermission('tool:gen:code')")
    @OperationLog(module = "Page Spec AI", businessType = "PUBLISH")
    @Idempotent("tool:ai:publish")
    @PostMapping("/publish")
    public ApiResponse<GenPageSpecCompileResultVO> publish(@Valid @RequestBody GenPageSpecPublishRequest request) {
        return ApiResponse.ok(genPageSpecService.publish(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @OperationLog(module = "Page Spec AI", businessType = "GENERATE")
    @Idempotent("tool:ai:generate")
    @PostMapping("/generate")
    public ApiResponse<String> generate(@Valid @RequestBody GenPageSpecGenerateRequest request) {
        return ApiResponse.ok(genPageSpecService.generate(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @PostMapping("/validate")
    public ApiResponse<GenPageSpecValidateResultVO> validate(@Valid @RequestBody GenPageSpecValidateRequest request) {
        return ApiResponse.ok(genPageSpecService.validate(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "') and @authz.hasPermission('tool:gen:code')")
    @OperationLog(module = "Page Spec AI", businessType = "COMPILE")
    @Idempotent("tool:ai:compile")
    @PostMapping("/compile")
    public ApiResponse<GenPageSpecCompileResultVO> compile(@Valid @RequestBody GenPageSpecCompileRequest request) {
        return ApiResponse.ok(genPageSpecService.compile(request));
    }
}
