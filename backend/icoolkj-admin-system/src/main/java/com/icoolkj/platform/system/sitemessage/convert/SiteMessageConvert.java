package com.icoolkj.platform.system.sitemessage.convert;

import com.icoolkj.platform.system.sitemessage.domain.SiteMessageEntity;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageVO;

public final class SiteMessageConvert {

    private SiteMessageConvert() {
    }

    public static SiteMessageVO toVo(SiteMessageEntity entity) {
        if (entity == null) {
            return null;
        }
        SiteMessageVO vo = new SiteMessageVO();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setMessageSubject(entity.getMessageSubject());
        vo.setMessageCategory(entity.getMessageCategory());
        vo.setMessageContent(entity.getMessageContent());
        vo.setSenderId(entity.getSenderId());
        vo.setSenderName(entity.getSenderName());
        vo.setSentAt(entity.getSentAt());
        return vo;
    }
}
