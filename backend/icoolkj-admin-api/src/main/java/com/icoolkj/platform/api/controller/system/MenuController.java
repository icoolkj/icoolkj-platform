package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.menu.dto.MenuCreateRequest;
import com.icoolkj.platform.system.menu.dto.MenuPageQuery;
import com.icoolkj.platform.system.menu.dto.MenuUpdateRequest;
import com.icoolkj.platform.system.menu.service.MenuQueryService;
import com.icoolkj.platform.system.menu.service.MenuService;
import com.icoolkj.platform.system.menu.vo.MenuRouterVO;
import com.icoolkj.platform.system.menu.vo.MenuVO;
import jakarta.validation.Valid;
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
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;

/** 菜单管理 */
@RestController
@RequestMapping("/system/menu")
public class MenuController {

    private final MenuService menuService;
    private final MenuQueryService menuQueryService;

    public MenuController(MenuService menuService, MenuQueryService menuQueryService) {
        this.menuService = menuService;
        this.menuQueryService = menuQueryService;
    }

    /** 当前用户可访问的路由树（侧边栏）；可选按产品宇宙过滤 */
    @GetMapping("/routers")
    public ApiResponse<List<MenuRouterVO>> routers(
            @RequestParam(value = "productCode", required = false) String productCode) {
        LoginUser loginUser = SecurityContextHelper.getLoginUser();
        if (loginUser == null) {
            throw new UnauthorizedException("error.unauthorized");
        }
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        return ApiResponse.ok(menuQueryService.getRouters(loginUser, locale, productCode));
    }

    @PreAuthorize("@authz.hasPermission('system:menu:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<MenuVO>> page(MenuPageQuery query) {
        return ApiResponse.ok(menuService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:menu:list')")
    @GetMapping("/{id}")
    public ApiResponse<MenuVO> getById(@PathVariable String id) {
        return ApiResponse.ok(menuService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:menu:add')")
    @OperationLog(module = "System Menu", businessType = "CREATE")
    @Idempotent("system:menu:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody MenuCreateRequest request) {
        return ApiResponse.ok(menuService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:menu:update')")
    @OperationLog(module = "System Menu", businessType = "UPDATE")
    @Idempotent("system:menu:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody MenuUpdateRequest request) {
        menuService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:menu:delete')")
    @OperationLog(module = "System Menu", businessType = "DELETE")
    @Idempotent("system:menu:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        menuService.deleteById(id);
        return ApiResponse.ok();
    }
}
