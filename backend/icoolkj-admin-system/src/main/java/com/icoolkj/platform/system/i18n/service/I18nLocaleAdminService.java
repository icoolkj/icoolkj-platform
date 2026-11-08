package com.icoolkj.platform.system.i18n.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.i18n.dto.I18nLocaleCreateRequest;
import com.icoolkj.platform.system.i18n.dto.I18nLocalePageQuery;
import com.icoolkj.platform.system.i18n.dto.I18nLocaleUpdateRequest;
import com.icoolkj.platform.system.i18n.vo.I18nLocaleVO;

public interface I18nLocaleAdminService {

    PageResult<I18nLocaleVO> page(I18nLocalePageQuery query);

    I18nLocaleVO getById(String id);

    String create(I18nLocaleCreateRequest request);

    void update(I18nLocaleUpdateRequest request);

    void deleteById(String id);
}
