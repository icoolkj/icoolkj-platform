package com.icoolkj.platform.generator.util;

import com.icoolkj.platform.generator.constant.GenConstants;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenTableColumn;
import com.icoolkj.platform.generator.vo.GenPageSpecDraftVO;
import com.icoolkj.platform.generator.vo.GenPageSpecEntityDraftVO;
import com.icoolkj.platform.generator.vo.GenPageSpecFieldDraftVO;
import com.icoolkj.platform.generator.vo.GenPageSpecMetaDraftVO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** 从 gen_table 列元数据推导初始页面方案（无需 AI） */
public final class PageSpecDraftBuilder {

    private PageSpecDraftBuilder() {
    }

    public static GenPageSpecDraftVO fromTable(GenTable table, String pageKeyOverride, String layoutOverride) {
        GenPageSpecDraftVO draft = new GenPageSpecDraftVO();
        String pageKey = StringUtils.defaultIfBlank(pageKeyOverride, table.getBusinessName());
        draft.setPageKey(pageKey);
        draft.setLayout(StringUtils.defaultIfBlank(layoutOverride, "standard-crud"));
        draft.setSummary(null);

        GenPageSpecMetaDraftVO meta = new GenPageSpecMetaDraftVO();
        meta.setApiModule(table.getApiModule());
        meta.setBackendModule(table.getBackendModule());
        meta.setPermissionPrefix(StringUtils.defaultIfBlank(table.getPermissionPrefix(),
                table.getApiModule() + ":" + pageKey));
        meta.setClassName(table.getClassName());
        draft.setMeta(meta);

        GenPageSpecEntityDraftVO entity = new GenPageSpecEntityDraftVO();
        entity.setTableName(table.getTableName());
        entity.setBusinessName(pageKey);
        entity.setFunctionName(StringUtils.defaultIfBlank(table.getFunctionName(), table.getTableComment()));
        draft.setEntity(entity);

        List<GenPageSpecFieldDraftVO> fields = new ArrayList<>();
        if (table.getColumns() != null) {
            for (GenTableColumn col : table.getColumns()) {
                if (table.isSuperColumn(col.getJavaField())) {
                    continue;
                }
                if (!col.isList() && !col.isInsert() && !col.isEdit() && !col.isQuery() && !col.isExport() && !col.isImport()) {
                    continue;
                }
                GenPageSpecFieldDraftVO f = new GenPageSpecFieldDraftVO();
                f.setJavaField(col.getJavaField());
                f.setColumnComment(col.getColumnComment());
                f.setIsQuery(col.isQuery());
                f.setQueryType(StringUtils.defaultIfBlank(col.getQueryType(), GenConstants.QUERY_EQ));
                f.setIsList(col.isList());
                f.setIsExport(col.isExport());
                f.setIsImport(col.isImport());
                f.setIsInsert(col.isInsert());
                f.setIsEdit(col.isEdit());
                f.setHtmlType(mapHtmlType(col.getHtmlType()));
                f.setDictType(col.getDictType());
                fields.add(f);
            }
        }
        draft.setFields(fields);
        return draft;
    }

    private static String mapHtmlType(String htmlType) {
        if (StringUtils.isBlank(htmlType)) {
            return "input";
        }
        return switch (htmlType) {
            case GenConstants.HTML_TEXTAREA -> "textarea";
            case GenConstants.HTML_SELECT -> "select";
            case GenConstants.HTML_RADIO -> "radio";
            case GenConstants.HTML_DATETIME -> "datetime";
            case GenConstants.HTML_TREE -> "tree-select";
            default -> "input";
        };
    }
}
