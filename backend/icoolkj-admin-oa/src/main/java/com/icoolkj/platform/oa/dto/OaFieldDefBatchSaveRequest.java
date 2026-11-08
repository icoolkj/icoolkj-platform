package com.icoolkj.platform.oa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaFieldDefBatchSaveRequest {

    @NotBlank
    private String versionId;

    @NotNull
    @Valid
    private List<OaFieldDefItemRequest> fields;

    /** 可选：FormDocument JSON；仅更新布局时也可随字段一并提交 */
    private String layoutJson;
}
