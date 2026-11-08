package com.icoolkj.platform.system.mapperxml;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the {@code Query_Where} / {@code Page_Query_Where} split contract.
 *
 * <p>Entity-backed statements such as {@code selectList(DeptEntity)} reuse the
 * {@code Query_Where} fragment, so that fragment may only reference properties
 * the entity actually exposes. Page-only properties ({@code dataScopeSql},
 * {@code configKeyPrefix}, ...) live in {@code Page_Query_Where}, which is used
 * by {@code selectPage} / {@code countByQuery} with a {@code PageQuery} DTO.
 */
class MapperQueryWhereContractTest {

    private static String resource(String path) throws Exception {
        try (InputStream in = MapperQueryWhereContractTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(in, "missing classpath resource: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String fragment(String xml, String id) {
        int start = xml.indexOf("<sql id=\"" + id + "\">");
        assertTrue(start >= 0, "missing <sql id=\"" + id + "\"> in mapper xml");
        int end = xml.indexOf("</sql>", start);
        return xml.substring(start, end);
    }

    private static void assertIncludesFragment(String xml, String statementId, String fragmentId) {
        assertTrue(
                xml.matches("(?s).*<select id=\"" + statementId + "\"[^>]*>.*<include refid=\""
                        + fragmentId + "\"/>.*"),
                statementId + " should include " + fragmentId);
    }

    @Test
    void deptQueryWhereIsEntitySafeAndDataScopeMovedToPageFragment() throws Exception {
        String xml = resource("mapper/system/dept/DeptMapper.xml");

        String queryWhere = fragment(xml, "Query_Where");
        assertFalse(queryWhere.contains("dataScopeSql"),
                "DeptMapper Query_Where must not reference dataScopeSql: DeptEntity has no such getter");
        assertTrue(fragment(xml, "Page_Query_Where").contains("dataScopeSql"),
                "DeptMapper Page_Query_Where must own dataScopeSql");

        assertIncludesFragment(xml, "selectList", "Query_Where");
        assertIncludesFragment(xml, "selectPage", "Page_Query_Where");
        assertIncludesFragment(xml, "countByQuery", "Page_Query_Where");
    }

    @Test
    void configQueryWhereOnlyReferencesEntityOwnedProperties() throws Exception {
        String xml = resource("mapper/system/config/ConfigMapper.xml");

        String queryWhere = fragment(xml, "Query_Where");
        assertFalse(queryWhere.contains("configKeyPrefix"),
                "ConfigMapper Query_Where must not reference configKeyPrefix (page-only)");
        assertFalse(queryWhere.contains("configKeyExcludePrefixList"),
                "ConfigMapper Query_Where must not reference configKeyExcludePrefixList (page-only)");
        String pageWhere = fragment(xml, "Page_Query_Where");
        assertTrue(pageWhere.contains("configKeyPrefix"));
        assertTrue(pageWhere.contains("configKeyExcludePrefixList"));

        assertIncludesFragment(xml, "selectList", "Query_Where");
        assertIncludesFragment(xml, "selectPage", "Page_Query_Where");
        assertIncludesFragment(xml, "countByQuery", "Page_Query_Where");
    }
}
