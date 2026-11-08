package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.constant.WfFieldPerm;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.util.StringUtils;

/** 从设计节点 fieldPerms 解析字段权限 */
public final class WorkflowNodeFieldPermSupport {

    private WorkflowNodeFieldPermSupport() {
    }

    public static String resolve(WorkflowModelDesignNodeRequest node, String fieldCode) {
        if (!StringUtils.hasText(fieldCode) || node == null || node.getFieldPerms() == null
                || node.getFieldPerms().isEmpty()) {
            return WfFieldPerm.R;
        }
        String raw = node.getFieldPerms().get(fieldCode.trim());
        if (raw == null) {
            // 兼容大小写 key
            for (Map.Entry<String, String> e : node.getFieldPerms().entrySet()) {
                if (e.getKey() != null && e.getKey().equalsIgnoreCase(fieldCode.trim())) {
                    raw = e.getValue();
                    break;
                }
            }
        }
        return WfFieldPerm.normalize(raw);
    }

    public static Map<String, String> normalizeMap(Map<String, String> raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : raw.entrySet()) {
            if (!StringUtils.hasText(e.getKey())) {
                continue;
            }
            out.put(e.getKey().trim(), WfFieldPerm.normalize(e.getValue()));
        }
        return out;
    }

    /** 校验 map；非法值抛错用调用方 BusinessException */
    public static void assertValid(Map<String, String> raw) {
        if (raw == null || raw.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> e : raw.entrySet()) {
            if (!StringUtils.hasText(e.getKey())) {
                continue;
            }
            if (!WfFieldPerm.isValid(e.getValue())) {
                throw new IllegalArgumentException(
                        "invalid fieldPerm for " + e.getKey() + ": " + e.getValue());
            }
        }
    }

    public static Map<String, Object> filterWritableOnly(WorkflowModelDesignNodeRequest node,
                                                         Map<String, Object> submitted) {
        if (submitted == null || submitted.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : submitted.entrySet()) {
            if (!StringUtils.hasText(e.getKey())) {
                continue;
            }
            if (WfFieldPerm.isWritable(resolve(node, e.getKey()))) {
                out.put(e.getKey().trim(), e.getValue());
            }
        }
        return out;
    }

    @SuppressWarnings("unused")
    private static String upper(String s) {
        return s == null ? null : s.toUpperCase(Locale.ROOT);
    }
}
