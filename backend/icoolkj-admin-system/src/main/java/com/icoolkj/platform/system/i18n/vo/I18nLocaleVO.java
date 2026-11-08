package com.icoolkj.platform.system.i18n.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class I18nLocaleVO {

    private String id;
    private String localeCode;
    private String localeName;
    private String nativeName;
    private Integer sortNo;
    private String isDefault;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

