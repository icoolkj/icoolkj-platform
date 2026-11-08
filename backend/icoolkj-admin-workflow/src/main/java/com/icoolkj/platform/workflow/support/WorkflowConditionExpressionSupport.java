package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 从 design 条件表达式中提取流程变量标识符（供 OA 条件字段注入） */
public final class WorkflowConditionExpressionSupport {

    private static final Pattern IDENTIFIER = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
    private static final Set<String> RESERVED = Set.of(
            "true", "false", "null", "and", "or", "not", "eq", "ne", "lt", "le", "gt", "ge",
            "empty", "div", "mod", "instanceof");

    private WorkflowConditionExpressionSupport() {
    }

    public static Set<String> collectIdentifiers(List<WorkflowModelDesignNodeRequest> nodes) {
        Set<String> out = new LinkedHashSet<>();
        if (CollectionUtils.isEmpty(nodes)) {
            return out;
        }
        Deque<WorkflowModelDesignNodeRequest> q = new ArrayDeque<>(nodes);
        while (!q.isEmpty()) {
            WorkflowModelDesignNodeRequest n = q.poll();
            if (n == null) {
                continue;
            }
            extractFromExpression(n.getConditionExpression(), out);
            if (n.getBranches() != null) {
                for (var br : n.getBranches()) {
                    if (br == null) {
                        continue;
                    }
                    // 条件网关分支用 expression；节点自身可能带 conditionExpression
                    extractFromExpression(br.getExpression(), out);
                    if (br.getNodes() != null) {
                        q.addAll(br.getNodes());
                    }
                }
            }
        }
        return out;
    }

    public static void extractFromExpression(String expression, Set<String> out) {
        if (!StringUtils.hasText(expression) || out == null) {
            return;
        }
        Matcher m = IDENTIFIER.matcher(expression);
        while (m.find()) {
            String id = m.group(1);
            if (!StringUtils.hasText(id)) {
                continue;
            }
            String lower = id.toLowerCase(Locale.ROOT);
            if (RESERVED.contains(lower)) {
                continue;
            }
            out.add(id);
        }
    }
}
