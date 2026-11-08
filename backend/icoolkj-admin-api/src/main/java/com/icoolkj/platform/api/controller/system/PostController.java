package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.post.dto.PostPageQuery;
import com.icoolkj.platform.system.post.dto.PostCreateRequest;
import com.icoolkj.platform.system.post.dto.PostUpdateRequest;
import com.icoolkj.platform.system.post.service.PostService;
import com.icoolkj.platform.system.post.vo.PostVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;


/** 岗位管理 */
@RestController
@RequestMapping("/system/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PreAuthorize("@authz.hasPermission('system:post:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<PostVO>> page(PostPageQuery query) {
        return ApiResponse.ok(postService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:post:list')")
    @GetMapping("/{id}")
    public ApiResponse<PostVO> getById(@PathVariable String id) {
        return ApiResponse.ok(postService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:post:add')")
    @OperationLog(module = "System Post", businessType = "CREATE")
    @Idempotent("system:post:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody PostCreateRequest request) {
        return ApiResponse.ok(postService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:post:update')")
    @OperationLog(module = "System Post", businessType = "UPDATE")
    @Idempotent("system:post:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody PostUpdateRequest request) {
        postService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:post:delete')")
    @OperationLog(module = "System Post", businessType = "DELETE")
    @Idempotent("system:post:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        postService.deleteById(id);
        return ApiResponse.ok();
    }

}
