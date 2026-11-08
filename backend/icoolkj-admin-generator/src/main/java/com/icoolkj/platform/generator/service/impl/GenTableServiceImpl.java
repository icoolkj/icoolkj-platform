package com.icoolkj.platform.generator.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.generator.config.GeneratorRuntimeProperties;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenTableColumn;
import com.icoolkj.platform.generator.dto.GenTablePageQuery;
import com.icoolkj.platform.generator.mapper.GenTableColumnMapper;
import com.icoolkj.platform.generator.mapper.GenTableMapper;
import com.icoolkj.platform.generator.service.GenI18nMessageService;
import com.icoolkj.platform.generator.service.GenTableService;
import com.icoolkj.platform.generator.service.GenViewConfigService;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.generator.util.GenI18nBuilder;
import com.icoolkj.platform.generator.util.GenTextNormalize;
import com.icoolkj.platform.generator.util.GenUtils;
import com.icoolkj.platform.generator.util.GenViewBuilder;
import com.icoolkj.platform.generator.util.VelocityInitializer;
import com.icoolkj.platform.generator.util.VelocityUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GenTableServiceImpl implements GenTableService {

    private final GenTableMapper genTableMapper;
    private final GenTableColumnMapper genTableColumnMapper;
    private final GenI18nMessageService genI18nMessageService;
    private final GenViewConfigService genViewConfigService;
    private final GeneratorRuntimeProperties generatorRuntimeProperties;

    public GenTableServiceImpl(GenTableMapper genTableMapper,
                               GenTableColumnMapper genTableColumnMapper,
                               GenI18nMessageService genI18nMessageService,
                               GenViewConfigService genViewConfigService,
                               GeneratorRuntimeProperties generatorRuntimeProperties) {
        this.genTableMapper = genTableMapper;
        this.genTableColumnMapper = genTableColumnMapper;
        this.genI18nMessageService = genI18nMessageService;
        this.genViewConfigService = genViewConfigService;
        this.generatorRuntimeProperties = generatorRuntimeProperties;
    }

    @Override
    public PageResult<GenTable> page(GenTablePageQuery query) {
        long total = genTableMapper.countGenTableByQuery(query);
        List<GenTable> list = total > 0 ? genTableMapper.selectGenTablePage(query) : List.of();
        return PageResult.of(list, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public List<GenTable> selectGenTableList(GenTable query) {
        return genTableMapper.selectGenTableList(query);
    }

    @Override
    public GenTable selectGenTableById(String id) {
        GenTable table = genTableMapper.selectGenTableById(id);
        if (table != null) {
            table.setColumns(genTableColumnMapper.selectGenTableColumnListByTableId(id));
            setPkColumn(table);
        }
        return table;
    }

    @Override
    public List<GenTable> selectDbTableList(GenTable query) {
        return genTableMapper.selectDbTableList(query);
    }

    @Override
    public PageResult<GenTable> pageDbTable(GenTablePageQuery query) {
        long total = genTableMapper.countDbTableByQuery(query);
        List<GenTable> list = total > 0 ? genTableMapper.selectDbTablePage(query) : List.of();
        return PageResult.of(list, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    @Transactional
    public void importTables(String[] tableNames, String operator) {
        List<GenTable> tables = genTableMapper.selectDbTableListByNames(tableNames);
        for (GenTable table : tables) {
            GenUtils.initTable(table, operator);
            table.setId(IdGeneratorUtils.nextIdStr());
            genTableMapper.insertGenTable(table);
            List<GenTableColumn> columns = genTableColumnMapper.selectDbTableColumnsByName(table.getTableName());
            int sort = 1;
            for (GenTableColumn column : columns) {
                GenUtils.initColumnField(column, table);
                column.setId(IdGeneratorUtils.nextIdStr());
                column.setTableId(table.getId());
                column.setSort(sort++);
                genTableColumnMapper.insertGenTableColumn(column);
            }
        }
    }

    @Override
    @Transactional
    public void updateGenTable(GenTable genTable) {
        GenTable existing = genTableMapper.selectGenTableById(genTable.getId());
        if (existing == null) {
            throw new BusinessException("error.gen.table_not_imported", genTable.getTableName());
        }
        mergeGenTable(existing, genTable);
        genTableMapper.updateGenTable(existing);
        if (genTable.getColumns() != null && !genTable.getColumns().isEmpty()) {
            for (GenTableColumn column : genTable.getColumns()) {
                genTableColumnMapper.updateGenTableColumn(column);
            }
        }
    }

    @Override
    @Transactional
    public void deleteGenTableByIds(String[] ids) {
        for (String id : ids) {
            genI18nMessageService.deleteByTableId(id);
            genViewConfigService.deleteByTableId(id);
            genTableColumnMapper.deleteGenTableColumnByTableId(id);
        }
        genTableMapper.deleteGenTableByIds(ids);
    }

    @Override
    public Map<String, String> previewCode(String id) {
        GenTable table = selectGenTableById(id);
        return renderTemplates(table);
    }

    @Override
    public byte[] downloadCode(String tableName) {
        GenTable table = loadTableByName(tableName);
        Map<String, String> data = renderTemplates(table);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(baos)) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                IOUtils.write(entry.getValue(), zip, StandardCharsets.UTF_8);
                zip.closeEntry();
            }
            zip.finish();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException("error.gen.zip_failed");
        }
    }

    @Override
    public byte[] downloadCodeBatch(String[] tableNames) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(baos)) {
            Map<String, String> merged = new LinkedHashMap<>();
            StringBuilder manifest = new StringBuilder("# 代码生成输出路径\n\n");
            for (String tableName : tableNames) {
                String normalized = tableName == null ? "" : tableName.trim();
                if (StringUtils.isBlank(normalized)) {
                    continue;
                }
                GenTable table = loadTableByName(normalized);
                Map<String, String> data = renderTemplates(table);
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    if ("_OUTPUT_PATHS.md".equals(entry.getKey())) {
                        manifest.append("## ").append(table.getTableName()).append("\n\n")
                                .append(entry.getValue()).append("\n");
                        continue;
                    }
                    merged.put(entry.getKey(), entry.getValue());
                }
            }
            merged.put("OUTPUT_PATHS.md", manifest.toString());
            for (Map.Entry<String, String> entry : merged.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                IOUtils.write(entry.getValue(), zip, StandardCharsets.UTF_8);
                zip.closeEntry();
            }
            zip.finish();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException("error.gen.zip_failed");
        }
    }

    @Override
    public void generateCode(String tableName) {
        if (!generatorRuntimeProperties.isDiskWriteEnabled()) {
            throw new BusinessException("error.gen.disk_write_disabled");
        }
        GenTable table = loadTableByName(tableName);
        VelocityInitializer.initVelocity();
        List<String> templates = VelocityUtils.getTemplateList(table);
        VelocityContext context = VelocityUtils.prepareContext(table);
        for (String template : templates) {
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, StandardCharsets.UTF_8.name());
            tpl.merge(context, sw);
            try {
                String path = VelocityUtils.getFileName(template, table);
                VelocityUtils.assertPathInsideProjectRoot(path, table);
                FileUtils.writeStringToFile(new File(path),
                        GenTextNormalize.toLfWithTrailingNewline(sw.toString()), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new BusinessException("error.gen.write_file_failed", template);
            }
        }
        writeGenI18nLocaleFiles(table);
        writeGenViewFile(table);
        try {
            String manifestPath = VelocityUtils.resolveProjectRoot(table) + "/gen/" + table.getTableName() + "/OUTPUT_PATHS.md";
            File manifestFile = new File(manifestPath);
            FileUtils.writeStringToFile(manifestFile,
                    GenTextNormalize.toLfWithTrailingNewline(VelocityUtils.buildOutputPathManifest(table, templates)),
                    StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BusinessException("error.gen.write_file_failed", "OUTPUT_PATHS.md");
        }
    }

    @Override
    @Transactional
    public void syncDb(String tableName) {
        GenTable table = loadTableByName(tableName);
        refreshTableMetaFromDb(table);
        genTableMapper.updateGenTable(table);
        List<GenTableColumn> dbColumns = genTableColumnMapper.selectDbTableColumnsByName(tableName);
        genTableColumnMapper.deleteGenTableColumnByTableId(table.getId());
        int sort = 1;
        for (GenTableColumn column : dbColumns) {
            GenUtils.initColumnField(column, table);
            column.setId(IdGeneratorUtils.nextIdStr());
            column.setTableId(table.getId());
            column.setSort(sort++);
            genTableColumnMapper.insertGenTableColumn(column);
        }
    }

    private GenTable loadTableByName(String tableName) {
        GenTable table = genTableMapper.selectGenTableByName(tableName);
        if (table == null) {
            throw new BusinessException("error.gen.table_not_imported", tableName);
        }
        table.setColumns(genTableColumnMapper.selectGenTableColumnListByTableId(table.getId()));
        setPkColumn(table);
        return table;
    }

    private Map<String, String> renderTemplates(GenTable table) {
        VelocityInitializer.initVelocity();
        List<String> templates = VelocityUtils.getTemplateList(table);
        VelocityContext context = VelocityUtils.prepareContext(table);
        Map<String, String> data = new LinkedHashMap<>();
        data.put("_OUTPUT_PATHS.md",
                GenTextNormalize.toLfWithTrailingNewline(VelocityUtils.buildOutputPathManifest(table, templates)));
        for (String template : templates) {
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, StandardCharsets.UTF_8.name());
            tpl.merge(context, sw);
            data.put(VelocityUtils.getRelativeOutputPath(template, table),
                    GenTextNormalize.toLfWithTrailingNewline(sw.toString()));
        }
        data.put(VelocityUtils.getGenI18nLocaleRelativePath(table, LocaleResolver.LOCALE_ZH_CN),
                buildGenI18nLocaleFileContent(table, LocaleResolver.LOCALE_ZH_CN));
        data.put(VelocityUtils.getGenI18nLocaleRelativePath(table, LocaleResolver.LOCALE_EN_US),
                buildGenI18nLocaleFileContent(table, LocaleResolver.LOCALE_EN_US));
        data.put(VelocityUtils.getGenI18nBackendLocaleRelativePath(table, LocaleResolver.LOCALE_ZH_CN),
                buildGenI18nLocaleFileContent(table, LocaleResolver.LOCALE_ZH_CN));
        data.put(VelocityUtils.getGenI18nBackendLocaleRelativePath(table, LocaleResolver.LOCALE_EN_US),
                buildGenI18nLocaleFileContent(table, LocaleResolver.LOCALE_EN_US));
        data.put(VelocityUtils.getGenViewRelativePath(table), buildGenViewFileContent(table));
        data.put(VelocityUtils.getGenViewBackendRelativePath(table), buildGenViewFileContent(table));
        return data;
    }

    private void writeGenI18nLocaleFiles(GenTable table) {
        writeGenI18nLocaleFile(table, LocaleResolver.LOCALE_ZH_CN);
        writeGenI18nLocaleFile(table, LocaleResolver.LOCALE_EN_US);
    }

    private void writeGenI18nLocaleFile(GenTable table, String locale) {
        try {
            String content = buildGenI18nLocaleFileContent(table, locale);
            for (String path : List.of(
                    VelocityUtils.getGenI18nLocalePath(table, locale),
                    VelocityUtils.getGenI18nBackendLocalePath(table, locale))) {
                VelocityUtils.assertPathInsideProjectRoot(path, table);
                File file = new File(path);
                FileUtils.forceMkdirParent(file);
                FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new BusinessException("error.gen.write_file_failed", locale + ".json");
        }
    }

    private String buildGenI18nLocaleFileContent(GenTable table, String locale) {
        String fresh = GenI18nBuilder.buildJson(table, table.getColumns(), locale);
        String existing = readExistingGenI18nFile(table, locale);
        if (StringUtils.isNotBlank(existing)) {
            return GenI18nBuilder.mergeJson(existing, fresh, locale);
        }
        return fresh;
    }

    private String readExistingGenI18nFile(GenTable table, String locale) {
        for (String path : List.of(
                VelocityUtils.getGenI18nLocalePath(table, locale),
                VelocityUtils.getGenI18nBackendLocalePath(table, locale))) {
            File file = new File(path);
            if (!file.exists()) {
                continue;
            }
            try {
                return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            } catch (IOException ignored) {
                // try next path
            }
        }
        return null;
    }

    private void writeGenViewFile(GenTable table) {
        try {
            String content = buildGenViewFileContent(table);
            for (String path : List.of(
                    VelocityUtils.getGenViewPath(table),
                    VelocityUtils.getGenViewBackendPath(table))) {
                VelocityUtils.assertPathInsideProjectRoot(path, table);
                File file = new File(path);
                FileUtils.forceMkdirParent(file);
                FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new BusinessException("error.gen.write_file_failed", "view.json");
        }
    }

    private String buildGenViewFileContent(GenTable table) {
        String fresh = GenViewBuilder.buildJson(table, table.getColumns());
        String existing = readExistingGenViewFile(table);
        if (StringUtils.isNotBlank(existing)) {
            return GenViewBuilder.mergeJson(existing, fresh);
        }
        return fresh;
    }

    private String readExistingGenViewFile(GenTable table) {
        for (String path : List.of(
                VelocityUtils.getGenViewPath(table),
                VelocityUtils.getGenViewBackendPath(table))) {
            File file = new File(path);
            if (!file.exists()) {
                continue;
            }
            try {
                return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            } catch (IOException ignored) {
                // try next path
            }
        }
        return null;
    }

    private void setPkColumn(GenTable table) {
        for (GenTableColumn column : table.getColumns()) {
            if (column.isPk()) {
                table.setPkColumn(column);
                break;
            }
        }
        if (table.getPkColumn() == null && table.getColumns() != null && !table.getColumns().isEmpty()) {
            table.setPkColumn(table.getColumns().get(0));
        }
    }

    private void mergeGenTable(GenTable target, GenTable incoming) {
        if (incoming.getTableComment() != null) {
            target.setTableComment(incoming.getTableComment());
        }
        if (incoming.getClassName() != null) {
            target.setClassName(incoming.getClassName());
        }
        if (incoming.getTplCategory() != null) {
            target.setTplCategory(incoming.getTplCategory());
        }
        if (incoming.getTplWebType() != null) {
            target.setTplWebType(incoming.getTplWebType());
        }
        if (incoming.getBackendModule() != null) {
            target.setBackendModule(incoming.getBackendModule());
        }
        if (incoming.getApiModule() != null) {
            target.setApiModule(incoming.getApiModule());
        }
        if (incoming.getBaseType() != null) {
            target.setBaseType(incoming.getBaseType());
        }
        if (incoming.getBusinessName() != null) {
            target.setBusinessName(incoming.getBusinessName());
        }
        if (incoming.getFunctionName() != null) {
            target.setFunctionName(incoming.getFunctionName());
        }
        if (incoming.getFunctionAuthor() != null) {
            target.setFunctionAuthor(incoming.getFunctionAuthor());
        }
        if (incoming.getRoutePrefix() != null) {
            target.setRoutePrefix(incoming.getRoutePrefix());
        }
        if (incoming.getPermissionPrefix() != null) {
            target.setPermissionPrefix(incoming.getPermissionPrefix());
        }
        if (incoming.getGenType() != null) {
            target.setGenType(incoming.getGenType());
        }
        if (incoming.getGenPath() != null) {
            target.setGenPath(incoming.getGenPath());
        }
        if (incoming.getOptions() != null) {
            target.setOptions(incoming.getOptions());
        }
        if (incoming.getRemark() != null) {
            target.setRemark(incoming.getRemark());
        }
    }

    /** Reload table_comment from information_schema; repair blank/corrupted function_name. */
    private void refreshTableMetaFromDb(GenTable table) {
        List<GenTable> rows = genTableMapper.selectDbTableListByNames(new String[]{table.getTableName()});
        if (rows.isEmpty() || StringUtils.isBlank(rows.get(0).getTableComment())) {
            return;
        }
        String comment = rows.get(0).getTableComment();
        table.setTableComment(comment);
        String functionName = table.getFunctionName();
        if (StringUtils.isBlank(functionName) || functionName.contains("?")) {
            table.setFunctionName(comment);
        }
    }

}
