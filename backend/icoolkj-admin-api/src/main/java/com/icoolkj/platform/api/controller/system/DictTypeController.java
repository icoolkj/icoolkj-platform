package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.dict.dto.DictTypePageQuery;
import com.icoolkj.platform.system.dict.dto.DictTypeCreateRequest;
import com.icoolkj.platform.system.dict.dto.DictTypeUpdateRequest;
import com.icoolkj.platform.system.dict.service.DictTypeService;
import com.icoolkj.platform.system.dict.vo.DictTypeVO;
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


/** 字典类型管理 */
@RestController
@RequestMapping("/system/dictType")
public class DictTypeController {

    private final DictTypeService dictTypeService;

    public DictTypeController(DictTypeService dictTypeService) {
        this.dictTypeService = dictTypeService;
    }

    @PreAuthorize("@authz.hasPermission('system:dict:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<DictTypeVO>> page(DictTypePageQuery query) {
        return ApiResponse.ok(dictTypeService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:dict:list')")
    @GetMapping("/{id}")
    public ApiResponse<DictTypeVO> getById(@PathVariable String id) {
        return ApiResponse.ok(dictTypeService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:dict:add')")
    @OperationLog(module = "System Dict Type", businessType = "CREATE")
    @Idempotent("system:dictType:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody DictTypeCreateRequest request) {
        return ApiResponse.ok(dictTypeService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:dict:update')")
    @OperationLog(module = "System Dict Type", businessType = "UPDATE")
    @Idempotent("system:dictType:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody DictTypeUpdateRequest request) {
        dictTypeService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:dict:delete')")
    @OperationLog(module = "System Dict Type", businessType = "DELETE")
    @Idempotent("system:dictType:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        dictTypeService.deleteById(id);
        return ApiResponse.ok();
    }

}
