package com.icoolkj.platform.api.controller.auth;

import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.auth.dto.MfaCodeRequest;
import com.icoolkj.platform.system.auth.dto.MfaVerifyRequest;
import com.icoolkj.platform.system.auth.service.MfaService;
import com.icoolkj.platform.system.auth.vo.MfaEnrollStartVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/mfa")
public class AuthMfaController {

    private final MfaService mfaService;

    public AuthMfaController(MfaService mfaService) {
        this.mfaService = mfaService;
    }

    @PostMapping("/verify")
    public ApiResponse<?> verify(@Valid @RequestBody MfaVerifyRequest request) {
        return ApiResponse.ok(mfaService.verifyLogin(request));
    }

    @Idempotent("auth:mfa:enroll:start")
    @PostMapping("/enroll/start")
    public ApiResponse<MfaEnrollStartVO> enrollStart() {
        return ApiResponse.ok(mfaService.startEnroll(requireUser()));
    }

    @OperationLog(module = "Auth", businessType = "MFA_ENROLL")
    @Idempotent("auth:mfa:enroll:confirm")
    @PostMapping("/enroll/confirm")
    public ApiResponse<Void> enrollConfirm(@Valid @RequestBody MfaCodeRequest request) {
        mfaService.confirmEnroll(requireUser(), request);
        return ApiResponse.ok();
    }

    @OperationLog(module = "Auth", businessType = "MFA_DISABLE")
    @Idempotent("auth:mfa:disable")
    @PostMapping("/disable")
    public ApiResponse<Void> disable(@Valid @RequestBody MfaCodeRequest request) {
        mfaService.disable(requireUser(), request);
        return ApiResponse.ok();
    }

    private LoginUser requireUser() {
        LoginUser loginUser = SecurityContextHelper.getLoginUser();
        if (loginUser == null) {
            throw new UnauthorizedException("error.unauthorized");
        }
        return loginUser;
    }
}
