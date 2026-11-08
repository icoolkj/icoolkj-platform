package com.icoolkj.platform.api.controller.infra;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.infra.dto.AsyncTaskPageQuery;
import com.icoolkj.platform.infra.service.AsyncTaskService;
import com.icoolkj.platform.infra.vo.AsyncTaskVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 异步任务管理 */
@RestController
@RequestMapping("/infra/asyncTask")
public class AsyncTaskController {

    private final AsyncTaskService asyncTaskService;

    public AsyncTaskController(AsyncTaskService asyncTaskService) {
        this.asyncTaskService = asyncTaskService;
    }

    @PreAuthorize("@authz.hasPermission('infra:asyncTask:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<AsyncTaskVO>> page(AsyncTaskPageQuery query) {
        return ApiResponse.ok(asyncTaskService.page(query));
    }

    /**
     * Owner-only status for async Excel import (and other submitters).
     * Authenticated; does not require {@code infra:asyncTask:list}.
     */
    @PreAuthorize("@authz.isAuthenticated()")
    @GetMapping("/mine/{id}")
    public ApiResponse<AsyncTaskVO> getMineById(@PathVariable String id) {
        return ApiResponse.ok(asyncTaskService.getMineById(id));
    }

    @PreAuthorize("@authz.hasPermission('infra:asyncTask:list')")
    @GetMapping("/{id}")
    public ApiResponse<AsyncTaskVO> getById(@PathVariable String id) {
        return ApiResponse.ok(asyncTaskService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('infra:asyncTask:delete')")
    @OperationLog(module = "Infra Async Task", businessType = "DELETE")
    @Idempotent("infra:asyncTask:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        asyncTaskService.deleteById(id);
        return ApiResponse.ok();
    }

}
