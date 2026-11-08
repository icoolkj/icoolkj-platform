package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.product.dto.ProductCreateRequest;
import com.icoolkj.platform.system.product.dto.ProductPageQuery;
import com.icoolkj.platform.system.product.dto.ProductUpdateRequest;
import com.icoolkj.platform.system.product.service.ProductQueryService;
import com.icoolkj.platform.system.product.service.ProductService;
import com.icoolkj.platform.system.product.vo.ProductManageVO;
import com.icoolkj.platform.system.product.vo.ProductVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 产品宇宙（Console Shell + 平台元数据） */
@RestController
@RequestMapping("/system/product")
public class ProductController {

    private final ProductQueryService productQueryService;
    private final ProductService productService;

    public ProductController(ProductQueryService productQueryService, ProductService productService) {
        this.productQueryService = productQueryService;
        this.productService = productService;
    }

    /** 当前用户可见产品（由已授权菜单推导，按 sort_no：业务靠前） */
    @GetMapping("/mine")
    public ApiResponse<List<ProductVO>> mine() {
        LoginUser loginUser = SecurityContextHelper.getLoginUser();
        if (loginUser == null) {
            throw new UnauthorizedException("error.unauthorized");
        }
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        return ApiResponse.ok(productQueryService.listMine(loginUser, locale));
    }

    /** 管理端：全部启用产品（菜单表单下拉） */
    @PreAuthorize("@authz.hasPermission('system:menu:list')")
    @GetMapping("/list")
    public ApiResponse<List<ProductVO>> list() {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        return ApiResponse.ok(productQueryService.listAllActive(locale));
    }

    @PreAuthorize("@authz.hasPermission('system:product:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<ProductManageVO>> page(ProductPageQuery query) {
        return ApiResponse.ok(productService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:product:list')")
    @GetMapping("/{id}")
    public ApiResponse<ProductManageVO> getById(@PathVariable String id) {
        return ApiResponse.ok(productService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:product:add')")
    @OperationLog(module = "System Product", businessType = "CREATE")
    @Idempotent("system:product:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:product:update')")
    @OperationLog(module = "System Product", businessType = "UPDATE")
    @Idempotent("system:product:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody ProductUpdateRequest request) {
        productService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:product:delete')")
    @OperationLog(module = "System Product", businessType = "DELETE")
    @Idempotent("system:product:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        productService.deleteById(id);
        return ApiResponse.ok();
    }
}
