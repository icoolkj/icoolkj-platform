package com.icoolkj.platform.system.notice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** 批量删除通知公告 */
@Getter
@Setter
public class NoticeBatchDeleteRequest {

    @NotEmpty(message = "{gen.notice.ids.required}")
    private List<String> ids;
}
