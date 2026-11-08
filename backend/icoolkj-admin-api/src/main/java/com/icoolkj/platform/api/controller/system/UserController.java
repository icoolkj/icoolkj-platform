package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.infra.excel.ExcelExportAsyncSubmitFacade;
import com.icoolkj.platform.infra.excel.ExcelImportAsyncSubmitFacade;
import com.icoolkj.platform.infra.service.UserAvatarFileService;
import com.icoolkj.platform.infra.vo.AsyncTaskSubmitVO;
import com.icoolkj.platform.infra.vo.UserAvatarPolicyVO;
import com.icoolkj.platform.infra.vo.UserAvatarUploadVO;
import com.icoolkj.platform.system.user.dto.UserAvatarBindRequest;
import com.icoolkj.platform.system.user.dto.UserPageQuery;
import com.icoolkj.platform.system.user.dto.UserRoleAssignRequest;
import com.icoolkj.platform.system.user.dto.UserCreateRequest;
import com.icoolkj.platform.system.user.dto.UserResetPasswordRequest;
import com.icoolkj.platform.system.user.dto.UserUpdateRequest;
import com.icoolkj.platform.system.user.excel.UserExcelExportAsyncHandler;
import com.icoolkj.platform.system.user.excel.UserExcelImportAsyncHandler;
import com.icoolkj.platform.system.user.service.UserProfileService;
import com.icoolkj.platform.system.user.service.UserRoleService;
import com.icoolkj.platform.system.user.service.UserService;
import com.icoolkj.platform.system.user.vo.UserProfileVO;
import com.icoolkj.platform.framework.excel.ExcelExportSupport;
import com.icoolkj.platform.framework.excel.ExcelImportLookup;
import com.icoolkj.platform.framework.excel.ExcelImportResult;
import com.icoolkj.platform.framework.excel.ExcelImportSupport;
import com.icoolkj.platform.framework.excel.ExcelValueFormatters;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.system.user.excel.UserExportRow;
import com.icoolkj.platform.system.user.excel.UserImportRow;
import com.icoolkj.platform.system.user.vo.UserVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;


/** 用户管理 */
@RestController
@RequestMapping("/system/user")
public class UserController {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final UserAvatarFileService userAvatarFileService;
    private final UserProfileService userProfileService;
    private final I18nMessageResolver i18nMessageResolver;
    private final ExcelImportLookup excelImportLookup;
    private final ExcelImportAsyncSubmitFacade excelImportAsyncSubmitFacade;
    private final ExcelExportAsyncSubmitFacade excelExportAsyncSubmitFacade;

    public UserController(UserService userService,
                          UserRoleService userRoleService,
                          UserAvatarFileService userAvatarFileService,
                          UserProfileService userProfileService,
                          I18nMessageResolver i18nMessageResolver,
                          ExcelImportLookup excelImportLookup,
                          ExcelImportAsyncSubmitFacade excelImportAsyncSubmitFacade,
                          ExcelExportAsyncSubmitFacade excelExportAsyncSubmitFacade) {
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.userAvatarFileService = userAvatarFileService;
        this.userProfileService = userProfileService;
        this.i18nMessageResolver = i18nMessageResolver;
        this.excelImportLookup = excelImportLookup;
        this.excelImportAsyncSubmitFacade = excelImportAsyncSubmitFacade;
        this.excelExportAsyncSubmitFacade = excelExportAsyncSubmitFacade;
    }

    @PreAuthorize("@authz.hasPermission('system:user:list')")
    @GetMapping("/avatar-policy")
    public ApiResponse<UserAvatarPolicyVO> avatarPolicy() {
        return ApiResponse.ok(userAvatarFileService.policy());
    }

    @PreAuthorize("@authz.hasPermission('system:user:update')")
    @OperationLog(module = "System User", businessType = "UPDATE")
    @Idempotent("system:user:avatar")
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileVO> uploadAvatar(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        String tenantId = SecurityContextHelper.getTenantId();
        String tenantCode = SecurityContextHelper.getLoginUser() != null
                ? SecurityContextHelper.getLoginUser().getTenantCode()
                : null;
        UserAvatarUploadVO uploaded = userAvatarFileService.uploadForUser(tenantId, id, file);
        UserAvatarBindRequest bind = new UserAvatarBindRequest();
        bind.setAvatarFileId(uploaded.getAvatarFileId());
        bind.setAvatarUrl(uploaded.getAvatarUrl());
        bind.setAvatarUrlSm(uploaded.getAvatarUrlSm());
        bind.setAvatarUrlLg(uploaded.getAvatarUrlLg());
        return ApiResponse.ok(userProfileService.applyAvatar(tenantId, tenantCode, id, bind));
    }

