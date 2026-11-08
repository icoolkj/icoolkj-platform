package com.icoolkj.platform.system.dict.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.dict.dto.DictItemCreateRequest;
import com.icoolkj.platform.system.dict.dto.DictItemPageQuery;
import com.icoolkj.platform.system.dict.dto.DictItemUpdateRequest;
import com.icoolkj.platform.system.dict.vo.DictItemVO;

/** 字典数据项表服务 */
public interface DictItemService {

    PageResult<DictItemVO> page(DictItemPageQuery query);

    DictItemVO getById(String id);

    String create(DictItemCreateRequest request);

    void update(DictItemUpdateRequest request);

    void deleteById(String id);
}
