package com.icoolkj.platform.system.auth.vo;

import lombok.Data;

@Data
public class MfaEnrollStartVO {

    private String otpAuthUri;
    private String secretHint;
}
