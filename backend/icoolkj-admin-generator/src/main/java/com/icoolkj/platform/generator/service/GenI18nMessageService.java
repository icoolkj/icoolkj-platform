package com.icoolkj.platform.generator.service;

import com.icoolkj.platform.generator.domain.GenI18nMessage;
import com.icoolkj.platform.generator.domain.GenTable;

import java.util.List;
import java.util.Map;

public interface GenI18nMessageService {

    List<GenI18nMessage> listByTableId(String tableId);

    GenI18nMessage getByTableIdAndLocale(String tableId, String locale);

    GenI18nMessage getRuntimeMessages(String tableId, String locale);

    GenI18nMessage getRuntimeMessagesByBusiness(String businessName, String apiModule, String locale);

    void save(GenI18nMessage row);

    /** Build from columns and upsert zh-CN + en-US rows. */
    void syncFromTable(GenTable table);

    void deleteByTableId(String tableId);

    /** Flat validation keys for backend MessageSource cache. */
    Map<String, String> getValidationMessageMap(String locale);

    /** Git baseline JSON from classpath (i18n/gen/{businessName}/{locale}.json). */
    String getGitBaselineJson(String businessName, String locale);

    /** Overwrite DB row with Git baseline for table/locale. */
    void restoreGitBaseline(String tableId, String businessName, String locale);
}
