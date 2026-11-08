package com.icoolkj.platform.workflow.spi;

import java.util.Optional;

/**
 * 流程通知文案模板源（配置驱动）。
 * 由 system 模块实现（读 {@code workflow.notify.templates}）；缺省不覆盖则回落 i18n。
 */
public interface WorkflowNotifyTemplateSource {

    /**
     * @param eventType {@code REJECT}/{@code TODO}/…（与 WorkflowProcessServiceImpl.EVENT_* 一致）
     * @return 非空时用 pattern；字段为空仍回落 i18n
     */
    Optional<EventTemplate> find(String eventType);

    record EventTemplate(String titlePattern, String contentPattern) {
    }
}
