package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.dict.dto.DictItemPageQuery;
import com.icoolkj.platform.system.dict.dto.DictItemCreateRequest;
import com.icoolkj.platform.system.dict.dto.DictItemUpdateRequest;
import com.icoolkj.platform.system.dict.service.DictItemService;
import com.icoolkj.platform.system.dict.vo.DictItemVO;
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


/** 字典项管理 */
@RestController
@RequestMapping("/system/dictItem")
public class DictItemController {

    private final DictItemService dictItemService;

    public DictItemController(DictItemService dictItemService) {
        this.dictItemService = dictItemService;
    }

    @PreAuthorize("@authz.hasPermission('system:dict:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<DictItemVO>> page(DictItemPageQuery query) {
        return ApiResponse.ok(dictItemService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:dict:list')")
    @GetMapping("/{id}")
    public ApiResponse<DictItemVO> getById(@PathVariable String id) {
        return ApiResponse.ok(dictItemService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:dict:add')")
    @OperationLog(module = "System Dict Item", businessType = "CREATE")
    @Idempotent("system:dictItem:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody DictItemCreateRequest request) {
        return ApiResponse.ok(dictItemService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:dict:update')")
    @OperationLog(module = "System Dict Item", businessType = "UPDATE")
    @Idempotent("system:dictItem:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody DictItemUpdateRequest request) {
        dictItemService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:dict:delete')")
    @OperationLog(module = "System Dict Item", businessType = "DELETE")
    @Idempotent("system:dictItem:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        dictItemService.deleteById(id);
        return ApiResponse.ok();
    }

}
