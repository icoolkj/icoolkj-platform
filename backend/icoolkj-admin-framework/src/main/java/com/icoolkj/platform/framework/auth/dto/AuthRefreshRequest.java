package com.icoolkj.platform.framework.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRefreshRequest {

    @NotBlank(message = "{validation.auth.refresh_token.required}")
    private String refreshToken;
}
