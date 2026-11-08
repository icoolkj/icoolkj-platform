package com.icoolkj.platform.oa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaVersionPublishRequest {

    @NotBlank
    private String versionId;
}
