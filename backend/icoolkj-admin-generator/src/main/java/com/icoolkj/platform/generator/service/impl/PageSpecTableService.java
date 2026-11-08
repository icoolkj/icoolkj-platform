package com.icoolkj.platform.generator.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.generator.constant.GenConstants;
import com.icoolkj.platform.generator.constant.GenModuleCatalog;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenTableColumn;
import com.icoolkj.platform.generator.dto.GenTableAlterApplyRequest;
import com.icoolkj.platform.generator.dto.GenTableAlterDesignRequest;
import com.icoolkj.platform.generator.dto.GenTableCreateRequest;
import com.icoolkj.platform.generator.dto.GenTableDdlExecuteRequest;
import com.icoolkj.platform.generator.dto.GenTableDesignRequest;
import com.icoolkj.platform.generator.dto.GenTablePageQuery;
import com.icoolkj.platform.generator.mapper.GenTableColumnMapper;
import com.icoolkj.platform.generator.mapper.GenTableMapper;
import com.icoolkj.platform.generator.service.GenTableService;
import com.icoolkj.platform.generator.util.GenUtils;
import com.icoolkj.platform.generator.util.TableDdlBuilder;
import com.icoolkj.platform.generator.vo.GenModuleVO;
import com.icoolkj.platform.generator.vo.GenTableAlterDesignVO;
import com.icoolkj.platform.generator.vo.GenTableDesignColumnVO;
import com.icoolkj.platform.generator.vo.GenTableDdlDesignVO;
import com.icoolkj.platform.generator.vo.GenTableDdlExecuteResultVO;
import com.icoolkj.platform.generator.vo.GenTableDesignVO;
import com.icoolkj.platform.generator.vo.GenTablePrepareResultVO;
import com.icoolkj.platform.generator.vo.GenTableSchemaColumnVO;
import com.icoolkj.platform.generator.vo.GenTableSchemaVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PageSpecTableService {

    private final GenTableMapper genTableMapper;
    private final GenTableColumnMapper genTableColumnMapper;
    private final GenTableService genTableService;
    private final PageSpecTableAiDesigner tableAiDesigner;
    private final AiChatFacade aiChatFacade;

    public PageSpecTableService(GenTableMapper genTableMapper,
                                GenTableColumnMapper genTableColumnMapper,
                                GenTableService genTableService,
                                PageSpecTableAiDesigner tableAiDesigner,
                                AiChatFacade aiChatFacade) {
        this.genTableMapper = genTableMapper;
        this.genTableColumnMapper = genTableColumnMapper;
        this.genTableService = genTableService;
        this.tableAiDesigner = tableAiDesigner;
        this.aiChatFacade = aiChatFacade;
    }

    public List<GenModuleVO> listModules() {
        return GenModuleCatalog.all().stream().map(spec -> {
            GenModuleVO vo = new GenModuleVO();
            vo.setApiModule(spec.apiModule());
            vo.setLabelKey(spec.labelKey());
            vo.setTablePrefix(spec.tablePrefix());
            vo.setBackendModule(spec.backendModule());
            vo.setBaseType(spec.baseType());
            vo.setRoutePrefix(spec.routePrefix());
            vo.setIncludeTenantColumn(spec.includeTenantColumn());
            return vo;
        }).collect(Collectors.toList());
    }

    public PageResult<GenTable> pageDbTables(GenTablePageQuery query) {
        long total = genTableMapper.countWizardDbTableByQuery(query);
        List<GenTable> list = total > 0 ? genTableMapper.selectWizardDbTablePage(query) : List.of();
        return PageResult.of(list, total, query.getPageNo(), query.getPageSize());
    }

    public List<GenTable> listDbTables(GenTable query) {
        return genTableMapper.selectWizardDbTableList(query);
    }

    @Transactional
    public GenTablePrepareResultVO prepareTable(String tableName) {
        String name = tableName.trim().toLowerCase();
        if (genTableMapper.countTableInDb(name) == 0) {
            throw new BusinessException("error.gen.table_design.table_not_in_db", name);
        }
        GenTable existing = genTableMapper.selectGenTableByName(name);
        if (existing != null) {
            return toPrepareResult(existing, "existed");
        }
        genTableService.importTables(new String[]{name}, "system");
        GenTable imported = genTableMapper.selectGenTableByName(name);
        if (imported == null) {
            throw new BusinessException("error.gen.page_spec.import_failed", name);
        }
        return toPrepareResult(imported, "imported");
    }

    public GenTableDesignVO designTable(GenTableDesignRequest request) {
        GenModuleCatalog.require(request.getApiModule());
        GenTableDesignVO design = tableAiDesigner.design(request.getUserPrompt(), request.getApiModule());
        design.setApiModule(StringUtils.defaultIfBlank(request.getApiModule(), design.getApiModule()));
        return enrichCreatePreview(design);
    }

    public GenTableDdlDesignVO designCreateDdl(GenTableDesignRequest request) {
        GenModuleCatalog.require(request.getApiModule());
        GenTableDdlDesignVO design = tableAiDesigner.designCreateDdl(request.getUserPrompt(), request.getApiModule());
        String recordId = aiChatFacade.saveDdlRecord(
                design.getInvokeId(),
                "CREATE_DDL",
                request.getApiModule(),
                null,
                request.getUserPrompt(),
                design.getSummary(),
                design.getDdl());
        design.setDdlRecordId(recordId);
        return design;
    }

    public GenTableDdlDesignVO designAlterDdl(GenTableAlterDesignRequest request) {
        GenTableSchemaVO schema = getTableSchema(request.getTableName());
        GenTableDdlDesignVO design = tableAiDesigner.designAlterDdl(
                schema.getTableName(),
                request.getUserPrompt(),
                schema.getColumns());
        String recordId = aiChatFacade.saveDdlRecord(
                design.getInvokeId(),
                "ALTER_DDL",
                null,
                schema.getTableName(),
                request.getUserPrompt(),
                design.getSummary(),
                design.getDdl());
        design.setDdlRecordId(recordId);
        return design;
    }

    @Transactional
    public GenTableDdlExecuteResultVO executeDdl(GenTableDdlExecuteRequest request) {
        List<String> statements = TableDdlBuilder.splitStatements(request.getDdl());
        LinkedHashSet<String> affectedTables = new LinkedHashSet<>();
        for (String statement : statements) {
            TableDdlBuilder.validateExecutableStatement(statement);
            String normalized = statement.trim().toLowerCase();
            if (normalized.startsWith("create table")) {
                affectedTables.add(TableDdlBuilder.extractCreateTableName(statement));
            } else {
                affectedTables.add(TableDdlBuilder.extractAlterTableName(statement));
            }
            genTableMapper.executeDdl(statement);
        }

        GenTableDdlExecuteResultVO result = new GenTableDdlExecuteResultVO();
        for (String tableName : affectedTables) {
            GenTable table = genTableMapper.selectGenTableByName(tableName);
            String action;
            if (table == null) {
                genTableService.importTables(new String[]{tableName}, "system");
                table = genTableMapper.selectGenTableByName(tableName);
                action = "created";
            } else {
                genTableService.syncDb(tableName);
                table = genTableMapper.selectGenTableByName(tableName);
                action = "altered";
            }
            if (table == null) {
                throw new BusinessException("error.gen.page_spec.import_failed", tableName);
            }
            result.getTableNames().add(tableName);
            result.getTables().add(toPrepareResult(table, action));
        }
        return result;
    }

    public String previewCreateTable(GenTableDesignVO design) {
        if (design == null) {
            throw new BusinessException("error.gen.page_spec.table_name_required");
        }
        design.setApiModule(StringUtils.defaultIfBlank(design.getApiModule(), "system"));
        return enrichCreatePreview(design).getDdlPreview();
    }

    public String previewAlterTable(GenTableAlterDesignVO alter) {
        if (alter == null || StringUtils.isBlank(alter.getTableName())) {
            throw new BusinessException("error.gen.page_spec.table_name_required");
        }
        GenTableSchemaVO schema = getTableSchema(alter.getTableName());
        Set<String> existing = schema.getColumns().stream()
                .map(GenTableSchemaColumnVO::getColumnName)
                .collect(Collectors.toSet());
        TableDdlBuilder.validateAlterDesign(alter, existing);
        return TableDdlBuilder.buildAlterTableSql(alter, existing);
    }

    private GenTableDesignVO enrichCreatePreview(GenTableDesignVO design) {
        TableDdlBuilder.validateDesign(design);
        design.setDdlPreview(TableDdlBuilder.buildCreateTableSql(design));
        return design;
    }

    @Transactional
    public GenTablePrepareResultVO createTable(GenTableCreateRequest request) {
        GenTableDesignVO design = request.getDesign();
        TableDdlBuilder.validateDesign(design);
        String ddl = TableDdlBuilder.buildCreateTableSql(design);
        TableDdlBuilder.validateExecutableDdl(ddl);

        if (genTableMapper.countTableInDb(design.getTableName()) > 0) {
            throw new BusinessException("error.gen.table_design.table_exists", design.getTableName());
        }

        genTableMapper.executeDdl(ddl);
        genTableService.importTables(new String[]{design.getTableName()}, "system");
        applyDesignToGenTable(design);
        GenTable imported = genTableMapper.selectGenTableByName(design.getTableName());
        if (imported == null) {
            throw new BusinessException("error.gen.page_spec.import_failed", design.getTableName());
        }
        return toPrepareResult(imported, "created");
    }

    public GenTableSchemaVO getTableSchema(String tableName) {
        String name = tableName.trim().toLowerCase();
        if (genTableMapper.countTableInDb(name) == 0) {
            throw new BusinessException("error.gen.table_design.table_not_in_db", name);
        }
        GenTableSchemaVO schema = new GenTableSchemaVO();
        schema.setTableName(name);
        List<GenTable> metas = genTableMapper.selectDbTableListByNames(new String[]{name});
        if (!metas.isEmpty()) {
            schema.setTableComment(metas.get(0).getTableComment());
        }
        List<GenTableSchemaColumnVO> columns = genTableColumnMapper.selectDbTableColumnsByName(name).stream()
                .map(this::toSchemaColumn)
                .collect(Collectors.toList());
        schema.setColumns(columns);
        return schema;
    }

    public GenTableAlterDesignVO designAlterTable(GenTableAlterDesignRequest request) {
        GenTableSchemaVO schema = getTableSchema(request.getTableName());
        GenTableAlterDesignVO alter = tableAiDesigner.designAlter(
                schema.getTableName(),
                request.getUserPrompt(),
                schema.getColumns());
        Set<String> existing = schema.getColumns().stream()
                .map(GenTableSchemaColumnVO::getColumnName)
                .collect(Collectors.toSet());
        TableDdlBuilder.validateAlterDesign(alter, existing);
        alter.setDdlPreview(TableDdlBuilder.buildAlterTableSql(alter, existing));
        return alter;
    }

    @Transactional
    public GenTablePrepareResultVO applyAlterTable(GenTableAlterApplyRequest request) {
        GenTableAlterDesignVO alter = request.getDesign();
        GenTableSchemaVO schema = getTableSchema(alter.getTableName());
        Set<String> existing = schema.getColumns().stream()
                .map(GenTableSchemaColumnVO::getColumnName)
                .collect(Collectors.toSet());
        TableDdlBuilder.validateAlterDesign(alter, existing);
        String ddl = TableDdlBuilder.buildAlterTableSql(alter, existing);
        TableDdlBuilder.validateExecutableAlterDdl(ddl);
        genTableMapper.executeDdl(ddl);

        GenTable table = genTableMapper.selectGenTableByName(alter.getTableName());
        if (table == null) {
            genTableService.importTables(new String[]{alter.getTableName()}, "system");
        } else {
            genTableService.syncDb(alter.getTableName());
        }
        GenTable synced = genTableMapper.selectGenTableByName(alter.getTableName());
        if (synced == null) {
            throw new BusinessException("error.gen.page_spec.import_failed", alter.getTableName());
        }
        applyDesignColumnMeta(synced.getId(), alter.getAddColumns());
        return toPrepareResult(synced, "altered");
    }

    private GenTableSchemaColumnVO toSchemaColumn(GenTableColumn column) {
        GenTableSchemaColumnVO vo = new GenTableSchemaColumnVO();
        vo.setColumnName(column.getColumnName());
        vo.setColumnComment(column.getColumnComment());
        vo.setColumnType(column.getColumnType());
        vo.setRequired(column.isRequired());
        vo.setSystemColumn(TableDdlBuilder.isSystemColumn(column.getColumnName()));
        return vo;
    }

    private void applyDesignToGenTable(GenTableDesignVO design) {
        GenTable table = genTableMapper.selectGenTableByName(design.getTableName());
        if (table == null) {
            return;
        }
        GenModuleCatalog.enrichDesign(design);
        table.setApiModule(design.getApiModule());
        table.setBackendModule(design.getBackendModule());
        table.setBaseType(design.getBaseType());
        table.setRoutePrefix(design.getRoutePrefix());
        if (StringUtils.isNotBlank(design.getTableComment())) {
            table.setFunctionName(design.getTableComment());
        }
        if (StringUtils.isNotBlank(table.getBusinessName())) {
            table.setPermissionPrefix(design.getApiModule() + ":" + table.getBusinessName());
        }
        genTableMapper.updateGenTable(table);
        applyDesignColumnMeta(table.getId(), design.getColumns());
    }

    private void applyDesignColumnMeta(String tableId, List<GenTableDesignColumnVO> designColumns) {
        if (StringUtils.isBlank(tableId) || designColumns == null || designColumns.isEmpty()) {
            return;
        }
        Map<String, GenTableDesignColumnVO> byName = designColumns.stream()
                .filter(c -> StringUtils.isNotBlank(c.getColumnName()))
                .collect(Collectors.toMap(
                        c -> c.getColumnName().trim().toLowerCase(),
                        c -> c,
                        (a, b) -> b));
        if (byName.isEmpty()) {
            return;
        }
        for (GenTableColumn column : genTableColumnMapper.selectGenTableColumnListByTableId(tableId)) {
            GenTableDesignColumnVO designCol = byName.get(column.getColumnName().toLowerCase());
            if (designCol == null || StringUtils.isBlank(designCol.getDictType())) {
                continue;
            }
            column.setDictType(designCol.getDictType().trim());
            column.setHtmlType(GenConstants.HTML_SELECT);
            genTableColumnMapper.updateGenTableColumn(column);
        }
    }

    private GenTablePrepareResultVO toPrepareResult(GenTable table, String action) {
        GenTablePrepareResultVO result = new GenTablePrepareResultVO();
        result.setTableId(table.getId());
        result.setTableName(table.getTableName());
        result.setPageKey(StringUtils.defaultIfBlank(table.getBusinessName(), GenUtils.getBusinessName(table.getTableName())));
        result.setAction(action);
        return result;
    }
}
