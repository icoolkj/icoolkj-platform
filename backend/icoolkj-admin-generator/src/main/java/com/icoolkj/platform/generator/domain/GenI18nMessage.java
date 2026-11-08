package com.icoolkj.platform.generator.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * gen_i18n_message — low-code CRUD UI i18n JSON per table/locale.
 */
@Data
public class GenI18nMessage {

    private String id;
    private String tableId;
    private String locale;
    /** JSON string: page / fields / meta */
    private String messagesJson;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
