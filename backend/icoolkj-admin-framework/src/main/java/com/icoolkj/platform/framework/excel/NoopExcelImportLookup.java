package com.icoolkj.platform.framework.excel;

import com.icoolkj.platform.common.exception.BusinessException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Fallback when system module is absent — no dropdowns; resolve fails closed
 * (never pass-through arbitrary dict/dept/post/role text).
 */
@Component
@ConditionalOnMissingBean(ExcelImportLookup.class)
public class NoopExcelImportLookup implements ExcelImportLookup {

    @Override
    public List<String> dictLabels(String dictType) {
        return List.of();
    }

    @Override
    public String resolveDictCode(String dictType, String rawLabelOrCode) {
        if (!StringUtils.hasText(rawLabelOrCode)) {
            return null;
        }
        throw new BusinessException(
                "error.excel.import_dict_invalid",
                dictType == null ? "" : dictType,
                rawLabelOrCode.trim());
    }

    @Override
    public List<String> deptNames() {
        return List.of();
    }

    @Override
    public String resolveDeptId(String nameOrId) {
        if (!StringUtils.hasText(nameOrId)) {
            return null;
        }
        throw new BusinessException("error.excel.import_dept_not_found", nameOrId.trim());
    }

    @Override
    public List<String> postNames() {
        return List.of();
    }

    @Override
    public String resolvePostId(String nameOrId) {
        if (!StringUtils.hasText(nameOrId)) {
            return null;
        }
        throw new BusinessException("error.excel.import_post_not_found", nameOrId.trim());
    }

    @Override
    public List<String> roleNames() {
        return List.of();
    }

    @Override
    public String resolveRoleId(String nameOrId) {
        if (!StringUtils.hasText(nameOrId)) {
            return null;
        }
        throw new BusinessException("error.excel.import_role_not_found", nameOrId.trim());
    }
}
