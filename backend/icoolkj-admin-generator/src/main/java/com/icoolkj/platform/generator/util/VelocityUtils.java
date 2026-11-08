package com.icoolkj.platform.generator.util;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.common.constant.MenuAssignScope;
import com.icoolkj.platform.common.constant.ProductCodes;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.generator.config.GenConfig;
import com.icoolkj.platform.generator.constant.GenConstants;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenTableColumn;
import com.icoolkj.platform.generator.util.GenTableOptionsHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class VelocityUtils {

    /** Max search fields shown on generated list page (avoid cluttered filter bar). */
    public static final int MAX_QUERY_COLUMNS = 4;

    private VelocityUtils() {
    }

    public static VelocityContext prepareContext(GenTable table) {
        VelocityContext ctx = new VelocityContext();
        String className = table.getClassName();
        ctx.put("tableName", table.getTableName());
        ctx.put("ClassName", className);
        ctx.put("className", String.valueOf(className.charAt(0)).toLowerCase() + className.substring(1));
        ctx.put("EntityName", table.getEntityName());
        ctx.put("functionName", table.getFunctionName());
        ctx.put("functionNameEn", resolveFunctionNameEn(table));
        ctx.put("author", table.getFunctionAuthor());
        ctx.put("datetime", LocalDate.now().toString());
        ctx.put("packageName", table.getPackageName());
        ctx.put("backendModule", table.getBackendModule());
        ctx.put("apiModule", table.getApiModule());
        ctx.put("baseType", table.getBaseType());
        ctx.put("businessName", table.getBusinessName());
        ctx.put("BusinessName", capitalize(table.getBusinessName()));
        ctx.put("routePath", table.getRoutePath());
        ctx.put("permissionPrefix", table.getPermissionPrefix());
        ctx.put("permAlias", resolvePermAlias(table));
        ctx.put("hasDataScope", hasDataScope(table.getColumns()));
        ctx.put("dataScopeDeptColumn", resolveDataScopeDeptColumn(table.getColumns()));
        ctx.put("dataScopeUserColumn", resolveDataScopeUserColumn(table.getColumns()));
        ctx.put("hasUserIdColumn", hasColumn(table.getColumns(), "user_id", "userId"));
        ctx.put("hasDeptIdColumn", hasColumn(table.getColumns(), "dept_id", "deptId"));
        ctx.put("hasDeletedColumn", hasColumn(table.getColumns(), "deleted"));
        ctx.put("orderByColumn", resolveOrderByColumn(table.getColumns()));
        ctx.put("componentPath", "/" + table.getApiModule() + "/" + table.getBusinessName() + "/index");
        String productCode = GenTableOptionsHelper.getProductCode(table);
        String assignScope = GenTableOptionsHelper.getAssignScope(table);
        String parentMenuId = resolveParentMenuId(table, productCode);
        ctx.put("productCode", productCode);
        ctx.put("assignScope", assignScope);
        ctx.put("grantTenantSuperRole", MenuAssignScope.isTenant(assignScope));
        ctx.put("parentMenuId", parentMenuId);
        ctx.put("menuPageId", IdGeneratorUtils.nextIdStr());
        ctx.put("menuBtnListId", IdGeneratorUtils.nextIdStr());
        ctx.put("menuBtnAddId", IdGeneratorUtils.nextIdStr());
        ctx.put("menuBtnUpdateId", IdGeneratorUtils.nextIdStr());
        ctx.put("menuBtnDeleteId", IdGeneratorUtils.nextIdStr());
        ctx.put("menuBtnExportId", IdGeneratorUtils.nextIdStr());
        ctx.put("menuBtnImportId", IdGeneratorUtils.nextIdStr());
        ctx.put("pkColumn", table.getPkColumn());
        ctx.put("columns", table.getColumns());
        List<GenTableColumn> exportColumns = getExportColumns(table);
        List<GenTableColumn> importColumns = getImportColumns(table);
        List<GenTableColumn> importUniqueColumns = getImportUniqueColumns(table);
        ctx.put("exportColumns", exportColumns);
        ctx.put("importColumns", importColumns);
        ctx.put("importUniqueColumns", importUniqueColumns);
        ctx.put("hasExport", !exportColumns.isEmpty());
        ctx.put("hasImport", !importColumns.isEmpty());
        ctx.put("hasImportUnique", !importUniqueColumns.isEmpty());
        ctx.put("hasExcel", !exportColumns.isEmpty() || !importColumns.isEmpty());
        ctx.put("queryColumns", getQueryColumns(table.getColumns(), MAX_QUERY_COLUMNS));
        ctx.put("dictTypes", collectDictTypes(table.getColumns()));
        ctx.put("table", table);
        ctx.put("tplCategory", table.getTplCategory());
        ctx.put("tree", GenConstants.TPL_TREE.equals(table.getTplCategory()));
        ctx.put("tableId", table.getId());
        ctx.put("importList", getImportList(table));
        return ctx;
    }

    /** Columns with {@code isExport=1} (Excel allowlist). */
    public static List<GenTableColumn> getExportColumns(GenTable table) {
        List<GenTableColumn> result = new ArrayList<>();
        if (table == null || table.getColumns() == null) {
            return result;
        }
        for (GenTableColumn column : table.getColumns()) {
            if (!column.isExport() || column.isPk()) {
                continue;
            }
            result.add(column);
        }
        return result;
    }

    public static boolean hasExportColumns(GenTable table) {
        return !getExportColumns(table).isEmpty();
    }

    /** Columns with {@code isImport=1} (Excel import allowlist; separate from export). */
    public static List<GenTableColumn> getImportColumns(GenTable table) {
        List<GenTableColumn> result = new ArrayList<>();
        if (table == null || table.getColumns() == null) {
            return result;
        }
        for (GenTableColumn column : table.getColumns()) {
            if (!column.isImport() || column.isPk()) {
                continue;
            }
            result.add(column);
        }
        return result;
    }

    public static boolean hasImportColumns(GenTable table) {
        return !getImportColumns(table).isEmpty();
    }

    /**
     * Import columns that participate in in-file uniqueness checks (required String business keys).
     * Matches username/code/account and *Code/*No/*Key — aligned with User gold sample intent.
     */
    public static List<GenTableColumn> getImportUniqueColumns(GenTable table) {
        List<GenTableColumn> result = new ArrayList<>();
        for (GenTableColumn column : getImportColumns(table)) {
            if (!column.isRequired()) {
                continue;
            }
            if (!"String".equals(column.getJavaType())) {
                continue;
            }
            String field = column.getJavaField();
            if (field == null || field.isBlank()) {
                continue;
            }
            String lower = field.toLowerCase(java.util.Locale.ROOT);
            if ("username".equals(lower)
                    || "code".equals(lower)
                    || "account".equals(lower)
                    || lower.endsWith("code")
                    || lower.endsWith("no")
                    || lower.endsWith("key")) {
                result.add(column);
            }
        }
        return result;
    }

    public static List<GenTableColumn> getQueryColumns(List<GenTableColumn> columns, int max) {
        List<GenTableColumn> result = new ArrayList<>();
        if (columns == null) {
            return result;
        }
        for (GenTableColumn column : columns) {
            if (column.isQuery()) {
                result.add(column);
                if (result.size() >= max) {
                    break;
                }
            }
        }
        return result;
    }

    /** Unique dict type keys referenced by select/radio columns. */
    public static List<String> collectDictTypes(List<GenTableColumn> columns) {
        Set<String> types = new LinkedHashSet<>();
        if (columns == null) {
            return new ArrayList<>();
        }
        for (GenTableColumn column : columns) {
            if (StringUtils.isBlank(column.getDictType())) {
                continue;
            }
            String htmlType = column.getHtmlType();
            if (GenConstants.HTML_SELECT.equals(htmlType) || GenConstants.HTML_RADIO.equals(htmlType)) {
                types.add(column.getDictType().trim());
            }
        }
        return new ArrayList<>(types);
    }

    /** Project-relative path for preview / zip / copy-paste. */
    public static String getRelativeOutputPath(String template, GenTable table) {
        String full = getFileName(template, table).replace('\\', '/');
        String root = resolveProjectRoot(table).replace('\\', '/');
        if (full.startsWith(root + "/")) {
            return full.substring(root.length() + 1);
        }
        if (full.startsWith(root)) {
            return full.substring(root.length());
        }
        return full;
    }

    public static String buildOutputPathManifest(GenTable table, List<String> templates) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 代码生成输出路径 — ").append(table.getTableName()).append("\n\n");
        sb.append("> 表名 `").append(table.getTableName()).append("`，模块 `")
                .append(table.getBackendModule()).append("`，前端模块 `")
                .append(table.getApiModule()).append("/").append(table.getBusinessName()).append("`\n\n");
        sb.append("| 类型 | 相对路径（复制到项目根目录） |\n");
        sb.append("|------|------------------------------|\n");
        for (String template : templates) {
            sb.append("| ").append(describeTemplate(template)).append(" | `")
                    .append(getRelativeOutputPath(template, table)).append("` |\n");
        }
        sb.append("| Gen i18n zh-CN (frontend) | `").append(getGenI18nLocaleRelativePath(table, LocaleResolver.LOCALE_ZH_CN)).append("` |\n");
        sb.append("| Gen i18n en-US (frontend) | `").append(getGenI18nLocaleRelativePath(table, LocaleResolver.LOCALE_EN_US)).append("` |\n");
        sb.append("| Gen i18n zh-CN (backend) | `").append(getGenI18nBackendLocaleRelativePath(table, LocaleResolver.LOCALE_ZH_CN)).append("` |\n");
        sb.append("| Gen i18n en-US (backend) | `").append(getGenI18nBackendLocaleRelativePath(table, LocaleResolver.LOCALE_EN_US)).append("` |\n");
        sb.append("| Gen view (frontend) | `").append(getGenViewRelativePath(table)).append("` |\n");
        sb.append("| Gen view (backend) | `").append(getGenViewBackendRelativePath(table)).append("` |\n");
        sb.append("\n## 配置说明\n\n");
        sb.append("- **backendModule**：Java 业务模块（system / infra / biz）\n");
        sb.append("- **apiModule**：Controller 包名 + 前端 api/types/views 目录\n");
        sb.append("- **businessName**：前端文件名与路由目录（camelCase 如 `ipAccessRule`）\n");
        sb.append("- **packageName**：Java 包路径，默认 `com.icoolkj.platform.{backendModule}`\n");
        return sb.toString();
    }

    private static String describeTemplate(String template) {
        if (template.contains("entity.java")) return "Entity";
        if (template.contains("mapper.java")) return "Mapper";
        if (template.contains("mapper.xml")) return "Mapper XML";
        if (template.contains("pageQuery.java")) return "PageQuery DTO";
        if (template.contains("createRequest.java")) return "CreateRequest DTO";
        if (template.contains("updateRequest.java")) return "UpdateRequest DTO";
        if (template.contains("vo.java")) return "VO";
        if (template.contains("convert.java")) return "Convert";
        if (template.contains("service.java") && !template.contains("serviceImpl")) return "Service";
        if (template.contains("serviceImpl.java")) return "ServiceImpl";
        if (template.contains("controller.java")) return "Controller";
        if (template.contains("exportRow.java")) return "ExportRow";
        if (template.contains("importRow.java")) return "ImportRow";
        if (template.contains("excelImportAsyncHandler.java")) return "ExcelImportAsyncHandler";
        if (template.contains("excelExportAsyncHandler.java")) return "ExcelExportAsyncHandler";
        if (template.contains("menu.sql")) return "Menu SQL";
        if (template.contains("api.ts")) return "Frontend API";
        if (template.contains("types.ts")) return "Frontend Types";
        if (template.contains("index.vue")) return "Frontend Page";
        return template;
    }

    public static List<String> getTemplateList(GenTable table) {
        boolean withExport = hasExportColumns(table);
        boolean withImport = hasImportColumns(table);
        return getTemplateList(table.getTplCategory(), table.getTplWebType(), withExport, withImport);
    }

    public static List<String> getTemplateList(String tplCategory, String tplWebType) {
        return getTemplateList(tplCategory, tplWebType, false, false);
    }

    public static List<String> getTemplateList(String tplCategory, String tplWebType, boolean withExcel) {
        return getTemplateList(tplCategory, tplWebType, withExcel, withExcel);
    }

    public static List<String> getTemplateList(String tplCategory, String tplWebType,
                                               boolean withExport, boolean withImport) {
        List<String> templates = new ArrayList<>();
        templates.add("vm/java/entity.java.vm");
        templates.add("vm/java/mapper.java.vm");
        templates.add("vm/xml/mapper.xml.vm");
        templates.add("vm/java/pageQuery.java.vm");
        templates.add("vm/java/createRequest.java.vm");
        templates.add("vm/java/updateRequest.java.vm");
        templates.add("vm/java/vo.java.vm");
        templates.add("vm/java/convert.java.vm");
        templates.add("vm/java/service.java.vm");
        templates.add("vm/java/serviceImpl.java.vm");
        templates.add("vm/java/controller.java.vm");
        if (withExport) {
            templates.add("vm/java/exportRow.java.vm");
            templates.add("vm/java/excelExportAsyncHandler.java.vm");
        }
        if (withImport) {
            templates.add("vm/java/importRow.java.vm");
            templates.add("vm/java/excelImportAsyncHandler.java.vm");
        }
        templates.add("vm/sql/menu.sql.vm");
        if (GenConstants.WEB_ELEMENT_PLUS_TS.equals(tplWebType)) {
            templates.add("vm/ts/types.ts.vm");
            templates.add("vm/ts/api.ts.vm");
            templates.add("vm/vue/v3/index.vue.vm");
        }
        return templates;
    }

    public static String getFileName(String template, GenTable table) {
        String projectRoot = resolveProjectRoot(table);
        String pkgPath = table.getPackageName().replace('.', File.separatorChar);
        String module = table.getBackendModule();
        String className = table.getClassName();
        String businessName = table.getBusinessName();

        if (template.contains("entity.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/domain/" + table.getEntityName() + ".java";
        }
        if (template.contains("mapper.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/mapper/" + className + "Mapper.java";
        }
        if (template.contains("mapper.xml.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/resources/mapper/" + module + "/" + className + "Mapper.xml";
        }
        if (template.contains("pageQuery.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/dto/" + className + "PageQuery.java";
        }
        if (template.contains("createRequest.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/dto/" + className + "CreateRequest.java";
        }
        if (template.contains("updateRequest.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/dto/" + className + "UpdateRequest.java";
        }
        if (template.contains("vo.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/vo/" + className + "VO.java";
        }
        if (template.contains("convert.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/convert/" + className + "Convert.java";
        }
        if (template.contains("service.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/service/" + className + "Service.java";
        }
        if (template.contains("serviceImpl.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/service/impl/" + className + "ServiceImpl.java";
        }
        if (template.contains("exportRow.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/excel/" + className + "ExportRow.java";
        }
        if (template.contains("importRow.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/excel/" + className + "ImportRow.java";
        }
        if (template.contains("excelImportAsyncHandler.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/excel/" + className + "ExcelImportAsyncHandler.java";
        }
        if (template.contains("excelExportAsyncHandler.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-" + module + "/src/main/java/" + pkgPath + "/excel/" + className + "ExcelExportAsyncHandler.java";
        }
        if (template.contains("controller.java.vm")) {
            return projectRoot + "/backend/icoolkj-admin-api/src/main/java/com/icoolkj/platform/api/controller/" + table.getApiModule() + "/" + className + "Controller.java";
        }
        if (template.contains("menu.sql.vm")) {
            return projectRoot + "/backend/database/migration/gen/" + table.getBusinessName() + "_menu.sql";
        }
        if (template.contains("api.ts.vm")) {
            return projectRoot + "/frontend/src/api/" + table.getApiModule() + "/" + businessName + ".ts";
        }
        if (template.contains("types.ts.vm")) {
            return projectRoot + "/frontend/src/types/" + table.getApiModule() + "/" + businessName + ".ts";
        }
        if (template.contains("index.vue.vm")) {
            return projectRoot + "/frontend/src/views/" + table.getApiModule() + "/" + businessName + "/index.vue";
        }
        return projectRoot + "/gen/" + businessName + "/" + template;
    }

    public static String getGenI18nLocalePath(GenTable table, String locale) {
        return resolveProjectRoot(table) + "/frontend/src/i18n/gen/"
                + table.getBusinessName() + "/" + locale + ".json";
    }

    public static String getGenI18nLocaleRelativePath(GenTable table, String locale) {
        return "frontend/src/i18n/gen/" + table.getBusinessName() + "/" + locale + ".json";
    }

    /** Same JSON as frontend — Bean Validation keys {@code gen.{businessName}.{field}.{rule}}. */
    public static String getGenI18nBackendLocalePath(GenTable table, String locale) {
        return resolveProjectRoot(table) + "/backend/icoolkj-admin-" + table.getBackendModule()
                + "/src/main/resources/i18n/gen/" + table.getBusinessName() + "/" + locale + ".json";
    }

    public static String getGenI18nBackendLocaleRelativePath(GenTable table, String locale) {
        return "backend/icoolkj-admin-" + table.getBackendModule() + "/src/main/resources/i18n/gen/"
                + table.getBusinessName() + "/" + locale + ".json";
    }

    public static String getGenViewPath(GenTable table) {
        return resolveProjectRoot(table) + "/frontend/src/i18n/gen/"
                + table.getBusinessName() + "/view.json";
    }

    public static String getGenViewRelativePath(GenTable table) {
        return "frontend/src/i18n/gen/" + table.getBusinessName() + "/view.json";
    }

    public static String getGenViewBackendPath(GenTable table) {
        return resolveProjectRoot(table) + "/backend/icoolkj-admin-" + table.getBackendModule()
                + "/src/main/resources/i18n/gen/" + table.getBusinessName() + "/view.json";
    }

    public static String getGenViewBackendRelativePath(GenTable table) {
        return "backend/icoolkj-admin-" + table.getBackendModule() + "/src/main/resources/i18n/gen/"
                + table.getBusinessName() + "/view.json";
    }

    public static String resolveProjectRoot(GenTable table) {
        File sandboxRoot = discoverSandboxRoot();
        if (GenConstants.GEN_PATH.equals(table.getGenType())
                && org.apache.commons.lang3.StringUtils.isNotBlank(table.getGenPath())) {
            File custom = canonicalize(new File(table.getGenPath()));
            File sandbox = canonicalize(sandboxRoot);
            if (!isUnderOrEqual(custom, sandbox)) {
                throw new BusinessException("error.gen.path_outside_sandbox");
            }
            return custom.getAbsolutePath();
        }
        return sandboxRoot.getAbsolutePath();
    }

    /** Reject writes that escape the resolved project root (path traversal / absolute escape). */
    public static void assertPathInsideProjectRoot(String absolutePath, GenTable table) {
        File target = canonicalize(new File(absolutePath));
        File root = canonicalize(new File(resolveProjectRoot(table)));
        if (!isUnderOrEqual(target, root)) {
            throw new BusinessException("error.gen.path_outside_sandbox");
        }
    }

    private static File discoverSandboxRoot() {
        File dir = new File(System.getProperty("user.dir"));
        while (dir != null) {
            if (new File(dir, "frontend").isDirectory() && new File(dir, "backend").isDirectory()) {
                return dir;
            }
            dir = dir.getParentFile();
        }
        return new File(System.getProperty("user.dir"), GenConfig.getProjectRoot());
    }

    private static File canonicalize(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException ex) {
            return file.getAbsoluteFile();
        }
    }

    private static boolean isUnderOrEqual(File candidate, File root) {
        Path candidatePath = candidate.toPath().normalize();
        Path rootPath = root.toPath().normalize();
        return candidatePath.startsWith(rootPath);
    }

    private static Set<String> getImportList(GenTable table) {
        Set<String> imports = new HashSet<>();
        for (GenTableColumn column : table.getColumns()) {
            if (table.isSuperColumn(column.getJavaField())) {
                continue;
            }
            switch (column.getJavaType()) {
                case GenConstants.TYPE_LOCAL_DATE_TIME -> imports.add("java.time.LocalDateTime");
                case GenConstants.TYPE_LOCAL_DATE -> imports.add("java.time.LocalDate");
                case GenConstants.TYPE_BIGDECIMAL -> imports.add("java.math.BigDecimal");
                default -> {
                }
            }
        }
        return imports;
    }

    private static boolean hasDataScope(List<GenTableColumn> columns) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(resolveDataScopeDeptColumn(columns))
                || org.apache.commons.lang3.StringUtils.isNotBlank(resolveDataScopeUserColumn(columns));
    }

    private static String resolveDataScopeDeptColumn(List<GenTableColumn> columns) {
        return resolveColumnName(columns, "dept_id", "deptId");
    }

    private static String resolveDataScopeUserColumn(List<GenTableColumn> columns) {
        return resolveColumnName(columns, "user_id", "userId");
    }

    private static String resolveColumnName(List<GenTableColumn> columns, String... candidates) {
        if (columns == null) {
            return "";
        }
        for (GenTableColumn column : columns) {
            for (String candidate : candidates) {
                if (candidate.equals(column.getColumnName()) || candidate.equals(column.getJavaField())) {
                    return column.getColumnName();
                }
            }
        }
        return "";
    }

    private static boolean hasColumn(List<GenTableColumn> columns, String... candidates) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(resolveColumnName(columns, candidates));
    }

    private static String resolveOrderByColumn(List<GenTableColumn> columns) {
        String columnName = resolveColumnName(columns, "created_at", "createdAt",
                "operation_at", "operationAt", "login_at", "loginAt",
                "accessed_at", "accessedAt", "called_at", "calledAt",
                "uploaded_at", "uploadedAt", "updated_at", "updatedAt");
        if (org.apache.commons.lang3.StringUtils.isNotBlank(columnName)) {
            return columnName;
        }
        return "id";
    }

    private static String resolveParentMenuId(GenTable table, String productCode) {
        String fromOptions = GenTableOptionsHelper.getParentMenuId(table);
        if (StringUtils.isNotBlank(fromOptions)) {
            return fromOptions.trim();
        }
        if (ProductCodes.OA.equals(productCode)) {
            return "4000100000000000000";
        }
        if (ProductCodes.FILES.equals(productCode)) {
            return "4000210002000000000";
        }
        if (ProductCodes.AI.equals(productCode)) {
            return "4000060100000000000";
        }
        return resolveParentMenuIdByApiModule(table.getApiModule());
    }

    private static String resolveParentMenuIdByApiModule(String apiModule) {
        if ("tenant".equals(apiModule)) {
            return "4000030000000000000";
        }
        if ("tool".equals(apiModule)) {
            return "4000060000000000000";
        }
        if ("monitor".equals(apiModule)) {
            return "4000050000000000000";
        }
        if ("security".equals(apiModule)) {
            return "4000040000000000000";
        }
        return "4000020000000000000";
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /** English menu title for sys_menu.name_i18n en-US (page uses ClassName, buttons use suffix). */
    private static String resolveFunctionNameEn(GenTable table) {
        String className = table.getClassName();
        if (className == null || className.isEmpty()) {
            return capitalize(table.getBusinessName());
        }
        return className.replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    private static String resolvePermAlias(GenTable table) {
        String permissionPrefix = table.getPermissionPrefix();
        String source = StringUtils.substringAfterLast(permissionPrefix, ":");
        if (StringUtils.isBlank(source)) {
            source = table.getBusinessName();
        }
        return "PERM_" + toScreamingSnake(source);
    }

    private static String toScreamingSnake(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        String normalized = value.trim()
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalized.toUpperCase();
    }
}