    @PreAuthorize("@authz.hasPermission('system:user:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<UserVO>> page(UserPageQuery query) {
        return ApiResponse.ok(userService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:user:export')")
    @OperationLog(module = "System User", businessType = "EXPORT")
    @GetMapping("/export")
    public void export(UserPageQuery query, HttpServletResponse response) {
        String statusNormal = ExcelValueFormatters.pageText(i18nMessageResolver, "user", "statusNormal");
        String statusDisabled = ExcelValueFormatters.pageText(i18nMessageResolver, "user", "statusDisabled");
        ExcelExportSupport.writeLocalizedPaged(
                response,
                "user",
                UserExportRow.class,
                query,
                userService::page,
                vo -> {
                    UserExportRow row = new UserExportRow();
                    row.setUsername(vo.getUsername());
                    row.setNickname(vo.getNickname());
                    row.setRealName(vo.getRealName());
                    row.setDeptName(vo.getDeptName());
                    row.setPhone(vo.getPhone());
                    row.setEmail(vo.getEmail());
                    row.setStatus(ExcelValueFormatters.commonStatus(vo.getStatus(), statusNormal, statusDisabled));
                    row.setCreatedAt(ExcelValueFormatters.dateTime(vo.getCreatedAt()));
                    return row;
                },
                i18nMessageResolver);
    }

    /**
     * Async export: submit query → poll {@code GET /infra/asyncTask/mine/{id}} → download staged file.
     * Sync {@link #export} remains available; FE prefers async when wired.
     */
    @PreAuthorize("@authz.hasPermission('system:user:export')")
    @OperationLog(module = "System User", businessType = "EXPORT")
    @Idempotent("system:user:export-async")
    @PostMapping("/export-async")
    public ApiResponse<AsyncTaskSubmitVO> exportAsync(UserPageQuery query) throws Exception {
        return ApiResponse.ok(excelExportAsyncSubmitFacade.submit(
                UserExcelExportAsyncHandler.TASK_TYPE,
                ExcelValueFormatters.pageText(i18nMessageResolver, "user", "exportAsyncTaskName"),
                query));
    }

    @PreAuthorize("@authz.hasPermission('system:user:import')")
    @GetMapping("/import-template")
    public void importTemplate(HttpServletResponse response) {
        ExcelImportSupport.writeImportTemplate(
                response, "user", UserImportRow.class, i18nMessageResolver, excelImportLookup);
    }

    @PreAuthorize("@authz.hasPermission('system:user:import')")
    @OperationLog(module = "System User", businessType = "IMPORT")
    @Idempotent("system:user:import")
    @PostMapping("/import")
    public ApiResponse<ExcelImportResult> importExcel(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(userService.importRows(ExcelImportSupport.readRows(file, UserImportRow.class)));
    }

    /**
     * Async import: stage file → submit task → poll {@code GET /infra/asyncTask/mine/{id}}.
     * Sync {@link #importExcel} remains available; FE prefers async when wired.
     */
    @PreAuthorize("@authz.hasPermission('system:user:import')")
    @OperationLog(module = "System User", businessType = "IMPORT")
    @Idempotent("system:user:import-async")
    @PostMapping("/import-async")
    public ApiResponse<AsyncTaskSubmitVO> importExcelAsync(@RequestParam("file") MultipartFile file) throws Exception {
        return ApiResponse.ok(excelImportAsyncSubmitFacade.submit(
                file,
                UserExcelImportAsyncHandler.TASK_TYPE,
                ExcelValueFormatters.pageText(i18nMessageResolver, "user", "importAsyncTaskName"),
                "user excel import"));
    }

    @PreAuthorize("@authz.hasPermission('system:user:list')")
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getById(@PathVariable String id) {
        return ApiResponse.ok(userService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:user:add')")
    @OperationLog(module = "System User", businessType = "CREATE")
    @Idempotent("system:user:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:user:update')")
    @OperationLog(module = "System User", businessType = "UPDATE")
    @Idempotent("system:user:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody UserUpdateRequest request) {
        userService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:user:delete')")
    @OperationLog(module = "System User", businessType = "DELETE")
    @Idempotent("system:user:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        String tenantId = SecurityContextHelper.getTenantId();
        userAvatarFileService.releaseForUser(tenantId, id);
        userService.deleteById(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:user:list')")
    @GetMapping("/{id}/roleIds")
    public ApiResponse<List<String>> listRoleIds(@PathVariable String id) {
        return ApiResponse.ok(userRoleService.listRoleIdsByUser(id));
    }

    @PreAuthorize("@authz.hasPermission('system:user:update')")
    @OperationLog(module = "System User", businessType = "ASSIGN_ROLE")
    @Idempotent("system:user:assignRoles")
    @PutMapping("/{id}/roles")
    public ApiResponse<Void> assignRoles(@PathVariable String id, @Valid @RequestBody UserRoleAssignRequest request) {
        userRoleService.assignRoles(id, request.getRoleIds());
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:user:resetPassword')")
    @OperationLog(module = "System User", businessType = "RESET_PASSWORD")
    @Idempotent("system:user:resetPassword")
    @PutMapping("/{id}/password/reset")
    public ApiResponse<Void> resetPassword(@PathVariable String id,
                                           @RequestBody(required = false) UserResetPasswordRequest request) {
        userService.resetPassword(id, request == null ? new UserResetPasswordRequest() : request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:user:unlock')")
    @OperationLog(module = "System User", businessType = "UNLOCK")
    @Idempotent("system:user:unlock")
    @PutMapping("/{id}/unlock")
    public ApiResponse<Void> unlock(@PathVariable String id) {
        userService.unlock(id);
        return ApiResponse.ok();
    }

}
