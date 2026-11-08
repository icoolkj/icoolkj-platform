package com.icoolkj.platform.infra.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.infra.dto.InfraFileRefBinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 业务目录附件 assets_json ↔ Map / file_ref 绑定（可复用于任意 catalog 业务）。
 */
@Component
public class BizCatalogAssetsSupport {

    private final ObjectMapper objectMapper;

    public BizCatalogAssetsSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> parseAssets(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
            return map != null ? new LinkedHashMap<>(map) : new LinkedHashMap<>();
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    public String writeAssets(Map<String, Object> assets) {
        Map<String, Object> cleaned = cleanAssets(assets);
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(cleaned);
        } catch (Exception e) {
            throw new IllegalStateException("assets_json serialize failed", e);
        }
    }

    public Map<String, Object> cleanAssets(Map<String, Object> assets) {
        if (assets == null || assets.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : assets.entrySet()) {
            if (entry == null || !StringUtils.hasText(entry.getKey())) {
                continue;
            }
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof List<?> list) {
                List<String> ids = new ArrayList<>();
                for (Object item : list) {
                    if (item == null) {
                        continue;
                    }
                    String id = String.valueOf(item).trim();
                    if (StringUtils.hasText(id)) {
                        ids.add(id);
                    }
                }
                if (!ids.isEmpty()) {
                    out.put(entry.getKey().trim(), ids);
                }
            } else {
                String id = String.valueOf(value).trim();
                if (StringUtils.hasText(id)) {
                    out.put(entry.getKey().trim(), id);
                }
            }
        }
        return out;
    }

    /** 多文件槽位用 profileCode:fileId 作为 ref_field，保证 sync 唯一 */
    public List<InfraFileRefBinding> toBindings(Map<String, Object> assets) {
        Map<String, Object> cleaned = cleanAssets(assets);
        List<InfraFileRefBinding> bindings = new ArrayList<>();
        for (Map.Entry<String, Object> entry : cleaned.entrySet()) {
            String profileCode = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof List<?> list) {
                for (Object item : list) {
                    String fileId = String.valueOf(item).trim();
                    bindings.add(new InfraFileRefBinding(fileId, profileCode + ":" + fileId));
                }
            } else {
                String fileId = String.valueOf(value).trim();
                bindings.add(new InfraFileRefBinding(fileId, profileCode));
            }
        }
        return bindings;
    }

    public static int countSlotFiles(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof List<?> list) {
            int count = 0;
            for (Object item : list) {
                if (item != null && StringUtils.hasText(String.valueOf(item).trim())) {
                    count++;
                }
            }
            return count;
        }
        return StringUtils.hasText(String.valueOf(value).trim()) ? 1 : 0;
    }
}
