package com.icoolkj.platform.generator.util;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.generator.constant.GenConstants;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenTableColumn;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies Gen Excel export/import templates render with zero hand-written rows for a sample table.
 */
class GenExcelExportGenSmokeTest {

    @BeforeAll
    static void initVelocity() {
        VelocityInitializer.initVelocity();
    }

    @Test
    void exportAndImportAreIndependentChannels() {
        GenTable table = sampleTable(true, true);
        assertTrue(VelocityUtils.hasExportColumns(table));
        assertTrue(VelocityUtils.hasImportColumns(table));
        List<String> templates = VelocityUtils.getTemplateList(table);
        assertTrue(templates.contains("vm/java/exportRow.java.vm"));
        assertTrue(templates.contains("vm/java/importRow.java.vm"));

        VelocityContext ctx = VelocityUtils.prepareContext(table);
        assertTrue(Boolean.TRUE.equals(ctx.get("hasExport")));
        assertTrue(Boolean.TRUE.equals(ctx.get("hasImport")));

        String exportRow = render("vm/java/exportRow.java.vm", ctx);
        assertTrue(exportRow.contains("class DemoExportRow"));
        assertTrue(exportRow.contains("@ExcelI18nProperty(\"gen.demo.name.label\")"));
        assertTrue(exportRow.contains("@ExcelMask(ExcelMask.Type.PHONE)"));

        String importRow = render("vm/java/importRow.java.vm", ctx);
        assertTrue(importRow.contains("@ExcelProperty(index = 0)"));
        assertTrue(importRow.contains("class DemoImportRow"));
        assertTrue(importRow.contains("@ExcelImportDict(\"sys_common_status\")"));
        assertFalse(importRow.contains("ExcelMask"));

        String controller = render("vm/java/controller.java.vm", ctx);
        assertTrue(controller.contains("@GetMapping(\"/export\")"));
        assertTrue(controller.contains("writeLocalizedPaged"));
        assertTrue(controller.contains("@PostMapping(\"/export-async\")"));
        assertTrue(controller.contains("excelExportAsyncSubmitFacade"));
        assertTrue(controller.contains("importRows"));
        assertTrue(controller.contains("@GetMapping(\"/import-template\")"));
        assertTrue(controller.contains("@PostMapping(\"/import-async\")"));
        assertTrue(controller.contains("excelImportAsyncSubmitFacade"));
        assertTrue(controller.contains("ExcelImportAsyncSubmitFacade"));
        assertTrue(controller.contains("ExcelImportResult"));
        assertFalse(controller.contains("syncBizFileRefs"));
        assertFalse(controller.contains("AsyncTaskRuntime"));

        assertTrue(templates.contains("vm/java/excelImportAsyncHandler.java.vm"));
        assertTrue(templates.contains("vm/java/excelExportAsyncHandler.java.vm"));
        String asyncHandler = render("vm/java/excelImportAsyncHandler.java.vm", ctx);
        assertTrue(asyncHandler.contains("excel.import.demo"));
        assertTrue(asyncHandler.contains("DemoExcelImportAsyncHandler"));
        assertTrue(asyncHandler.contains("AbstractExcelImportAsyncHandler"));
        assertTrue(asyncHandler.contains("importRows"));
        String exportHandler = render("vm/java/excelExportAsyncHandler.java.vm", ctx);
        assertTrue(exportHandler.contains("excel.export.demo"));
        assertTrue(exportHandler.contains("AbstractExcelExportAsyncHandler"));

        String service = render("vm/java/service.java.vm", ctx);
        assertTrue(service.contains("ExcelImportResult importRows"));

        String serviceImpl = render("vm/java/serviceImpl.java.vm", ctx);
        assertTrue(serviceImpl.contains("importRows"));
        assertTrue(serviceImpl.contains("toCreateRequest"));
        assertTrue(serviceImpl.contains("firstBeanValidationMessage"));
        assertTrue(serviceImpl.contains("ExcelImportI18n.encodeViolation"));
        assertTrue(serviceImpl.contains("validator.validate"));
        assertTrue(serviceImpl.contains("isBlankImportRow"));
        assertTrue(serviceImpl.contains("validateImportRequest"));
        assertTrue(serviceImpl.contains("validateImportDomain"));
        assertTrue(serviceImpl.contains("checkImportUniqueness"));
        assertTrue(serviceImpl.contains("countByCode"));
        assertTrue(serviceImpl.contains("error.excel.import_already_exists"));
        assertTrue(serviceImpl.contains("create(item.request())"));
        assertFalse(serviceImpl.contains("error.excel.import_not_implemented"));
        assertTrue(serviceImpl.contains("setRollbackOnly"));

        String mapperJava = render("vm/java/mapper.java.vm", ctx);
        assertTrue(mapperJava.contains("countByCode"));
        String mapperXml = render("vm/xml/mapper.xml.vm", ctx);
        assertTrue(mapperXml.contains("countByCode"));
        assertTrue(mapperXml.contains("demo_code"));

        String apiTs = render("vm/ts/api.ts.vm", ctx);
        assertTrue(apiTs.contains("importDemoAsync"));
        assertTrue(apiTs.contains("exportDemoAsync"));
        assertTrue(apiTs.contains("export function importDemo("));
        assertTrue(apiTs.contains("/import-async"));
        assertTrue(apiTs.contains("/export-async"));
        assertTrue(apiTs.contains("${BASE}/import`"));
        assertTrue(apiTs.contains("importFileAsync"));
        assertTrue(apiTs.contains("exportFileAsync"));
        assertTrue(apiTs.contains("genCrudResource"));

        String indexVue = render("vm/vue/v3/index.vue.vm", ctx);
        assertTrue(indexVue.contains("GenRuntimePage"));
        assertTrue(indexVue.contains("page-key=\"demo\""));
        assertTrue(indexVue.contains("@/components/gen"));
        assertFalse(indexVue.contains("importDemoAsync"));
        assertFalse(indexVue.contains("GenExcelToolbarActions"));

        String viewJson = GenViewBuilder.buildJson(table, table.getColumns());
        assertTrue(viewJson.contains("toolbarActions"), viewJson);
        assertTrue(viewJson.contains("\"builtin\": \"export\""), viewJson);
        assertTrue(viewJson.contains("\"builtin\": \"import\""), viewJson);
        assertFalse(viewJson.contains("importTemplate"), viewJson);
    }

