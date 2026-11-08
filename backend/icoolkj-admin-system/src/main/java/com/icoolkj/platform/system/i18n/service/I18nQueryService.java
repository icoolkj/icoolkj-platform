package com.icoolkj.platform.system.i18n.service;

import com.icoolkj.platform.system.i18n.vo.I18nBootstrapVO;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface I18nQueryService {

    I18nBootstrapVO getBootstrap();

    Map<String, String> getMessageMap(String locale, String scope);

    String resolve(String code, Object[] args, Locale locale);

    List<String> getEnabledLocaleCodes();
}
