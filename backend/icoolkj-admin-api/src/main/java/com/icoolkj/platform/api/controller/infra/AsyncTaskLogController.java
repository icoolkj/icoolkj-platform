package com.icoolkj.platform.api.controller.infra;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.infra.dto.AsyncTaskLogPageQuery;
import com.icoolkj.platform.infra.service.AsyncTaskLogService;
import com.icoolkj.platform.infra.vo.AsyncTaskLogVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 异步任务日志管理 */
@RestController
@RequestMapping("/infra/asyncTaskLog")
public class AsyncTaskLogController {

    private final AsyncTaskLogService asyncTaskLogService;

    public AsyncTaskLogController(AsyncTaskLogService asyncTaskLogService) {
        this.asyncTaskLogService = asyncTaskLogService;
    }

    @PreAuthorize("@authz.hasPermission('infra:asyncTaskLog:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<AsyncTaskLogVO>> page(AsyncTaskLogPageQuery query) {
        return ApiResponse.ok(asyncTaskLogService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('infra:asyncTaskLog:list')")
    @GetMapping("/{id}")
    public ApiResponse<AsyncTaskLogVO> getById(@PathVariable String id) {
        return ApiResponse.ok(asyncTaskLogService.getById(id));
    }

}
