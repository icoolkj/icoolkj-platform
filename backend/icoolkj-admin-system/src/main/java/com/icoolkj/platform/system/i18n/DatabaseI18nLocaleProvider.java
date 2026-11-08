package com.icoolkj.platform.system.i18n;

import com.icoolkj.platform.common.i18n.I18nLocaleProvider;
import com.icoolkj.platform.system.i18n.service.I18nQueryService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseI18nLocaleProvider implements I18nLocaleProvider {

    private final I18nQueryService i18nQueryService;

    public DatabaseI18nLocaleProvider(I18nQueryService i18nQueryService) {
        this.i18nQueryService = i18nQueryService;
    }

    @Override
    public List<String> getEnabledLocaleCodes() {
        return i18nQueryService.getEnabledLocaleCodes();
    }
}
