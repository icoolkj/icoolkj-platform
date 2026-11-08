package com.icoolkj.platform.system.i18n;

import com.icoolkj.platform.system.i18n.service.I18nCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Clears Redis i18n cache on startup so Flyway-seeded message keys are visible immediately.
 */
@Component
public class I18nCacheStartupRefresher {

    private static final Logger log = LoggerFactory.getLogger(I18nCacheStartupRefresher.class);

    private final I18nCacheService i18nCacheService;

    public I18nCacheStartupRefresher(I18nCacheService i18nCacheService) {
        this.i18nCacheService = i18nCacheService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        i18nCacheService.refreshAllMessages();
        i18nCacheService.evictEnabledLocales();
        log.debug("I18n Redis cache cleared on startup");
    }
}
