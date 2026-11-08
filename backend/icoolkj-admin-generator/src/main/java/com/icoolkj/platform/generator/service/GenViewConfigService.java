package com.icoolkj.platform.generator.service;

import com.icoolkj.platform.generator.domain.GenViewConfig;
import com.icoolkj.platform.generator.domain.GenTable;

public interface GenViewConfigService {

    GenViewConfig getByTableId(String tableId);

    GenViewConfig getRuntimeByBusiness(String businessName, String apiModule);

    void save(GenViewConfig row);

    void syncFromTable(GenTable table);

    void deleteByTableId(String tableId);

    /** Git baseline JSON from classpath (i18n/gen/{businessName}/view.json). */
    String getGitBaselineJson(String businessName);

    /** Overwrite DB row with Git baseline. */
    void restoreGitBaseline(String tableId, String businessName);
}
