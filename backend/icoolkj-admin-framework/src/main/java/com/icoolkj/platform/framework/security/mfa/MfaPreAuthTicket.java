package com.icoolkj.platform.framework.security.mfa;

import com.icoolkj.platform.framework.security.model.LoginUser;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Redis payload for MFA pre-auth ticket (password already verified). */
@Data
public class MfaPreAuthTicket implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LoginUser loginUser;
    private String tenantCode;
}
