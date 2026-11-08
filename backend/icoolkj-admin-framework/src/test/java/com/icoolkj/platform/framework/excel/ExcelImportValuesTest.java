package com.icoolkj.platform.framework.excel;

import com.icoolkj.platform.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelImportValuesTest {

    @Test
    void resolveDictAcceptsLabelOrCode() {
        List<ExcelImportValues.LabelValue> items = List.of(
                new ExcelImportValues.LabelValue("男", "0"),
                new ExcelImportValues.LabelValue("女", "1"));
        assertEquals("0", ExcelImportValues.resolveDictCode("sys_gender", items, "男"));
        assertEquals("1", ExcelImportValues.resolveDictCode("sys_gender", items, "1"));
        assertEquals("0", ExcelImportValues.resolveDictCode("sys_gender", items, "男(0)"));
        assertThrows(BusinessException.class,
                () -> ExcelImportValues.resolveDictCode("sys_gender", items, "其它"));
    }

    @Test
    void buildDropdownOptionsFromAnnotations() throws Exception {
        List<Field> fields = ExcelImportSupport.listImportFields(SampleImportRow.class);
        ExcelImportLookup lookup = new ExcelImportLookup() {
            @Override
            public List<String> dictLabels(String dictType) {
                return "sys_gender".equals(dictType) ? List.of("男", "女") : List.of();
            }

            @Override
            public String resolveDictCode(String dictType, String rawLabelOrCode) {
                return rawLabelOrCode;
            }

            @Override
            public List<String> deptNames() {
                return List.of("总经办", "研发中心");
            }

            @Override
            public String resolveDeptId(String nameOrId) {
                return nameOrId;
            }

            @Override
            public List<String> postNames() {
                return List.of("工程师");
            }

            @Override
            public String resolvePostId(String nameOrId) {
                return nameOrId;
            }

            @Override
            public List<String> roleNames() {
                return List.of("管理员");
            }

            @Override
            public String resolveRoleId(String nameOrId) {
                return nameOrId;
            }
        };
        Map<Integer, List<String>> dropdowns = ExcelImportSupport.buildDropdownOptions(fields, lookup);
        assertEquals(List.of("总经办", "研发中心"), dropdowns.get(0));
        assertEquals(List.of("男", "女"), dropdowns.get(1));
        assertEquals(List.of("工程师"), dropdowns.get(2));
        assertEquals(List.of("管理员"), dropdowns.get(3));
        assertTrue(dropdowns.size() >= 4);
    }

    @Test
    void resolveNameOrIdAcceptsIdAndUniqueName() {
        List<Map.Entry<String, String>> entries = List.of(
                Map.entry("研发", "1"),
                Map.entry("市场", "2"),
                Map.entry("市场", "3"));
        assertEquals("1", ExcelImportValues.resolveNameOrId(
                "研发", entries, "nf", "amb"));
        assertEquals("1", ExcelImportValues.resolveNameOrId(
                "1", entries, "nf", "amb"));
        assertThrows(BusinessException.class, () -> ExcelImportValues.resolveNameOrId(
                "市场", entries, "nf", "amb"));
        assertThrows(BusinessException.class, () -> ExcelImportValues.resolveNameOrId(
                "未知", entries, "nf", "amb"));
    }

    public static class SampleImportRow {
        @com.alibaba.excel.annotation.ExcelProperty(index = 0)
        @ExcelI18nProperty("dept")
        @ExcelImportRef(ExcelImportRef.Kind.DEPT_NAME)
        private String deptName;

        @com.alibaba.excel.annotation.ExcelProperty(index = 1)
        @ExcelI18nProperty("gender")
        @ExcelImportDict("sys_gender")
        private String gender;

        @com.alibaba.excel.annotation.ExcelProperty(index = 2)
        @ExcelI18nProperty("post")
        @ExcelImportRef(ExcelImportRef.Kind.POST_NAME)
        private String postName;

        @com.alibaba.excel.annotation.ExcelProperty(index = 3)
        @ExcelI18nProperty("role")
        @ExcelImportRef(ExcelImportRef.Kind.ROLE_NAME)
        private String roleName;
    }
}
