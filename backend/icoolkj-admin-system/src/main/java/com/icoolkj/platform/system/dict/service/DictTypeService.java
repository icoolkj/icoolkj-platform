package com.icoolkj.platform.system.dict.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.dict.dto.DictTypeCreateRequest;
import com.icoolkj.platform.system.dict.dto.DictTypePageQuery;
import com.icoolkj.platform.system.dict.dto.DictTypeUpdateRequest;
import com.icoolkj.platform.system.dict.vo.DictTypeVO;

/** 字典类型表服务 */
public interface DictTypeService {

    PageResult<DictTypeVO> page(DictTypePageQuery query);

    DictTypeVO getById(String id);

    String create(DictTypeCreateRequest request);

    void update(DictTypeUpdateRequest request);

    void deleteById(String id);
}
