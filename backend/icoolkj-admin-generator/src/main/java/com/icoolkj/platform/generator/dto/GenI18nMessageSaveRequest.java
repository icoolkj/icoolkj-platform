package com.icoolkj.platform.generator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenI18nMessageSaveRequest {

    private String id;

    @NotBlank
    private String tableId;

    @NotBlank
    private String locale;

    @NotBlank
    private String messagesJson;

    private String remark;
}
