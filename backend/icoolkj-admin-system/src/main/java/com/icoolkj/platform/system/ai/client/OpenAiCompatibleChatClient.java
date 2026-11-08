package com.icoolkj.platform.system.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.system.ai.dto.AiChatMessageItem;
import com.icoolkj.platform.system.ai.protocol.AiProtocols;
import com.icoolkj.platform.system.ai.protocol.openai.OpenAiCompatibleEndpoints;
import com.icoolkj.platform.system.ai.protocol.openai.OpenAiCompatibleParamMerger;
import com.icoolkj.platform.system.ai.service.AiRuntimeConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Component
public class OpenAiCompatibleChatClient {

    private final ObjectMapper objectMapper;

    public OpenAiCompatibleChatClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String chat(AiRuntimeConfig config, String systemPrompt, String userMessage) {
        if (StringUtils.hasText(systemPrompt)) {
            return chatMessages(config, List.of(
                    new AiChatMessageItem("system", systemPrompt),
                    new AiChatMessageItem("user", userMessage)
            ));
        }
        return chatMessages(config, List.of(new AiChatMessageItem("user", userMessage)));
    }

    public String chatMessages(AiRuntimeConfig config, List<AiChatMessageItem> messageItems) {
        ArrayNode messages = objectMapper.createArrayNode();
        if (messageItems != null) {
            for (AiChatMessageItem item : messageItems) {
                if (item == null || !StringUtils.hasText(item.getRole()) || !StringUtils.hasText(item.getContent())) {
                    continue;
                }
                ObjectNode node = messages.addObject();
                node.put("role", item.getRole().trim());
                node.put("content", item.getContent());
            }
        }
        OpenAiCompletionResult result = complete(config, messages, null);
        if (!StringUtils.hasText(result.getContent())) {
            throw new BusinessException("error.ai.empty_response");
        }
        return result.getContent().trim();
    }

    public OpenAiCompletionResult complete(AiRuntimeConfig config, ArrayNode messages, ArrayNode tools) {
        if (!config.isEnabled()) {
            throw new BusinessException("error.ai.not_enabled");
        }
        if (!StringUtils.hasText(config.getApiKey())) {
            throw new BusinessException("error.ai.api_key_missing");
        }
        if (messages == null || messages.isEmpty()) {
            throw new BusinessException("error.ai.empty_request");
        }
        AiProtocols.requireSupported(config.getProtocol());
        String url = OpenAiCompatibleEndpoints.absoluteUrl(
                config.getBaseUrl(), OpenAiCompatibleEndpoints.CHAT_COMPLETIONS);

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", config.getModel());
            body.put("stream", false);
            body.set("messages", messages);
            OpenAiCompatibleParamMerger.mergeChatDefaults(
                    objectMapper, body, config.getDefaultParamsJson());
            if (tools != null && !tools.isEmpty()) {
                body.set("tools", tools);
                body.put("tool_choice", "auto");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(Math.max(5, config.getTimeoutSeconds())))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.getApiKey().trim())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("error.ai.http_error", String.valueOf(response.statusCode()), truncate(response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choice = root.path("choices").path(0);
            JsonNode message = choice.path("message");
            OpenAiCompletionResult result = new OpenAiCompletionResult();
            result.setFinishReason(choice.path("finish_reason").asText(null));
            if (message.hasNonNull("content") && StringUtils.hasText(message.get("content").asText())) {
                result.setContent(message.get("content").asText().trim());
            }
            result.setRawAssistantMessage(message.deepCopy());
            JsonNode toolCalls = message.path("tool_calls");
            if (toolCalls.isArray()) {
                for (JsonNode call : toolCalls) {
                    String id = call.path("id").asText("");
                    String name = call.path("function").path("name").asText("");
                    String args = call.path("function").path("arguments").asText("{}");
                    if (StringUtils.hasText(id) && StringUtils.hasText(name)) {
                        result.getToolCalls().add(new OpenAiCompletionResult.ToolCall(id, name, args));
                    }
                }
            }
            if (!StringUtils.hasText(result.getContent()) && !result.hasToolCalls()) {
                throw new BusinessException("error.ai.empty_response");
            }
            return result;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("error.ai.request_failed", ex.getMessage());
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private static String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > 200 ? body.substring(0, 200) + "…" : body;
    }
}
