package com.icoolkj.platform.system.sitemessage.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.annotation.DataScope;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageBatchDeleteRequest;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageInboxPageQuery;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessagePageQuery;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageSendRequest;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageRecipientPageQuery;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageInboxVO;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageRecipientStatusVO;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageVO;
import java.util.List;

/** 站内信服务 */
public interface SiteMessageService {

    @DataScope(userColumn = "user_id", deptColumn = "")
    PageResult<SiteMessageVO> page(SiteMessagePageQuery query);

    SiteMessageVO getById(String id);

    PageResult<SiteMessageRecipientStatusVO> recipientPage(String messageId, SiteMessageRecipientPageQuery query);

    String send(SiteMessageSendRequest request);

    /**
     * 流程生命周期等系统通知：按收件人真实租户落库，不要求本租户 dept 过滤；
     * 独立事务，避免通知失败污染主业务事务。无有效收件人时静默跳过。
     */
    void notifyUsers(List<String> userIds, String subject, String content);

    void deleteById(String id);

    void batchDelete(SiteMessageBatchDeleteRequest request);

    PageResult<SiteMessageInboxVO> inboxPage(SiteMessageInboxPageQuery query);

    SiteMessageInboxVO inboxDetail(String recipientId, String tenantId, String userId);

    long countUnread(String tenantId, String userId);

    void markRead(String recipientId, String tenantId, String userId);
}
