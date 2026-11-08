package com.icoolkj.platform.workflow.support;

import org.springframework.util.StringUtils;

/**
 * 流程标题唯一契约（P0-1）。
 * <ul>
 *   <li>业务域（如 OA）在提交时组装 {@code bizTitle}，写入 {@code wf_biz_process.biz_title} 与流程变量；
 *       这是运行态展示与通知的权威标题。</li>
 *   <li>工作流仅在启动请求未带 {@code bizTitle} 时，用模型草稿 {@code behavior.titleTemplate}
 *       （缺省 {@code summaryTemplate}）生成兜底标题。</li>
 *   <li>已启动实例不再用草稿模板重算标题；改模型不影响在途标题。</li>
 * </ul>
 */
public final class WorkflowTitleContract {

    private WorkflowTitleContract() {
    }

    /** 规范化业务传入标题；空白视为未提供。 */
    public static String normalizeBizTitle(String bizTitle) {
        if (!StringUtils.hasText(bizTitle)) {
            return null;
        }
        String trimmed = bizTitle.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
