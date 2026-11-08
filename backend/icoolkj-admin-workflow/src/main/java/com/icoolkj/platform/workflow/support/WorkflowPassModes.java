package com.icoolkj.platform.workflow.support;

import java.util.Locale;
import org.springframework.util.StringUtils;

/** approve.passMode 规范化与多实例判定（与 {@link LinearBpmnBuilder} 对齐）。 */
public final class WorkflowPassModes {

    private WorkflowPassModes() {
    }

    public static String normalize(String passMode) {
        if (!StringUtils.hasText(passMode)) {
            return LinearBpmnBuilder.PASS_SEQUENTIAL;
        }
        String v = passMode.trim().toLowerCase(Locale.ROOT);
        if (LinearBpmnBuilder.PASS_ANY.equals(v)) {
            return LinearBpmnBuilder.PASS_ANY;
        }
        if (LinearBpmnBuilder.PASS_ALL.equals(v)) {
            return LinearBpmnBuilder.PASS_ALL;
        }
        if ("sequentialall".equals(v) || LinearBpmnBuilder.PASS_SEQUENTIAL_ALL.equalsIgnoreCase(passMode.trim())) {
            return LinearBpmnBuilder.PASS_SEQUENTIAL_ALL;
        }
        return LinearBpmnBuilder.PASS_SEQUENTIAL;
    }

    public static boolean isMultiInstance(String passMode) {
        String m = normalize(passMode);
        return LinearBpmnBuilder.PASS_ANY.equals(m)
                || LinearBpmnBuilder.PASS_ALL.equals(m)
                || LinearBpmnBuilder.PASS_SEQUENTIAL_ALL.equals(m);
    }
}
