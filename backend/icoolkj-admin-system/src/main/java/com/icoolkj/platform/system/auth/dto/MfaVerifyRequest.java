package com.icoolkj.platform.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaVerifyRequest {

    @NotBlank
    private String mfaTicket;

    @NotBlank
    private String code;
}
