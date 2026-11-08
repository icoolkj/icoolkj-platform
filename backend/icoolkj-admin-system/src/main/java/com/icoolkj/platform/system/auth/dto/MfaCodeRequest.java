package com.icoolkj.platform.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaCodeRequest {

    @NotBlank
    private String code;
}