    @Test
    void exportOnlyDoesNotEmitImportToolbar() {
        GenTable table = sampleTable(true, false);
        assertTrue(VelocityUtils.hasExportColumns(table));
        assertFalse(VelocityUtils.hasImportColumns(table));
        assertFalse(VelocityUtils.getTemplateList(table).contains("vm/java/importRow.java.vm"));

        String viewJson = GenViewBuilder.buildJson(table, table.getColumns());
        assertTrue(viewJson.contains("\"builtin\": \"export\""), viewJson);
        assertFalse(viewJson.contains("\"builtin\": \"import\""), viewJson);
    }

    @Test
    void importDefaultsOffUnlessChecked() {
        GenTable table = sampleTable(true, false);
        assertFalse(VelocityUtils.hasImportColumns(table));
        String controller = render("vm/java/controller.java.vm", VelocityUtils.prepareContext(table));
        assertTrue(controller.contains("@GetMapping(\"/export\")"));
        assertFalse(controller.contains("import-template"));
    }
    @Test
    void noExcelWhenAllFlagsOff() {
        GenTable table = sampleTable(false, false);
        assertFalse(VelocityUtils.hasExportColumns(table));
        assertFalse(VelocityUtils.hasImportColumns(table));
        String viewJson = GenViewBuilder.buildJson(table, table.getColumns());
        assertFalse(viewJson.contains("toolbarActions"));
    }

    private static String render(String template, VelocityContext ctx) {
        Template tpl = Velocity.getTemplate(template, StandardCharsets.UTF_8.name());
        StringWriter writer = new StringWriter();
        tpl.merge(ctx, writer);
        return writer.toString();
    }

    private static GenTable sampleTable(boolean export, boolean importFlag) {
        GenTable table = new GenTable();
        table.setTableName("biz_demo");
        table.setClassName("Demo");
        table.setBusinessName("demo");
        table.setFunctionName("演示");
        table.setFunctionAuthor("test");
        table.setBackendModule("biz");
        table.setApiModule("biz");
        table.setBaseType(GenConstants.BASE_TENANT);
        table.setTplCategory(GenConstants.TPL_CRUD);
        table.setTplWebType(GenConstants.WEB_ELEMENT_PLUS_TS);
        table.setPermissionPrefix("biz:demo");
        table.setRoutePrefix("/biz");

        GenTableColumn code = col("demo_code", "code", "String", export, importFlag);
        code.setIsRequired(GenConstants.REQUIRE);
        GenTableColumn name = col("name", "name", "String", export, importFlag);
        GenTableColumn phone = col("phone", "phone", "String", export, importFlag);
        GenTableColumn status = col("status", "status", "String", export, importFlag);
        status.setDictType("sys_common_status");
        GenTableColumn skip = col("avatar", "avatar", "String", false, false);
        table.setColumns(List.of(code, name, phone, status, skip));
        return table;
    }

    private static GenTableColumn col(String columnName, String javaField, String javaType,
                                      boolean export, boolean importFlag) {
        GenTableColumn c = new GenTableColumn();
        c.setColumnName(columnName);
        c.setJavaField(javaField);
        c.setJavaType(javaType);
        c.setIsPk(CommonConstants.NO);
        c.setIsList(export ? GenConstants.REQUIRE : CommonConstants.NO);
        c.setIsExport(export ? GenConstants.REQUIRE : CommonConstants.NO);
        c.setIsImport(importFlag ? GenConstants.REQUIRE : CommonConstants.NO);
        c.setIsInsert(importFlag ? GenConstants.REQUIRE : CommonConstants.NO);
        c.setIsEdit(GenConstants.REQUIRE);
        c.setIsQuery(CommonConstants.NO);
        return c;
    }
}
