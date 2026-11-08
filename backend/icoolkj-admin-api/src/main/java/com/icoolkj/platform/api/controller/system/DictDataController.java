package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.system.dict.service.DictDataService;
import com.icoolkj.platform.system.dict.vo.DictDataVO;
import com.icoolkj.platform.system.dict.vo.DictTypeOptionVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Runtime dictionary data — used by generated CRUD pages (authenticated). */
@RestController
@RequestMapping("/system/dict/data")
public class DictDataController {

    private final DictDataService dictDataService;

    public DictDataController(DictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    @GetMapping("/{dictType}")
    public ApiResponse<List<DictDataVO>> getByType(@PathVariable String dictType) {
        return ApiResponse.ok(dictDataService.listByDictType(dictType));
    }

    @GetMapping
    public ApiResponse<Map<String, List<DictDataVO>>> batch(@RequestParam String types) {
        List<String> dictTypes = Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        return ApiResponse.ok(dictDataService.listByDictTypes(dictTypes));
    }

    /**
     * Runtime: dict type keys for codegen / select fields (no dict admin permission).
     *
     * @param scope optional filter, e.g. {@code oa} = OA form field dict pool
     */
    @GetMapping("/types")
    public ApiResponse<List<DictTypeOptionVO>> listTypes(
            @RequestParam(value = "scope", required = false) String scope) {
        return ApiResponse.ok(dictDataService.listActiveDictTypes(scope));
    }
}
