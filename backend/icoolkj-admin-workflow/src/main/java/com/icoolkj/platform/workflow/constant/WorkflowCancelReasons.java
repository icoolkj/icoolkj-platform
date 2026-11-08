package com.icoolkj.platform.workflow.constant;

import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * 撤回/取消原因解析（写入 {@code wf_task_action.payload_json.reasonCode}）。
 * <p>工作流只区分：
 * <ul>
 *   <li>原因码 token（{@code [a-z][a-z0-9_]*}）→ 原样写入</li>
 *   <li>自由意见 → 返回 null，调用方写入 comment，payload 可用 {@link #DEFAULT}</li>
 * </ul>
 * 业务域原因码放业务模块；本类仅含<strong>流程动作</strong>通用码。
 */
public final class WorkflowCancelReasons {

    /** 未指明原因时的默认码 */
    public static final String DEFAULT = "cancelled";

    /** 已发申请入口撤回 */
    public static final String STARTER_WITHDRAW = "starter_withdraw";

    private static final Pattern REASON_CODE = Pattern.compile("[a-z][a-z0-9_]*");

    private WorkflowCancelReasons() {
    }

    /**
     * @return 原因码 token；自由意见返回 null
     */
    public static String resolveCode(String reasonOrComment) {
        if (!StringUtils.hasText(reasonOrComment)) {
            return DEFAULT;
        }
        String raw = reasonOrComment.trim();
        if (isReasonCodeToken(raw)) {
            return raw;
        }
        return null;
    }

    public static boolean isKnownCode(String value) {
        return StringUtils.hasText(value) && isReasonCodeToken(value.trim());
    }

    private static boolean isReasonCodeToken(String value) {
        return REASON_CODE.matcher(value).matches();
    }
}
