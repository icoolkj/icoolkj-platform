package com.icoolkj.platform.system.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.system.ai.protocol.AiProtocols;
import com.icoolkj.platform.system.ai.protocol.openai.OpenAiCompatibleEndpoints;
import com.icoolkj.platform.system.ai.service.AiRuntimeConfig;
import com.icoolkj.platform.system.ai.support.AiResolvedRuntime;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Component
public class OpenAiCompatibleImageClient {

    private static final String DEFAULT_SIZE = "1024x1024";
    private static final String DEFAULT_RESPONSE_FORMAT = "url";
    private static final int MAX_N = 6;

    private final ObjectMapper objectMapper;

    public OpenAiCompatibleImageClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OpenAiImageResult generate(AiRuntimeConfig config, String prompt) {
        List<OpenAiImageResult> list = generateAll(config, prompt, null);
        return list.isEmpty() ? null : list.get(0);
    }

    public OpenAiImageResult generate(AiRuntimeConfig config, String prompt, String defaultParamsJson) {
        OpenAiImageGenerateOptions options = new OpenAiImageGenerateOptions();
        options.setDefaultParamsJson(defaultParamsJson);
        List<OpenAiImageResult> list = generateAll(config, prompt, options);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<OpenAiImageResult> generateAll(
            AiRuntimeConfig config, String prompt, OpenAiImageGenerateOptions options) {
        if (!config.isEnabled()) {
            throw new BusinessException("error.ai.not_enabled");
        }
        if (!StringUtils.hasText(config.getApiKey())) {
            throw new BusinessException("error.ai.api_key_missing");
        }
        if (!StringUtils.hasText(prompt)) {
            throw new BusinessException("error.ai.image_prompt_required");
        }

        ResolvedParams params = resolveParams(config, options);
        AiProtocols.requireSupported(config.getProtocol());
        String url = OpenAiCompatibleEndpoints.absoluteUrl(
                config.getBaseUrl(), OpenAiCompatibleEndpoints.IMAGES_GENERATIONS);

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", config.getModel());
            body.put("prompt", prompt.trim());
            body.put("n", params.n);
            body.put("size", params.size);
            body.put("response_format", params.responseFormat);
            if (StringUtils.hasText(params.negativePrompt)) {
                body.put("negative_prompt", params.negativePrompt);
            }
            if (params.promptExtend != null) {
                body.put("prompt_extend", params.promptExtend);
            }
            if (params.watermark != null) {
                body.put("watermark", params.watermark);
            }

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(Math.max(30, config.getTimeoutSeconds())))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.getApiKey().trim())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                if (response.statusCode() == 404) {
                    throw new BusinessException("error.ai.image_endpoint_not_found", url);
                }
                throw new BusinessException(
                        "error.ai.http_error",
                        String.valueOf(response.statusCode()),
                        truncate(response.body()));
            }

            JsonNode data = objectMapper.readTree(response.body()).path("data");
            if (!data.isArray() || data.isEmpty()) {
                throw new BusinessException("error.ai.image_empty_response");
            }

            List<OpenAiImageResult> results = new ArrayList<>();
            int index = 0;
            for (JsonNode item : data) {
                byte[] bytes = decodeImageBytes(client, item, config.getTimeoutSeconds());
                if (bytes.length == 0) {
                    continue;
                }
                OpenAiImageResult result = new OpenAiImageResult();
                result.setContent(bytes);
                result.setContentType(detectContentType(bytes));
                result.setFilename(
                        "ai-image-" + System.currentTimeMillis() + "-" + (index++)
                                + extensionFor(result.getContentType()));
                results.add(result);
            }
            if (results.isEmpty()) {
                throw new BusinessException("error.ai.image_empty_response");
            }
            return results;
        } catch (BusinessException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("error.ai.image_decode_failed");
        } catch (Exception ex) {
            throw new BusinessException("error.ai.image_request_failed", ex.getMessage());
        }
    }

    private ResolvedParams resolveParams(AiRuntimeConfig config, OpenAiImageGenerateOptions options) {
        ResolvedParams params = new ResolvedParams();
        params.size = DEFAULT_SIZE;
        params.n = 1;
        params.responseFormat = DEFAULT_RESPONSE_FORMAT;

        String paramsJson = options != null ? options.getDefaultParamsJson() : null;
        if (!StringUtils.hasText(paramsJson) && config instanceof AiResolvedRuntime resolved) {
            paramsJson = resolved.getDefaultParamsJson();
        }
        if (StringUtils.hasText(paramsJson)) {
            try {
                JsonNode node = objectMapper.readTree(paramsJson);
                applyJsonParams(params, node);
            } catch (Exception ignored) {
            }
        }
        if (options != null) {
            if (StringUtils.hasText(options.getSize())) {
                params.size = options.getSize().trim();
            }
            if (options.getN() != null) {
                params.n = clampN(options.getN());
            }
            if (options.getNegativePrompt() != null) {
                params.negativePrompt = options.getNegativePrompt().trim();
            }
            if (StringUtils.hasText(options.getResponseFormat())) {
                params.responseFormat = normalizeResponseFormat(options.getResponseFormat());
            }
            if (options.getPromptExtend() != null) {
                params.promptExtend = options.getPromptExtend();
            }
            if (options.getWatermark() != null) {
                params.watermark = options.getWatermark();
            }
        }
        params.n = clampN(params.n);
        params.responseFormat = normalizeResponseFormat(params.responseFormat);
        return params;
    }

    private static void applyJsonParams(ResolvedParams params, JsonNode node) {
        if (node.hasNonNull("size") && StringUtils.hasText(node.get("size").asText())) {
            params.size = node.get("size").asText().trim();
        }
        if (node.has("n") && node.get("n").canConvertToInt()) {
            params.n = clampN(node.get("n").asInt(1));
        }
        if (node.hasNonNull("negative_prompt")) {
            params.negativePrompt = node.get("negative_prompt").asText("").trim();
        }
        if (node.hasNonNull("response_format") && StringUtils.hasText(node.get("response_format").asText())) {
            params.responseFormat = normalizeResponseFormat(node.get("response_format").asText());
        }
        if (node.has("prompt_extend") && node.get("prompt_extend").isBoolean()) {
            params.promptExtend = node.get("prompt_extend").asBoolean();
        }
        if (node.has("watermark") && node.get("watermark").isBoolean()) {
            params.watermark = node.get("watermark").asBoolean();
        }
    }

    private byte[] decodeImageBytes(HttpClient client, JsonNode item, int timeoutSeconds) throws Exception {
        String b64 = item.path("b64_json").asText(null);
        if (StringUtils.hasText(b64)) {
            return Base64.getDecoder().decode(b64.trim());
        }
        String imageUrl = item.path("url").asText(null);
        if (!StringUtils.hasText(imageUrl)) {
            return new byte[0];
        }
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl.trim()))
                .timeout(Duration.ofSeconds(Math.max(30, timeoutSeconds)))
                .GET()
                .build();
        HttpResponse<byte[]> imageResponse = client.send(get, HttpResponse.BodyHandlers.ofByteArray());
        if (imageResponse.statusCode() < 200 || imageResponse.statusCode() >= 300) {
            throw new BusinessException(
                    "error.ai.http_error",
                    String.valueOf(imageResponse.statusCode()),
                    "download image url failed");
        }
        byte[] bytes = imageResponse.body();
        return bytes != null ? bytes : new byte[0];
    }

    private static int clampN(int n) {
        return Math.max(1, Math.min(MAX_N, n));
    }

    private static String normalizeResponseFormat(String format) {
        if (!StringUtils.hasText(format)) {
            return DEFAULT_RESPONSE_FORMAT;
        }
        String value = format.trim().toLowerCase(Locale.ROOT);
        if ("b64_json".equals(value) || "b64".equals(value) || "base64".equals(value)) {
            return "b64_json";
        }
        return "url";
    }

    private static String detectContentType(byte[] bytes) {
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47) {
            return "image/png";
        }
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (bytes.length >= 6) {
            String header = new String(bytes, 0, 6);
            if ("GIF87a".equals(header) || "GIF89a".equals(header)) {
                return "image/gif";
            }
        }
        if (bytes.length >= 12) {
            String riff = new String(bytes, 0, 4);
            String webp = new String(bytes, 8, 4);
            if ("RIFF".equals(riff) && "WEBP".equals(webp)) {
                return "image/webp";
            }
        }
        return "image/png";
    }

    private static String extensionFor(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return ".png";
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".png";
        };
    }

    private static String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > 200 ? body.substring(0, 200) + "…" : body;
    }

    private static final class ResolvedParams {
        private String size;
        private int n;
        private String negativePrompt;
        private String responseFormat;
        private Boolean promptExtend;
        private Boolean watermark;
    }
}
