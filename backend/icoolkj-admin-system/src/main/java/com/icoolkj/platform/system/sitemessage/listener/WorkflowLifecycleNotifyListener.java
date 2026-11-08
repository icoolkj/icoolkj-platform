package com.icoolkj.platform.system.sitemessage.listener;

import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.system.sitemessage.service.SiteMessageService;
import com.icoolkj.platform.workflow.event.WorkflowLifecycleNotifyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 流程生命周期通知 → 站内信（事务提交后发送，失败不影响审批主流程） */
@Component
public class WorkflowLifecycleNotifyListener {

    private static final Logger log = LoggerFactory.getLogger(WorkflowLifecycleNotifyListener.class);

    private final SiteMessageService siteMessageService;
    private final I18nMessageResolver i18nMessageResolver;

    public WorkflowLifecycleNotifyListener(SiteMessageService siteMessageService,
                                           I18nMessageResolver i18nMessageResolver) {
        this.siteMessageService = siteMessageService;
        this.i18nMessageResolver = i18nMessageResolver;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowLifecycleNotify(WorkflowLifecycleNotifyEvent event) {
        if (event == null || CollectionUtils.isEmpty(event.getRecipientUserIds())) {
            return;
        }
        try {
            String fallback = i18nMessageResolver.resolve("notify.workflow.default");
            String subject = StringUtils.hasText(event.getTitle()) ? event.getTitle() : fallback;
            String content = StringUtils.hasText(event.getContent()) ? event.getContent() : subject;
            siteMessageService.notifyUsers(event.getRecipientUserIds(), subject, content);
        } catch (Exception ex) {
            log.warn("Send workflow lifecycle site message failed, eventType={}, processInstanceId={}",
                    event.getEventType(), event.getProcessInstanceId(), ex);
        }
    }
}
