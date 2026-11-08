package com.icoolkj.platform.system.i18n.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.i18n.dto.I18nMessageCreateRequest;
import com.icoolkj.platform.system.i18n.dto.I18nMessagePageQuery;
import com.icoolkj.platform.system.i18n.dto.I18nMessageUpdateRequest;
import com.icoolkj.platform.system.i18n.vo.I18nMessageVO;

public interface I18nMessageAdminService {

    PageResult<I18nMessageVO> page(I18nMessagePageQuery query);

    I18nMessageVO getById(String id);

    String create(I18nMessageCreateRequest request);

    void update(I18nMessageUpdateRequest request);

    void deleteById(String id);

    void refreshCache();
}
