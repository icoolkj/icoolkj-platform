package com.icoolkj.platform.system.i18n.vo;

import lombok.Data;

import java.util.List;

/** 前端 i18n 启动引导 */
@Data
public class I18nBootstrapVO {

    private String defaultLocale;
    private List<String> supportedLocales;
    private List<I18nLocaleVO> locales;

}

