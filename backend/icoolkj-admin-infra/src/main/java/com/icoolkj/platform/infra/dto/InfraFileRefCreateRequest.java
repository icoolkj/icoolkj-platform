package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InfraFileRefCreateRequest {

    @NotBlank
    private String bizType;

    @NotBlank
    private String bizId;

    private String refField;
}
