package com.icoolkj.platform.oa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaVersionCreateRequest {

    @NotBlank
    private String templateId;

    @Size(max = 20)
    private String wfModelId;

    @Size(max = 500)
    private String remark;
}
