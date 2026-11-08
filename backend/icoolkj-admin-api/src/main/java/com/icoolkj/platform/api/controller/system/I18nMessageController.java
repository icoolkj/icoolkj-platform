package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.i18n.dto.I18nMessageCreateRequest;
import com.icoolkj.platform.system.i18n.dto.I18nMessagePageQuery;
import com.icoolkj.platform.system.i18n.dto.I18nMessageUpdateRequest;
import com.icoolkj.platform.system.i18n.service.I18nMessageAdminService;
import com.icoolkj.platform.system.i18n.vo.I18nMessageVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 国际化文案管理 */
@RestController
@RequestMapping("/system/i18n/message")
public class I18nMessageController {

    private final I18nMessageAdminService i18nMessageAdminService;

    public I18nMessageController(I18nMessageAdminService i18nMessageAdminService) {
        this.i18nMessageAdminService = i18nMessageAdminService;
    }

    @PreAuthorize("@authz.hasPermission('system:i18nMessage:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<I18nMessageVO>> page(I18nMessagePageQuery query) {
        return ApiResponse.ok(i18nMessageAdminService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:i18nMessage:list')")
    @GetMapping("/{id}")
    public ApiResponse<I18nMessageVO> getById(@PathVariable String id) {
        return ApiResponse.ok(i18nMessageAdminService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:i18nMessage:add')")
    @OperationLog(module = "System I18n Message", businessType = "CREATE")
    @Idempotent("system:i18nMessage:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody I18nMessageCreateRequest request) {
        return ApiResponse.ok(i18nMessageAdminService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:i18nMessage:update')")
    @OperationLog(module = "System I18n Message", businessType = "UPDATE")
    @Idempotent("system:i18nMessage:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody I18nMessageUpdateRequest request) {
        i18nMessageAdminService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:i18nMessage:update')")
    @OperationLog(module = "System I18n Message", businessType = "REFRESH_CACHE")
    @Idempotent("system:i18nMessage:refreshCache")
    @PostMapping("/refreshCache")
    public ApiResponse<Void> refreshCache() {
        i18nMessageAdminService.refreshCache();
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:i18nMessage:delete')")
    @OperationLog(module = "System I18n Message", businessType = "DELETE")
    @Idempotent("system:i18nMessage:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        i18nMessageAdminService.deleteById(id);
        return ApiResponse.ok();
    }
}
