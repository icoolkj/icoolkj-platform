package com.icoolkj.platform.generator.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenTableColumn;
import com.icoolkj.platform.generator.dto.GenPageSpecCompileRequest;
import com.icoolkj.platform.generator.dto.GenPageSpecGenerateRequest;
import com.icoolkj.platform.generator.dto.GenPageSpecValidateRequest;
import com.icoolkj.platform.generator.service.GenPageSpecService;
import com.icoolkj.platform.generator.service.GenTableService;
import com.icoolkj.platform.generator.util.PageSpecNormalizer;
import com.icoolkj.platform.generator.vo.GenPageSpecCompileResultVO;
import com.icoolkj.platform.generator.vo.GenPageSpecValidateResultVO;
import com.icoolkj.platform.generator.dto.GenPageSpecPublishRequest;
import com.icoolkj.platform.generator.util.PageSpecDraftBuilder;
import com.icoolkj.platform.generator.util.PageSpecJsonSchemaValidator;
import com.icoolkj.platform.generator.util.PageSpecYamlAssembler;
import com.icoolkj.platform.common.constant.AiInvokeBizType;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.generator.util.GenClasspathBaselineLoader;
import com.icoolkj.platform.generator.util.GenI18nBuilder;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.generator.vo.GenPageSpecDraftVO;
import com.icoolkj.platform.system.ai.service.AiRuntimeConfig;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GenPageSpecServiceImpl implements GenPageSpecService {

    private static final Pattern FENCE_YAML = Pattern.compile("```(?:yaml|yml)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /** Keep in sync with frontend registerPlatformBlocks.ts */
    private static final Set<String> KNOWN_DIALOG_WIDGETS = Set.of(
            "GenUserRoleDialog",
            "GenMenuAuthDialog",
            "GenNoticeDetailDialog",
            "GenSiteMessageDetailDialog",
            "GenAiInvokeLogDetailDialog",
            "GenAiDdlLogDetailDialog",
            "GenAiChatLogDetailDialog"
    );

    private final GenTableService genTableService;
    private final AiRuntimeConfig aiRuntimeConfig;
    private final AiChatFacade aiChatFacade;
    private final PageSpecCompiler pageSpecCompiler;
    private final PageSpecNormalizer pageSpecNormalizer;
    private final PageSpecAiRefiner pageSpecAiRefiner;
    private final I18nMessageResolver i18n;

    public GenPageSpecServiceImpl(GenTableService genTableService,
                                  AiRuntimeConfig aiRuntimeConfig,
                                  AiChatFacade aiChatFacade,
                                  PageSpecCompiler pageSpecCompiler,
                                  PageSpecNormalizer pageSpecNormalizer,
                                  PageSpecAiRefiner pageSpecAiRefiner,
                                  I18nMessageResolver i18n) {
        this.genTableService = genTableService;
        this.aiRuntimeConfig = aiRuntimeConfig;
        this.aiChatFacade = aiChatFacade;
        this.pageSpecCompiler = pageSpecCompiler;
        this.pageSpecNormalizer = pageSpecNormalizer;
        this.pageSpecAiRefiner = pageSpecAiRefiner;
        this.i18n = i18n;
    }

    @Override
    public String getPromptTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("gen-spec/gen-page-spec.prompt.md");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BusinessException("error.ai.prompt_load_failed", ex.getMessage());
        }
    }

    @Override
    public String generate(GenPageSpecGenerateRequest request) {
        if (!aiRuntimeConfig.isEnabled() || !StringUtils.hasText(aiRuntimeConfig.getApiKey())) {
            throw new BusinessException("error.ai.not_enabled");
        }

        String columnsText = resolveColumnsText(request);
        String template = getPromptTemplate();
        String systemPrompt = template
                .replace("{{USER_PROMPT}}", StringUtils.hasText(request.getUserPrompt()) ? request.getUserPrompt() : "(无额外说明)")
                .replace("{{DDL_OR_COLUMNS}}", columnsText);

        String userMessage = """
                请为 pageKey=%s、layout=%s 生成完整的 page-spec.yaml。
                必须包含顶层字段：specVersion、pageKey、layout、meta、entity、fields。
                meta 必须含 apiModule、backendModule、permissionPrefix、className。
                若有特殊行操作/详情弹窗，写在 extensions（只能引用已注册积木，如 GenUserRoleDialog、GenNoticeDetailDialog）；
                标准 CRUD 无特殊交互时可省略 extensions。
                只输出 YAML 正文，可用 ```yaml 代码块包裹。
                """.formatted(request.getPageKey().trim(), request.getLayout().trim());

        String raw = aiChatFacade.chat(aiRuntimeConfig, systemPrompt, userMessage, AiInvokeBizType.PAGE_SPEC_GENERATE)
                .getContent();
        String yaml = extractYaml(raw);
        PageSpecNormalizer.NormalizeResult normalized = pageSpecNormalizer.normalize(yaml, request.getTableId());
        return normalized.getYamlContent();
    }

    @Override
    public GenPageSpecValidateResultVO validate(GenPageSpecValidateRequest request) {
        PageSpecNormalizer.NormalizeResult normalized = pageSpecNormalizer.normalize(
                request.getYamlContent(), request.getTableId());
        GenPageSpecValidateResultVO result = validateYaml(normalized.getYamlContent());
        if (!normalized.getWarnings().isEmpty()) {
            result.getWarnings().addAll(normalized.getWarnings());
        }
        if (!normalized.getYamlContent().equals(request.getYamlContent())) {
            result.setNormalizedYaml(normalized.getYamlContent());
        }
        return result;
    }

    @Override
    public GenPageSpecCompileResultVO compile(GenPageSpecCompileRequest request) {
        PageSpecNormalizer.NormalizeResult normalized = pageSpecNormalizer.normalize(
                request.getYamlContent(), request.getTableId());
        GenPageSpecValidateResultVO validation = validateYaml(normalized.getYamlContent());
        if (!validation.isValid()) {
            throw new BusinessException("error.gen.page_spec.validate_failed",
                    String.join("; ", validation.getErrors()));
        }
        return pageSpecCompiler.compile(normalized.getYamlContent());
    }

    @Override
    public GenPageSpecDraftVO draftFromTable(String tableId, String pageKey, String layout) {
        GenTable table = requireTable(tableId);
        return applyDefaultSummary(PageSpecDraftBuilder.fromTable(table, pageKey, layout), table.getTableName());
    }

    @Override
    public GenPageSpecDraftVO analyze(GenPageSpecGenerateRequest request) {
        GenTable table = requireTable(request.getTableId());
        GenPageSpecDraftVO baseline = applyDefaultSummary(
                PageSpecDraftBuilder.fromTable(
                table,
                StringUtils.hasText(request.getPageKey()) ? request.getPageKey() : table.getBusinessName(),
                StringUtils.hasText(request.getLayout()) ? request.getLayout() : "standard-crud"),
                table.getTableName());
        if (!StringUtils.hasText(request.getUserPrompt())) {
            return baseline;
        }
        return pageSpecAiRefiner.refine(baseline, request.getUserPrompt());
    }

    private GenPageSpecDraftVO applyDefaultSummary(GenPageSpecDraftVO draft, String tableName) {
        draft.setSummary(resolvePageSpecDefaultSummary(tableName));
        return draft;
    }

    private String resolvePageSpecDefaultSummary(String tableName) {
        String locale = LocaleResolver.normalize(LocaleContextHolder.getLocale().toLanguageTag());
        String template = GenClasspathBaselineLoader.loadI18nJson("page_spec", locale)
                .map(json -> GenI18nBuilder.readPageText(json, "defaultSummary"))
                .filter(StringUtils::hasText)
                .orElseGet(() -> GenClasspathBaselineLoader.loadI18nJson("page_spec", LocaleResolver.LOCALE_ZH_CN)
                        .map(json -> GenI18nBuilder.readPageText(json, "defaultSummary"))
                        .orElse(""));
        if (!StringUtils.hasText(template)) {
            return tableName;
        }
        return MessageFormat.format(template, tableName);
    }

    @Override
    public GenPageSpecCompileResultVO publish(GenPageSpecPublishRequest request) {
        String yaml = PageSpecYamlAssembler.toYaml(request.getDraft());
        GenPageSpecCompileRequest compileRequest = new GenPageSpecCompileRequest();
        compileRequest.setYamlContent(yaml);
        compileRequest.setTableId(request.getTableId());
        return compile(compileRequest);
    }

    private GenTable requireTable(String tableId) {
        if (!StringUtils.hasText(tableId)) {
            throw new BusinessException("error.gen.page_spec.table_required");
        }
        GenTable table = genTableService.selectGenTableById(tableId);
        if (table == null) {
            throw new BusinessException("error.gen.page_spec.table_required");
        }
        return table;
    }

    @SuppressWarnings("unchecked")
    private GenPageSpecValidateResultVO validateYaml(String yamlContent) {
        GenPageSpecValidateResultVO result = new GenPageSpecValidateResultVO();
        result.setValid(true);

        Map<String, Object> doc;
        try {
            Object loaded = new Yaml().load(yamlContent);
            if (!(loaded instanceof Map)) {
                result.setValid(false);
                result.getErrors().add(i18n.resolve("error.gen.page_spec.yaml_root_not_mapping"));
                return result;
            }
            doc = (Map<String, Object>) loaded;
        } catch (Exception ex) {
            result.setValid(false);
            result.getErrors().add("invalid YAML: " + ex.getMessage());
            return result;
        }

        requireField(doc, "specVersion", result);
        requireField(doc, "pageKey", result);
        requireField(doc, "layout", result);
        requireField(doc, "meta", result);

        Object layout = doc.get("layout");
        if (layout != null && !List.of("standard-crud", "tree-crud", "master-detail", "region-org").contains(String.valueOf(layout))) {
            result.setValid(false);
            result.getErrors().add("layout must be standard-crud | tree-crud | master-detail | region-org");
        }

        if ("master-detail".equals(String.valueOf(layout))) {
            if (!(doc.get("masterDetail") instanceof Map)) {
                result.setValid(false);
                result.getErrors().add("master-detail layout requires masterDetail block");
            }
        } else if ("region-org".equals(String.valueOf(layout))) {
            result.getWarnings().add("region-org layout is hand-crafted; compile-page-spec may not fully generate it");
        } else if (layout != null) {
            if (!doc.containsKey("entity")) {
                result.getWarnings().add("single-table layout should include entity block");
            }
            if (!doc.containsKey("fields")) {
                result.getWarnings().add("single-table layout should include fields list");
            }
        }

        validateExtensions(doc.get("extensions"), result);

        for (String schemaError : PageSpecJsonSchemaValidator.validatePageSpec(doc)) {
            result.setValid(false);
            result.getErrors().add(schemaError);
        }

        result.setValid(result.getErrors().isEmpty());
        return result;
    }

    @SuppressWarnings("unchecked")
    private void validateExtensions(Object extensionsObj, GenPageSpecValidateResultVO result) {
        if (extensionsObj == null) {
            return;
        }
        if (!(extensionsObj instanceof Map<?, ?>)) {
            result.setValid(false);
            result.getErrors().add("extensions must be a mapping");
            return;
        }
        Map<String, Object> extensions = (Map<String, Object>) extensionsObj;
        Object rowActions = extensions.get("rowActions");
        if (rowActions != null && !(rowActions instanceof List<?>)) {
            result.setValid(false);
            result.getErrors().add("extensions.rowActions must be a list");
        } else if (rowActions instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String) {
                    continue;
                }
                if (!(item instanceof Map<?, ?> action)) {
                    result.setValid(false);
                    result.getErrors().add("extensions.rowActions items must be string or object");
                    continue;
                }
                Object dialog = action.get("dialog");
                if (dialog != null && !KNOWN_DIALOG_WIDGETS.contains(String.valueOf(dialog))) {
                    result.getWarnings().add("unknown dialog brick id (register first): " + dialog);
                }
                Object handler = action.get("handler");
                if (handler != null && !String.valueOf(handler).contains(".")) {
                    result.getWarnings().add("handler should be namespaced id (e.g. user.resetPassword): " + handler);
                }
            }
        }
        Object dialogs = extensions.get("dialogs");
        if (dialogs != null && !(dialogs instanceof List<?>)) {
            result.setValid(false);
            result.getErrors().add("extensions.dialogs must be a list");
        } else if (dialogs instanceof List<?> list) {
            for (Object id : list) {
                if (!KNOWN_DIALOG_WIDGETS.contains(String.valueOf(id))) {
                    result.getWarnings().add("unknown dialog brick id (register first): " + id);
                }
            }
        }
        Object toolbarActions = extensions.get("toolbarActions");
        if (toolbarActions != null && !(toolbarActions instanceof List<?>)) {
            result.setValid(false);
            result.getErrors().add("extensions.toolbarActions must be a list");
        } else if (toolbarActions instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String s) {
                    if ("importTemplate".equals(s)) {
                        result.getWarnings().add(
                                "toolbarActions \"importTemplate\" is deprecated; use \"import\" (template download is inside the dialog)");
                    } else if (!"export".equals(s) && !"import".equals(s)) {
                        result.getWarnings().add(
                                "unknown toolbarActions string id (prefer object with builtin export|import): " + s);
                    }
                    continue;
                }
                if (!(item instanceof Map<?, ?> action)) {
                    result.setValid(false);
                    result.getErrors().add("extensions.toolbarActions items must be string or object");
                    continue;
                }
                Object builtin = action.get("builtin");
                if (builtin != null) {
                    String b = String.valueOf(builtin);
                    if ("importTemplate".equals(b)) {
                        result.getWarnings().add(
                                "toolbarActions.builtin importTemplate is deprecated; use import");
                    } else if (!"export".equals(b) && !"import".equals(b) && !"batchDelete".equals(b)) {
                        result.getWarnings().add(
                                "unknown toolbarActions.builtin (supported: export|import|batchDelete): " + builtin);
                    }
                }
            }
        }
    }

    private String resolveColumnsText(GenPageSpecGenerateRequest request) {
        if (StringUtils.hasText(request.getColumnsText())) {
            return request.getColumnsText().trim();
        }
        if (!StringUtils.hasText(request.getTableId())) {
            throw new BusinessException("error.gen.page_spec.table_required");
        }
        GenTable table = genTableService.selectGenTableById(request.getTableId());
        if (table == null) {
            throw new BusinessException("error.gen.page_spec.table_required");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("tableName: ").append(table.getTableName()).append('\n');
        sb.append("tableComment: ").append(StringUtils.hasText(table.getTableComment()) ? table.getTableComment() : "").append('\n');
        sb.append("businessName: ").append(table.getBusinessName()).append('\n');
        sb.append("className: ").append(table.getClassName()).append('\n');
        sb.append("apiModule: ").append(table.getApiModule()).append('\n');
        sb.append("backendModule: ").append(table.getBackendModule()).append('\n');
        sb.append("permissionPrefix: ").append(table.getPermissionPrefix()).append('\n');
        sb.append("className: ").append(table.getClassName()).append('\n');
        sb.append("suggestedMeta:\n");
        sb.append("  apiModule: ").append(table.getApiModule()).append('\n');
        sb.append("  backendModule: ").append(table.getBackendModule()).append('\n');
        sb.append("  permissionPrefix: ").append(table.getPermissionPrefix()).append('\n');
        sb.append("  className: ").append(table.getClassName()).append('\n');
        sb.append("columns:\n");
        List<GenTableColumn> columns = table.getColumns();
        if (columns != null) {
            for (GenTableColumn col : columns) {
                sb.append("  - javaField: ").append(col.getJavaField())
                        .append(", javaType: ").append(col.getJavaType())
                        .append(", columnComment: ").append(nullToEmpty(col.getColumnComment()))
                        .append(", isQuery: ").append(col.isQuery())
                        .append(", queryType: ").append(nullToEmpty(col.getQueryType()))
                        .append(", isList: ").append(col.isList())
                        .append(", isExport: ").append(col.isExport())
                        .append(", isImport: ").append(col.isImport())
                        .append(", isInsert: ").append(col.isInsert())
                        .append(", isEdit: ").append(col.isEdit())
                        .append(", htmlType: ").append(nullToEmpty(col.getHtmlType()))
                        .append(", dictType: ").append(nullToEmpty(col.getDictType()))
                        .append('\n');
            }
        }
        return sb.toString();
    }

    private static void requireField(Map<String, Object> doc, String key, GenPageSpecValidateResultVO result) {
        Object value = doc.get(key);
        if (value == null || (value instanceof String s && !StringUtils.hasText(s))) {
            result.setValid(false);
            result.getErrors().add("missing required field: " + key);
        }
    }

    private static String extractYaml(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new BusinessException("error.ai.empty_response");
        }
        Matcher m = FENCE_YAML.matcher(raw);
        if (m.find()) {
            return m.group(1).trim();
        }
        return raw.trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
