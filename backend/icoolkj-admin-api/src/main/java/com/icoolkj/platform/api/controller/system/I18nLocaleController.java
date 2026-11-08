package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.i18n.dto.I18nLocaleCreateRequest;
import com.icoolkj.platform.system.i18n.dto.I18nLocalePageQuery;
import com.icoolkj.platform.system.i18n.dto.I18nLocaleUpdateRequest;
import com.icoolkj.platform.system.i18n.service.I18nLocaleAdminService;
import com.icoolkj.platform.system.i18n.vo.I18nLocaleVO;
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

/** 国际化语言配置管理 */
@RestController
@RequestMapping("/system/i18n/locale")
public class I18nLocaleController {

    private final I18nLocaleAdminService i18nLocaleAdminService;

    public I18nLocaleController(I18nLocaleAdminService i18nLocaleAdminService) {
        this.i18nLocaleAdminService = i18nLocaleAdminService;
    }

    @PreAuthorize("@authz.hasPermission('system:i18nLocale:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<I18nLocaleVO>> page(I18nLocalePageQuery query) {
        return ApiResponse.ok(i18nLocaleAdminService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:i18nLocale:list')")
    @GetMapping("/{id}")
    public ApiResponse<I18nLocaleVO> getById(@PathVariable String id) {
        return ApiResponse.ok(i18nLocaleAdminService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:i18nLocale:add')")
    @OperationLog(module = "System I18n Locale", businessType = "CREATE")
    @Idempotent("system:i18nLocale:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody I18nLocaleCreateRequest request) {
        return ApiResponse.ok(i18nLocaleAdminService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:i18nLocale:update')")
    @OperationLog(module = "System I18n Locale", businessType = "UPDATE")
    @Idempotent("system:i18nLocale:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody I18nLocaleUpdateRequest request) {
        i18nLocaleAdminService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:i18nLocale:delete')")
    @OperationLog(module = "System I18n Locale", businessType = "DELETE")
    @Idempotent("system:i18nLocale:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        i18nLocaleAdminService.deleteById(id);
        return ApiResponse.ok();
    }
}
