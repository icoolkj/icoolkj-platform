package com.icoolkj.platform.generator.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.constant.AiInvokeBizType;
import com.icoolkj.platform.generator.vo.GenPageSpecDraftVO;
import com.icoolkj.platform.generator.vo.GenPageSpecFieldDraftVO;
import com.icoolkj.platform.system.ai.service.AiRuntimeConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 用已启用的 chat 能力调整结构化方案 */
@Component
public class PageSpecAiRefiner {

    private static final Pattern FENCE_JSON = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AiRuntimeConfig aiRuntimeConfig;
    private final AiChatFacade aiChatFacade;

    public PageSpecAiRefiner(AiRuntimeConfig aiRuntimeConfig, AiChatFacade aiChatFacade) {
        this.aiRuntimeConfig = aiRuntimeConfig;
        this.aiChatFacade = aiChatFacade;
    }

    public GenPageSpecDraftVO refine(GenPageSpecDraftVO baseline, String userPrompt) {
        if (!aiRuntimeConfig.isEnabled() || StringUtils.isBlank(aiRuntimeConfig.getApiKey())) {
            throw new BusinessException("error.ai.not_enabled");
        }
        if (StringUtils.isBlank(userPrompt)) {
            return baseline;
        }

        String systemPrompt = """
                你是 icoolkj 低代码页面设计助手。根据用户需求调整页面方案 JSON。
                只输出 JSON，不要 Markdown 说明。JSON 结构：
                {
                  "summary": "一句话方案说明",
                  "pageKey": "camelCase",
                  "layout": "standard-crud|tree-crud",
                  "fields": [
                    { "javaField": "...", "isQuery": true, "queryType": "EQ|LIKE",
                      "isList": true, "isExport": true, "isImport": false, "isInsert": true, "isEdit": true,
                      "htmlType": "input|select|radio|textarea|datetime", "dictType": "..." }
                  ],
                  "extensions": {
                    "toolbarActions": ["export"],
                    "rowActions": [
                      { "id": "edit", "builtin": "edit", "permKey": "update" },
                      { "id": "delete", "builtin": "delete", "permKey": "delete", "type": "danger" }
                    ],
                    "dialogs": ["GenUserRoleDialog"]
                  }
                }
                规则：
                - 只能使用 baseline 里已有的 javaField，不要编造字段。
                - extensions 只能引用已注册积木（如 GenUserRoleDialog、GenNoticeDetailDialog、user.resetPassword）。
                - 不要编造未注册的 dialog/handler 名称；无特殊交互时可省略 extensions。
                """;

        String userMessage = "baseline JSON:\n" + toJsonSafe(baseline)
                + "\n\n用户需求：\n" + userPrompt.trim()
                + "\n\n请输出调整后的 JSON。";

        String raw = aiChatFacade.chat(aiRuntimeConfig, systemPrompt, userMessage, AiInvokeBizType.PAGE_SPEC_ANALYZE)
                .getContent();
        return mergeAiPatch(baseline, extractJson(raw));
    }

    private GenPageSpecDraftVO mergeAiPatch(GenPageSpecDraftVO baseline, String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root.hasNonNull("summary")) {
                baseline.setSummary(root.get("summary").asText());
            }
            if (root.hasNonNull("pageKey")) {
                baseline.setPageKey(root.get("pageKey").asText());
            }
            if (root.hasNonNull("layout")) {
                baseline.setLayout(root.get("layout").asText());
            }
            if (root.has("fields") && root.get("fields").isArray()) {
                Map<String, GenPageSpecFieldDraftVO> byField = new LinkedHashMap<>();
                for (GenPageSpecFieldDraftVO f : baseline.getFields()) {
                    byField.put(f.getJavaField(), f);
                }
                Iterator<JsonNode> it = root.get("fields").elements();
                while (it.hasNext()) {
                    JsonNode node = it.next();
                    String javaField = node.path("javaField").asText(null);
                    if (StringUtils.isBlank(javaField) || !byField.containsKey(javaField)) {
                        continue;
                    }
                    GenPageSpecFieldDraftVO target = byField.get(javaField);
                    applyFieldPatch(target, node);
                }
                baseline.setFields(new ArrayList<>(byField.values()));
            }
            if (root.has("extensions") && root.get("extensions").isObject()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> extensions = MAPPER.convertValue(root.get("extensions"), Map.class);
                baseline.setExtensions(extensions != null ? new LinkedHashMap<>(extensions) : new LinkedHashMap<>());
            }
            if (baseline.getEntity() != null && StringUtils.isNotBlank(baseline.getPageKey())) {
                baseline.getEntity().setBusinessName(baseline.getPageKey());
            }
            if (baseline.getMeta() != null && StringUtils.isNotBlank(baseline.getPageKey())
                    && StringUtils.isNotBlank(baseline.getMeta().getApiModule())) {
                baseline.getMeta().setPermissionPrefix(
                        baseline.getMeta().getApiModule() + ":" + baseline.getPageKey());
            }
            return baseline;
        } catch (Exception ex) {
            throw new BusinessException("error.gen.page_spec.ai_parse_failed", ex.getMessage());
        }
    }

    private static void applyFieldPatch(GenPageSpecFieldDraftVO target, JsonNode node) {
        if (node.has("isQuery")) {
            target.setIsQuery(node.get("isQuery").asBoolean());
        }
        if (node.has("queryType")) {
            target.setQueryType(node.get("queryType").asText());
        }
        if (node.has("isList")) {
            target.setIsList(node.get("isList").asBoolean());
        }
        if (node.has("isExport")) {
            target.setIsExport(node.get("isExport").asBoolean());
        }
        if (node.has("isImport")) {
            target.setIsImport(node.get("isImport").asBoolean());
        }
        if (node.has("isInsert")) {
            target.setIsInsert(node.get("isInsert").asBoolean());
        }
        if (node.has("isEdit")) {
            target.setIsEdit(node.get("isEdit").asBoolean());
        }
        if (node.has("htmlType")) {
            target.setHtmlType(node.get("htmlType").asText());
        }
        if (node.has("dictType")) {
            String dict = node.get("dictType").asText(null);
            target.setDictType(StringUtils.isBlank(dict) ? null : dict);
        }
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

    private static String toJsonSafe(GenPageSpecDraftVO draft) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(draft);
        } catch (Exception ex) {
            return draft.toString();
        }
    }
}
