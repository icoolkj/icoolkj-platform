package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.constant.I18nScopeConstants;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.system.i18n.service.I18nQueryService;
import com.icoolkj.platform.system.i18n.vo.I18nBootstrapVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 国际化公共接口（前端启动加载） */
@RestController
@RequestMapping("/system/i18n")
public class I18nController {

    private final I18nQueryService i18nQueryService;

    public I18nController(I18nQueryService i18nQueryService) {
        this.i18nQueryService = i18nQueryService;
    }

    @GetMapping("/bootstrap")
    public ApiResponse<I18nBootstrapVO> bootstrap() {
        return ApiResponse.ok(i18nQueryService.getBootstrap());
    }

    @GetMapping("/messages")
    public ApiResponse<Map<String, String>> messages(
            @RequestParam(required = false) String locale,
            @RequestParam(defaultValue = I18nScopeConstants.FRONTEND) String scope) {
        String resolved = LocaleResolver.normalize(locale);
        return ApiResponse.ok(i18nQueryService.getMessageMap(resolved, scope));
    }
}
