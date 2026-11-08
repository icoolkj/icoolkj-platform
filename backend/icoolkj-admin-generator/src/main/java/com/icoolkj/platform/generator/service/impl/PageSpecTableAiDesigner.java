package com.icoolkj.platform.generator.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.generator.constant.GenModuleCatalog;
import com.icoolkj.platform.common.constant.AiInvokeBizType;
import com.icoolkj.platform.generator.vo.GenTableAlterDesignVO;
import com.icoolkj.platform.generator.vo.GenTableAlterModifyColumnVO;
import com.icoolkj.platform.generator.vo.GenTableDesignColumnVO;
import com.icoolkj.platform.generator.vo.GenTableDesignVO;
import com.icoolkj.platform.generator.vo.GenTableDdlDesignVO;
import com.icoolkj.platform.generator.vo.GenTableSchemaColumnVO;
import com.icoolkj.platform.generator.vo.AiChatResultVO;
import com.icoolkj.platform.system.ai.service.AiRuntimeConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PageSpecTableAiDesigner {

    private static final Pattern FENCE_JSON = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern FENCE_SQL = Pattern.compile("```(?:sql|mysql)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AiRuntimeConfig aiRuntimeConfig;
    private final AiChatFacade aiChatFacade;

    public PageSpecTableAiDesigner(AiRuntimeConfig aiRuntimeConfig, AiChatFacade aiChatFacade) {
        this.aiRuntimeConfig = aiRuntimeConfig;
        this.aiChatFacade = aiChatFacade;
    }

    public GenTableDesignVO design(String userPrompt, String apiModule) {
        if (!aiRuntimeConfig.isEnabled() || StringUtils.isBlank(aiRuntimeConfig.getApiKey())) {
            throw new BusinessException("error.ai.not_enabled");
        }
        String module = StringUtils.defaultIfBlank(apiModule, "system");
        GenModuleCatalog.require(module);
        String moduleRules = GenModuleCatalog.all().stream()
                .map(spec -> "- " + spec.apiModule() + "：表前缀 " + spec.tablePrefix()
                        + "，backend=" + spec.backendModule() + "，baseType=" + spec.baseType())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
        String systemPrompt = """
                你是 icoolkj 平台 MySQL 表结构设计助手。根据用户需求设计业务表（不含 id/tenant/审计/deleted，系统会自动补齐）。
                只输出 JSON，结构：
                {
                  "tableName": "sys_product",
                  "tableComment": "产品表",
                  "summary": "一句话说明",
                  "columns": [
                    { "columnName": "product_name", "columnComment": "产品名称", "columnType": "varchar(100)", "required": true, "dictType": "sys_common_status" }
                  ]
                }
                模块与表前缀（apiModule 由用户指定，不要擅自更改表前缀）：
                """
                + moduleRules + """
                
                通用规则：
                - columnName 小写 snake_case
                - columnType 仅限 varchar(n)/char(n)/int/bigint/tinyint(1)/decimal(10,2)/datetime(3)/text
                - status 类字段用 char(1)，并给出 dictType（如 sys_common_status）
                - 不要输出 id、tenant_id、created_by、created_at、updated_by、updated_at、deleted
                """;

        String userMessage = "目标 apiModule：" + module + "\n用户需求：\n" + userPrompt.trim()
                + "\n\n表名必须使用模块对应前缀。";
        String raw = chat(AiInvokeBizType.TABLE_DESIGN, systemPrompt, userMessage).getContent();
        return parseDesign(extractJson(raw), module);
    }

    public GenTableDdlDesignVO designCreateDdl(String userPrompt, String apiModule) {
        if (!aiRuntimeConfig.isEnabled() || StringUtils.isBlank(aiRuntimeConfig.getApiKey())) {
            throw new BusinessException("error.ai.not_enabled");
        }
        String module = StringUtils.defaultIfBlank(apiModule, "system");
        GenModuleCatalog.require(module);
        boolean tenant = GenModuleCatalog.includeTenantColumn(module);
        String prefix = GenModuleCatalog.all().stream()
                .filter(spec -> spec.apiModule().equals(module))
                .map(GenModuleCatalog.ModuleSpec::tablePrefix)
                .findFirst()
                .orElse("sys_");
        String tenantBlock = tenant ? """
                  tenant_id varchar(20) not null default '0' comment '租户ID，0表示平台级',
                """ : "";
        String systemPrompt = """
                你是 icoolkj 平台 MySQL DDL 助手。根据用户需求直接输出可执行的 CREATE TABLE 语句（MySQL 8，InnoDB，utf8mb4）。
                只输出 SQL，不要 JSON。可用 ```sql 代码块包裹。
                规则：
                - 表名必须使用模块前缀（当前模块前缀："""
                + prefix + """
                ）
                - 每张表必须包含标准字段：id、"""
                + (tenant ? "tenant_id、" : "")
                + """
                created_by、created_at、updated_by、updated_at、deleted、primary key (id)
                - 业务字段放在 id 之后、审计字段之前
                - 可使用 CREATE TABLE IF NOT EXISTS
                - 支持一次输出多张表（多条 CREATE TABLE，用分号分隔），例如主表+明细表
                - 禁止 DROP/TRUNCATE/INSERT/UPDATE/DELETE/ALTER
                - column 命名 snake_case；类型用 varchar(n)/char(n)/int/bigint/tinyint(1)/decimal(10,2)/datetime(3)/text
                单表示例结构：
                CREATE TABLE IF NOT EXISTS """
                + prefix + """
                product (
                  id varchar(20) not null comment '主键ID',
                """
                + tenantBlock + """
                  product_name varchar(200) not null comment '产品名称',
                  created_by varchar(20) null comment '创建人',
                  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
                  updated_by varchar(20) null comment '更新人',
                  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
                  deleted tinyint(1) not null default 0 comment '是否删除',
                  primary key (id)"""
                + (tenant ? ",\n  key idx_" + prefix + "product_tenant_id (tenant_id)" : "")
                + """
                ) engine=innodb default charset=utf8mb4 comment='产品表'
                """;

        String userMessage = "目标 apiModule：" + module + "\n用户需求：\n" + userPrompt.trim();
        AiChatResultVO chatResult = chat(AiInvokeBizType.DDL_CREATE, systemPrompt, userMessage);
        GenTableDdlDesignVO design = parseDdlDesign(chatResult.getContent());
        design.setInvokeId(chatResult.getInvokeId());
        return design;
    }

    public GenTableDdlDesignVO designAlterDdl(String tableName, String userPrompt, List<GenTableSchemaColumnVO> currentColumns) {
        if (!aiRuntimeConfig.isEnabled() || StringUtils.isBlank(aiRuntimeConfig.getApiKey())) {
            throw new BusinessException("error.ai.not_enabled");
        }
        String schemaJson;
        try {
            schemaJson = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(currentColumns);
        } catch (Exception ex) {
            throw new BusinessException("error.gen.page_spec.ai_parse_failed", ex.getMessage());
        }
        String systemPrompt = """
                你是 icoolkj 平台 MySQL DDL 助手。根据用户需求对已有表生成可执行的 ALTER TABLE 语句（仅 ADD COLUMN / MODIFY COLUMN）。
                只输出 SQL，不要 JSON。可用 ```sql 代码块包裹。
                规则：
                - 禁止 DROP/RENAME/TRUNCATE/CREATE TABLE
                - 禁止修改 id、tenant_id、created_by、created_at、updated_by、updated_at、deleted
                - 一条 ALTER 可含多个 ADD/MODIFY 子句，或多条 ALTER TABLE 语句（分号分隔）
                - 新增字段名不得与现有字段重复
                """;

        String userMessage = "目标表：" + tableName.trim().toLowerCase()
                + "\n\n当前字段（systemColumn=true 为系统字段，不可变更）：\n"
                + schemaJson
                + "\n\n变更需求：\n"
                + userPrompt.trim();
        AiChatResultVO chatResult = chat(AiInvokeBizType.DDL_ALTER, systemPrompt, userMessage);
        GenTableDdlDesignVO design = parseDdlDesign(chatResult.getContent());
        design.setInvokeId(chatResult.getInvokeId());
        return design;
    }

    private GenTableDdlDesignVO parseDdlDesign(String raw) {
        String ddl = extractSql(raw);
        if (StringUtils.isBlank(ddl)) {
            throw new BusinessException("error.gen.page_spec.ai_empty_ddl");
        }
        GenTableDdlDesignVO vo = new GenTableDdlDesignVO();
        vo.setDdl(ddl.trim());
        vo.setSummary(extractSummary(raw, ddl));
        return vo;
    }

    private static String extractSummary(String raw, String ddl) {
        String prefix = raw.substring(0, Math.max(0, raw.indexOf(ddl))).trim();
        if (prefix.length() > 200) {
            prefix = prefix.substring(0, 200) + "...";
        }
        return StringUtils.isBlank(prefix) ? null : prefix;
    }

    private static String extractSql(String raw) {
        Matcher m = FENCE_SQL.matcher(raw);
        if (m.find()) {
            return m.group(1).trim();
        }
        String trimmed = raw.trim();
        if (trimmed.toLowerCase().startsWith("create table") || trimmed.toLowerCase().startsWith("alter table")) {
            return trimmed;
        }
        int createIdx = indexOfIgnoreCase(trimmed, "create table");
        int alterIdx = indexOfIgnoreCase(trimmed, "alter table");
        int start = createIdx >= 0 && alterIdx >= 0 ? Math.min(createIdx, alterIdx)
                : Math.max(createIdx, alterIdx);
        if (start >= 0) {
            return trimmed.substring(start).trim();
        }
        return trimmed;
    }

    private static int indexOfIgnoreCase(String text, String needle) {
        return text.toLowerCase().indexOf(needle.toLowerCase());
    }

    public GenTableAlterDesignVO designAlter(String tableName, String userPrompt, List<GenTableSchemaColumnVO> currentColumns) {
        if (!aiRuntimeConfig.isEnabled() || StringUtils.isBlank(aiRuntimeConfig.getApiKey())) {
            throw new BusinessException("error.ai.not_enabled");
        }
        String schemaJson;
        try {
            schemaJson = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(currentColumns);
        } catch (Exception ex) {
            throw new BusinessException("error.gen.page_spec.ai_parse_failed", ex.getMessage());
        }
        String systemPrompt = """
                你是 icoolkj 平台 MySQL 表结构变更助手。根据用户需求对已有表生成 ALTER 方案（仅 ADD COLUMN / MODIFY COLUMN）。
                只输出 JSON，结构：
                {
                  "summary": "变更说明",
                  "addColumns": [
                    { "columnName": "brand", "columnComment": "品牌", "columnType": "varchar(100)", "required": false }
                  ],
                  "modifyColumns": [
                    { "columnName": "price", "columnComment": "价格", "columnType": "decimal(12,2)", "required": true }
                  ]
                }
                规则：
                - 仅对业务字段 addColumns / modifyColumns，禁止动 id、tenant_id、created_by、created_at、updated_by、updated_at、deleted
                - 禁止 DROP/RENAME 字段
                - addColumns 中的 columnName 不得与现有字段重复
                - modifyColumns 中的 columnName 必须来自现有业务字段
                - columnType 仅限 varchar(n)/char(n)/int/bigint/tinyint(1)/decimal(10,2)/datetime(3)/text
                - 若无变更，addColumns 与 modifyColumns 均为空数组（后端会拒绝）
                """;

        String userMessage = "目标表：" + tableName.trim().toLowerCase()
                + "\n\n当前字段（systemColumn=true 为系统字段，不可变更）：\n"
                + schemaJson
                + "\n\n变更需求：\n"
                + userPrompt.trim();
        String raw = chat(AiInvokeBizType.TABLE_ALTER_DESIGN, systemPrompt, userMessage).getContent();
        return parseAlterDesign(extractJson(raw), tableName);
    }

    private AiChatResultVO chat(String bizType, String systemPrompt, String userMessage) {
        return aiChatFacade.chat(aiRuntimeConfig, systemPrompt, userMessage, bizType);
    }

    private GenTableAlterDesignVO parseAlterDesign(String json, String tableName) {
        try {
            JsonNode root = MAPPER.readTree(json);
            GenTableAlterDesignVO design = new GenTableAlterDesignVO();
            design.setTableName(tableName.trim().toLowerCase());
            design.setSummary(text(root, "summary"));

            List<GenTableDesignColumnVO> addColumns = new ArrayList<>();
            if (root.has("addColumns") && root.get("addColumns").isArray()) {
                Iterator<JsonNode> it = root.get("addColumns").elements();
                while (it.hasNext()) {
                    JsonNode node = it.next();
                    GenTableDesignColumnVO col = new GenTableDesignColumnVO();
                    col.setColumnName(text(node, "columnName"));
                    col.setColumnComment(text(node, "columnComment"));
                    col.setColumnType(text(node, "columnType"));
                    col.setRequired(node.path("required").asBoolean(false));
                    String dictType = text(node, "dictType");
                    col.setDictType(StringUtils.isBlank(dictType) ? null : dictType);
                    addColumns.add(col);
                }
            }
            design.setAddColumns(addColumns);

            List<GenTableAlterModifyColumnVO> modifyColumns = new ArrayList<>();
            if (root.has("modifyColumns") && root.get("modifyColumns").isArray()) {
                Iterator<JsonNode> it = root.get("modifyColumns").elements();
                while (it.hasNext()) {
                    JsonNode node = it.next();
                    GenTableAlterModifyColumnVO col = new GenTableAlterModifyColumnVO();
                    col.setColumnName(text(node, "columnName"));
                    col.setColumnComment(text(node, "columnComment"));
                    col.setColumnType(text(node, "columnType"));
                    if (node.has("required") && !node.get("required").isNull()) {
                        col.setRequired(node.get("required").asBoolean());
                    }
                    modifyColumns.add(col);
                }
            }
            design.setModifyColumns(modifyColumns);
            return design;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("error.gen.page_spec.ai_parse_failed", ex.getMessage());
        }
    }

    private GenTableDesignVO parseDesign(String json, String defaultModule) {
        try {
            JsonNode root = MAPPER.readTree(json);
            GenTableDesignVO design = new GenTableDesignVO();
            design.setTableName(text(root, "tableName"));
            design.setTableComment(text(root, "tableComment"));
            design.setApiModule(defaultModule);
            design.setSummary(text(root, "summary"));

            List<GenTableDesignColumnVO> columns = new ArrayList<>();
            if (root.has("columns") && root.get("columns").isArray()) {
                Iterator<JsonNode> it = root.get("columns").elements();
                while (it.hasNext()) {
                    JsonNode node = it.next();
                    GenTableDesignColumnVO col = new GenTableDesignColumnVO();
                    col.setColumnName(text(node, "columnName"));
                    col.setColumnComment(text(node, "columnComment"));
                    col.setColumnType(text(node, "columnType"));
                    col.setRequired(node.path("required").asBoolean(false));
                    String dictType = text(node, "dictType");
                    col.setDictType(StringUtils.isBlank(dictType) ? null : dictType);
                    columns.add(col);
                }
            }
            design.setColumns(columns);
            return design;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("error.gen.page_spec.ai_parse_failed", ex.getMessage());
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static String extractJson(String raw) {
        Matcher m = FENCE_JSON.matcher(raw);
        if (m.find()) {
            return m.group(1).trim();
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw.trim();
    }
}
