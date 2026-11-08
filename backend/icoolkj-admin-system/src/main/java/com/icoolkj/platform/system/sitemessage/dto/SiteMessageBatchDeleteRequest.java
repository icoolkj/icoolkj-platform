package com.icoolkj.platform.system.sitemessage.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** 批量删除站内信 */
@Getter
@Setter
public class SiteMessageBatchDeleteRequest {

    @NotEmpty(message = "{gen.siteMessage.ids.required}")
    private List<String> ids;
}
