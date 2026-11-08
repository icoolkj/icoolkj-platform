package com.icoolkj.platform.system.i18n.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class I18nMessageVO {

    private String id;
    private String messageKey;
    private String locale;
    private String messageValue;
    private String module;
    private String scope;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

