package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.banner.dto.BannerBatchDeleteRequest;
import com.icoolkj.platform.system.banner.dto.BannerCreateRequest;
import com.icoolkj.platform.system.banner.dto.BannerPageQuery;
import com.icoolkj.platform.system.banner.dto.BannerPublishedQuery;
import com.icoolkj.platform.system.banner.dto.BannerStatusUpdateRequest;
import com.icoolkj.platform.system.banner.dto.BannerUpdateRequest;
import com.icoolkj.platform.system.banner.service.BannerService;
import com.icoolkj.platform.system.banner.vo.BannerListVO;
import com.icoolkj.platform.system.banner.vo.BannerPublishedVO;
import com.icoolkj.platform.system.banner.vo.BannerVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 轮播图管理 */
@RestController
@RequestMapping("/system/banner")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @PreAuthorize("@authz.hasPermission('system:banner:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<BannerListVO>> page(BannerPageQuery query) {
        return ApiResponse.ok(bannerService.page(query));
    }

    @PreAuthorize("@authz.isAuthenticated()")
    @GetMapping("/published")
    public ApiResponse<List<BannerPublishedVO>> listPublished(@Valid BannerPublishedQuery query) {
        return ApiResponse.ok(bannerService.listPublished(query));
    }

    @PreAuthorize("@authz.hasPermission('system:banner:list')")
    @GetMapping("/{id}")
    public ApiResponse<BannerVO> getById(@PathVariable String id) {
        return ApiResponse.ok(bannerService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:banner:add')")
    @OperationLog(module = "System Banner", businessType = "CREATE")
    @Idempotent("system:banner:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody BannerCreateRequest request) {
        return ApiResponse.ok(bannerService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:banner:update')")
    @OperationLog(module = "System Banner", businessType = "UPDATE")
    @Idempotent("system:banner:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody BannerUpdateRequest request) {
        bannerService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:banner:update')")
    @OperationLog(module = "System Banner", businessType = "UPDATE")
    @Idempotent("system:banner:updateStatus")
    @PutMapping("/status")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody BannerStatusUpdateRequest request) {
        bannerService.updateStatus(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:banner:delete')")
    @OperationLog(module = "System Banner", businessType = "DELETE")
    @Idempotent("system:banner:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        bannerService.deleteById(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:banner:delete')")
    @OperationLog(module = "System Banner", businessType = "DELETE")
    @Idempotent("system:banner:batchDelete")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody BannerBatchDeleteRequest request) {
        bannerService.batchDelete(request);
        return ApiResponse.ok();
    }
}
