package com.icoolkj.platform.infra.mapperxml;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the {@code Query_Where} / {@code Page_Query_Where} split for infra
 * mappers: entity-backed {@code selectList} fragments must not reference
 * page-query-only properties (dataScopeSql, excelStagingOwnerUserId).
 */
class InfraMapperQueryWhereContractTest {

    private static String resource(String path) throws Exception {
        try (InputStream in = InfraMapperQueryWhereContractTest.class.getClassLoader().getResourceAsStream(path)) {
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
    void asyncTaskQueryWhereIsEntitySafe() throws Exception {
        String xml = resource("mapper/infra/AsyncTaskMapper.xml");

        assertFalse(fragment(xml, "Query_Where").contains("dataScopeSql"),
                "AsyncTaskMapper Query_Where must not reference dataScopeSql");
        assertTrue(fragment(xml, "Page_Query_Where").contains("dataScopeSql"));

        assertIncludesFragment(xml, "selectList", "Query_Where");
        assertIncludesFragment(xml, "selectPage", "Page_Query_Where");
        assertIncludesFragment(xml, "countByQuery", "Page_Query_Where");
    }

    @Test
    void infraFileQueryWhereIsEntitySafe() throws Exception {
        String xml = resource("mapper/infra/InfraFileMapper.xml");

        String queryWhere = fragment(xml, "Query_Where");
        assertFalse(queryWhere.contains("dataScopeSql"),
                "InfraFileMapper Query_Where must not reference dataScopeSql");
        assertFalse(queryWhere.contains("excelStagingOwnerUserId"),
                "InfraFileMapper Query_Where must not reference excelStagingOwnerUserId (page-only)");
        String pageWhere = fragment(xml, "Page_Query_Where");
        assertTrue(pageWhere.contains("dataScopeSql"));
        assertTrue(pageWhere.contains("excelStagingOwnerUserId"));

        assertIncludesFragment(xml, "selectList", "Query_Where");
        assertIncludesFragment(xml, "selectPage", "Page_Query_Where");
        assertIncludesFragment(xml, "countByQuery", "Page_Query_Where");
    }
}
