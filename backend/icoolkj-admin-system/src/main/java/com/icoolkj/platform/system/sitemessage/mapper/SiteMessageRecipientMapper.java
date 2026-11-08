package com.icoolkj.platform.system.sitemessage.mapper;

import com.icoolkj.platform.system.sitemessage.domain.SiteMessageRecipientEntity;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageInboxPageQuery;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageRecipientPageQuery;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageInboxVO;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageRecipientStatusVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SiteMessageRecipientMapper {

    SiteMessageRecipientEntity selectById(@Param("id") String id);

    SiteMessageInboxVO selectInboxDetail(@Param("id") String id,
                                         @Param("tenantId") String tenantId,
                                         @Param("userId") String userId);

    List<SiteMessageInboxVO> selectInboxPage(SiteMessageInboxPageQuery query);

    long countInboxByQuery(SiteMessageInboxPageQuery query);

    int batchInsert(@Param("list") List<SiteMessageRecipientEntity> list);

    int markRead(@Param("id") String id,
                 @Param("tenantId") String tenantId,
                 @Param("userId") String userId,
                 @Param("readAt") LocalDateTime readAt);

    int deleteByMessageId(@Param("messageId") String messageId);

    int deleteByMessageIds(@Param("messageIds") List<String> messageIds);

    long countByMessageId(@Param("messageId") String messageId);

    List<SiteMessageRecipientStatusVO> selectRecipientStatusPage(SiteMessageRecipientPageQuery query);

    long countRecipientStatusByQuery(SiteMessageRecipientPageQuery query);
}
