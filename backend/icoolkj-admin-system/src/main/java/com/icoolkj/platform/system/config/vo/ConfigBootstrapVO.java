package com.icoolkj.platform.system.config.vo;

import lombok.Data;

import java.util.List;
import com.icoolkj.platform.system.brandpack.vo.BrandPackVO;
import com.icoolkj.platform.system.i18n.vo.I18nLocaleVO;

/** 前端启动引导配置（语言等） */
@Data
public class ConfigBootstrapVO {

    private String defaultLocale;
    private List<String> supportedLocales;
    private List<I18nLocaleVO> locales;
    /** True when icoolkj.demo.enabled — UI should hide write actions. */
    private boolean demoMode;
    /** OEM / 站点品牌（sys_brand_pack）；空则前端用 shell.config 兜底 */
    private BrandPackVO brand;
}
