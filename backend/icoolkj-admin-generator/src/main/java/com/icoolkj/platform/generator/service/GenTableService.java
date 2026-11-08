package com.icoolkj.platform.generator.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.dto.GenTablePageQuery;

import java.util.List;
import java.util.Map;

public interface GenTableService {

    PageResult<GenTable> page(GenTablePageQuery query);

    List<GenTable> selectGenTableList(GenTable query);

    GenTable selectGenTableById(String id);

    List<GenTable> selectDbTableList(GenTable query);

    PageResult<GenTable> pageDbTable(GenTablePageQuery query);

    void importTables(String[] tableNames, String operator);

    void updateGenTable(GenTable genTable);

    void deleteGenTableByIds(String[] ids);

    Map<String, String> previewCode(String id);

    byte[] downloadCode(String tableName);

    byte[] downloadCodeBatch(String[] tableNames);

    void generateCode(String tableName);

    void syncDb(String tableName);
}
