package com.icoolkj.platform.oa.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.oa.dto.OaFieldDefItemRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.util.StringUtils;

/**
 * Layout Persistence Gate (presentationVersion 2): reference-shape layout_json only.
 * Forbidden contract keys must not be stored; fieldCodes ⊆ field pool.
 * Span SoT is 1–24; v1 packages are upgraded once on normalize (no runtime guessing).
 */
public final class OaLayoutJsonValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int PRESENTATION_VERSION = 2;
    private static final Set<String> FORBIDDEN_FIELD_KEYS = Set.of(
            "fieldType",
            "componentType",
            "dictType",
            "required",
            "fieldRequired",
            "maxLength",
            "defaultValue",
            "platformLocked",
            "fieldPerm",
            "fieldName"
    );

    private OaLayoutJsonValidator() {
    }

    /**
     * Validate and normalize layout JSON to presentationVersion 2 reference shape.
     *
     * @return normalized JSON string, or null when input blank
     */
    public static String validateAndNormalize(String layoutJson, List<OaFieldDefItemRequest> fields) {
        if (!StringUtils.hasText(layoutJson)) {
            return null;
        }
        Set<String> pool = new HashSet<>();
        if (fields != null) {
            for (OaFieldDefItemRequest f : fields) {
                if (f != null && StringUtils.hasText(f.getFieldCode())) {
                    pool.add(f.getFieldCode().trim());
                }
            }
        }
        try {
            JsonNode root = MAPPER.readTree(layoutJson.trim());
            if (!root.isObject()) {
                throw new BusinessException("error.oa.layout_json_invalid");
            }
            ObjectNode obj = (ObjectNode) root;
            if (!obj.has("schemaVersion") || obj.get("schemaVersion").asInt(-1) != 1) {
                throw new BusinessException("error.oa.layout_schema_unsupported");
            }
            if (!obj.has("nodes") || !obj.get("nodes").isArray()) {
                throw new BusinessException("error.oa.layout_json_invalid");
            }
            boolean fromV1 = obj.path("presentationVersion").asInt(0) != PRESENTATION_VERSION;
            Set<String> seen = new HashSet<>();
            ArrayNode outNodes = MAPPER.createArrayNode();
            for (JsonNode node : obj.get("nodes")) {
                if (node == null || !node.isObject()) {
                    continue;
                }
                JsonNode normalized = normalizeNode((ObjectNode) node, pool, seen, fromV1);
                if (normalized != null) {
                    outNodes.add(normalized);
                }
            }
            ObjectNode out = MAPPER.createObjectNode();
            out.put("schemaVersion", 1);
            out.put("presentationVersion", PRESENTATION_VERSION);
            ObjectNode form = MAPPER.createObjectNode();
            JsonNode formIn = obj.get("form");
            if (formIn != null && formIn.isObject()) {
                if (formIn.has("labelWidth")) {
                    form.put("labelWidth", formIn.get("labelWidth").asInt(100));
                } else {
                    form.put("labelWidth", 100);
                }
                String pos = formIn.path("labelPosition").asText("left");
                form.put("labelPosition", "top".equals(pos) ? "top" : "left");
                String size = formIn.path("size").asText("default");
                if ("small".equals(size) || "large".equals(size)) {
                    form.put("size", size);
                } else {
                    form.put("size", "default");
                }
                String align = formIn.path("labelAlign").asText("right");
                form.put("labelAlign", "left".equals(align) ? "left" : "right");
                form.put("hideRequiredAsterisk", formIn.path("hideRequiredAsterisk").asBoolean(false));
            } else {
                form.put("labelWidth", 100);
                form.put("labelPosition", "left");
                form.put("size", "default");
                form.put("labelAlign", "right");
                form.put("hideRequiredAsterisk", false);
            }
            out.set("form", form);
            out.set("nodes", outNodes);
            return MAPPER.writeValueAsString(out);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("error.oa.layout_json_invalid");
        }
    }

    private static JsonNode normalizeNode(
            ObjectNode node, Set<String> pool, Set<String> seen, boolean fromV1) {
        String type = node.path("type").asText("");
        String id = node.path("id").asText("");
        if (!StringUtils.hasText(id)) {
            id = "n_" + Integer.toHexString(System.identityHashCode(node));
        }
        return switch (type) {
            case "divider" -> {
                ObjectNode out = MAPPER.createObjectNode();
                out.put("type", "divider");
                out.put("id", id);
                if (node.hasNonNull("title")) {
                    out.put("title", node.get("title").asText());
                }
                yield out;
            }
            case "field" -> {
                String code = extractFieldCode(node);
                if (!StringUtils.hasText(code)) {
                    yield null;
                }
                assertPoolCode(code, pool, seen);
                ObjectNode src = node.has("field") && node.get("field").isObject()
                        ? (ObjectNode) node.get("field")
                        : node;
                yield buildFieldNode(id, code, src, fromV1, true);
            }
            case "row", "card" -> {
                ArrayNode childrenIn = node.has("children") && node.get("children").isArray()
                        ? (ArrayNode) node.get("children")
                        : MAPPER.createArrayNode();
                ArrayNode childrenOut = MAPPER.createArrayNode();
                for (JsonNode child : childrenIn) {
                    if (child == null || !child.isObject()) {
                        continue;
                    }
                    ObjectNode c = (ObjectNode) child;
                    rejectForbiddenKeys(c);
                    String code = c.path("fieldCode").asText("");
                    if (!StringUtils.hasText(code)) {
                        continue;
                    }
                    assertPoolCode(code, pool, seen);
                    childrenOut.add(buildPresentationRef(c, code, fromV1));
                }
                if ("row".equals(type) && childrenOut.isEmpty()) {
                    yield null;
                }
                ObjectNode out = MAPPER.createObjectNode();
                out.put("type", type);
                out.put("id", id);
                if ("card".equals(type) && node.hasNonNull("title")) {
                    out.put("title", node.get("title").asText());
                }
                out.set("children", childrenOut);
                yield out;
            }
            default -> null;
        };
    }

    private static String extractFieldCode(ObjectNode node) {
        if (node.has("field") && node.get("field").isObject()) {
            ObjectNode field = (ObjectNode) node.get("field");
            rejectForbiddenKeys(field);
            return field.path("fieldCode").asText("");
        }
        rejectForbiddenKeys(node);
        return node.path("fieldCode").asText("");
    }

    private static void rejectForbiddenKeys(ObjectNode node) {
        for (String key : FORBIDDEN_FIELD_KEYS) {
            if (node.has(key)) {
                node.remove(key);
            }
        }
        if (node.has("field") && node.get("field").isObject()) {
            ObjectNode field = (ObjectNode) node.get("field");
            for (String key : FORBIDDEN_FIELD_KEYS) {
                field.remove(key);
            }
        }
        // Never persist colSpan after v2
        node.remove("colSpan");
    }

    private static void assertPoolCode(String code, Set<String> pool, Set<String> seen) {
        if (!pool.contains(code)) {
            throw new BusinessException("error.oa.layout_unknown_field", code);
        }
        if (!seen.add(code)) {
            throw new BusinessException("error.oa.layout_duplicate_field", code);
        }
    }

    private static ObjectNode buildFieldNode(
            String id, String code, ObjectNode src, boolean fromV1, boolean root) {
        ObjectNode out = MAPPER.createObjectNode();
        out.put("type", "field");
        out.put("id", id);
        out.put("fieldCode", code);
        out.put("span", resolveSpan(src, fromV1, root));
        if (src.hasNonNull("placeholder")) {
            out.put("placeholder", src.get("placeholder").asText());
        }
        return out;
    }

    private static ObjectNode buildPresentationRef(ObjectNode src, String code, boolean fromV1) {
        ObjectNode out = MAPPER.createObjectNode();
        out.put("fieldCode", code);
        out.put("span", resolveSpan(src, fromV1, false));
        if (src.hasNonNull("placeholder")) {
            out.put("placeholder", src.get("placeholder").asText());
        }
        return out;
    }

    /**
     * v2 hot path: clamp span 1–24 (missing → root 24 / child 8).
     * v1 upgrade: colSpan→span thirds; span 1|2|3→thirds; root span==8→24.
     */
    private static int resolveSpan(ObjectNode src, boolean fromV1, boolean root) {
        boolean hasSpan = src.has("span") && src.get("span").isNumber();
        boolean hasCol = src.has("colSpan") && src.get("colSpan").isNumber();
        if (!fromV1) {
            if (hasSpan) {
                return clampSpan(src.get("span").asInt(root ? 24 : 8));
            }
            return root ? 24 : 8;
        }
        if (hasSpan) {
            int n = src.get("span").asInt(root ? 24 : 8);
            if (n == 1 || n == 2 || n == 3) {
                return legacyThirds(n);
            }
            int span = clampSpan(n);
            if (root && span == 8) {
                return 24;
            }
            return span;
        }
        if (hasCol) {
            int span = legacyThirds(src.get("colSpan").asInt(1));
            if (root && span == 8) {
                return 24;
            }
            return span;
        }
        return root ? 24 : 8;
    }

    private static int legacyThirds(int col) {
        if (col == 3) {
            return 24;
        }
        if (col == 2) {
            return 16;
        }
        return 8;
    }

    private static int clampSpan(int span) {
        return Math.min(24, Math.max(1, span));
    }
}
