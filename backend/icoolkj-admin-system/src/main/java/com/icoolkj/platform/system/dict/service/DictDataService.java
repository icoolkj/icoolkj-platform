package com.icoolkj.platform.system.dict.service;

import com.icoolkj.platform.system.dict.vo.DictDataVO;
import com.icoolkj.platform.system.dict.vo.DictTypeOptionVO;

import java.util.List;
import java.util.Map;

/** Runtime dictionary data for generated CRUD pages and form controls. */
public interface DictDataService {

    List<DictDataVO> listByDictType(String dictType);

    Map<String, List<DictDataVO>> listByDictTypes(List<String> dictTypes);

    /** Active dict types visible to current tenant (tenant + platform). */
    List<DictTypeOptionVO> listActiveDictTypes();

    /**
     * Active dict types with optional scope filter (e.g. {@code oa} for OA form field binding).
     *
     * @param scope blank = all active types
     */
    List<DictTypeOptionVO> listActiveDictTypes(String scope);
}
