package com.icoolkj.platform.api.controller.auth;

import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.framework.auth.AuthService;
import com.icoolkj.platform.framework.auth.AuthTokenIssuer;
import com.icoolkj.platform.framework.auth.dto.AuthLoginContext;
import com.icoolkj.platform.framework.auth.dto.AuthLoginRequest;
import com.icoolkj.platform.framework.auth.dto.AuthLoginResponse;
import com.icoolkj.platform.framework.auth.dto.AuthRefreshRequest;
import com.icoolkj.platform.framework.jwt.JwtProperties;
import com.icoolkj.platform.framework.jwt.JwtTokenService;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.security.service.ConcurrentLoginOptions;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.framework.security.token.TokenBlacklistService;
import com.icoolkj.platform.framework.security.crypto.PasswordPublicKeyVO;
import com.icoolkj.platform.framework.security.crypto.PasswordTransportCipher;
import com.icoolkj.platform.infra.fileaccess.FileAccessPurpose;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import com.icoolkj.platform.system.auth.service.CaptchaService;
import com.icoolkj.platform.system.auth.service.LoginBrandService;
import com.icoolkj.platform.system.auth.service.MfaService;
import com.icoolkj.platform.system.user.service.UserPreferenceService;
import com.icoolkj.platform.system.auth.service.impl.LoginSecurityConfigService;
import com.icoolkj.platform.system.auth.vo.AuthLoginBrandVO;
import com.icoolkj.platform.system.auth.vo.AuthMeVO;
import com.icoolkj.platform.system.auth.vo.CaptchaChallengeVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Authentication endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginSessionService loginSessionService;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserPreferenceService userPreferenceService;
    private final CaptchaService captchaService;
    private final LoginBrandService loginBrandService;
    private final InfraFileService infraFileService;
    private final LoginSecurityConfigService loginSecurityConfigService;
    private final MfaService mfaService;
    private final AuthTokenIssuer authTokenIssuer;
    private final PasswordTransportCipher passwordTransportCipher;

    public AuthController(AuthService authService,
                          LoginSessionService loginSessionService,
                          JwtTokenService jwtTokenService,
                          JwtProperties jwtProperties,
                          TokenBlacklistService tokenBlacklistService,
                          UserPreferenceService userPreferenceService,
                          CaptchaService captchaService,
                          LoginBrandService loginBrandService,
                          InfraFileService infraFileService,
                          LoginSecurityConfigService loginSecurityConfigService,
                          MfaService mfaService,
                          AuthTokenIssuer authTokenIssuer,
                          PasswordTransportCipher passwordTransportCipher) {
        this.authService = authService;
        this.loginSessionService = loginSessionService;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userPreferenceService = userPreferenceService;
        this.captchaService = captchaService;
        this.loginBrandService = loginBrandService;
        this.infraFileService = infraFileService;
        this.loginSecurityConfigService = loginSecurityConfigService;
        this.mfaService = mfaService;
        this.authTokenIssuer = authTokenIssuer;
        this.passwordTransportCipher = passwordTransportCipher;
    }

    @GetMapping("/captcha")
    public ApiResponse<CaptchaChallengeVO> captcha(@RequestParam("tenantCode") String tenantCode) {
        return ApiResponse.ok(captchaService.createChallenge(tenantCode));
    }

    @GetMapping("/password-public-key")
    public ApiResponse<PasswordPublicKeyVO> passwordPublicKey() {
        return ApiResponse.ok(passwordTransportCipher.publicKey());
    }

    /** 登录页按租户编码解析公开品牌（匿名）；无效租户回退平台品牌且 tenantMatched=false */
    @GetMapping("/login-brand")
    public ApiResponse<AuthLoginBrandVO> loginBrand(
            @RequestParam(value = "tenantCode", required = false) String tenantCode) {
        return ApiResponse.ok(loginBrandService.resolveByTenantCode(tenantCode));
    }

    /**
     * 登录页公开品牌素材（匿名）；必须同时传目录与槽位，避免跨目录混淆。
     */
    @GetMapping("/login-brand/asset")
    public ResponseEntity<Resource> loginBrandAsset(
            @RequestParam(value = "tenantCode", required = false) String tenantCode,
            @RequestParam("catalogCode") String catalogCode,
            @RequestParam("profileCode") String profileCode) {
        return downloadPublicBrandFile(
                loginBrandService.resolveAssetFileId(tenantCode, catalogCode, profileCode));
    }

    private ResponseEntity<Resource> downloadPublicBrandFile(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            InfraFileVO meta = infraFileService.getById(fileId, FileAccessPurpose.LOGIN_BRAND);
            Resource resource = infraFileService.loadDownloadResource(fileId, FileAccessPurpose.LOGIN_BRAND);
            String encoded = URLEncoder.encode(meta.getOriginalName(), StandardCharsets.UTF_8).replace("+", "%20");
            MediaType mediaType = MediaTypeFactory.getMediaType(meta.getOriginalName())
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encoded)
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/login")
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        LoginUser loginUser = authService.login(request, AuthLoginContext.fromCurrentRequest());
        if (mfaService.requiresMfaChallenge(loginUser)) {
            String ticket = mfaService.createPreAuthTicket(loginUser, request.getTenantCode().trim());
            return ApiResponse.ok(AuthLoginResponse.mfaChallenge(ticket));
        }
        boolean mustEnroll = mfaService.shouldForceEnroll(loginUser);
        loginUser.setMustEnrollMfa(mustEnroll);

        ConcurrentLoginOptions concurrency = new ConcurrentLoginOptions(
                loginSecurityConfigService.isAllowMultiLogin(loginUser.getTenantId()),
                loginSecurityConfigService.isSingleLoginKickoutOldest(loginUser.getTenantId())
        );
        String sessionId = loginSessionService.createSession(
                loginUser, request.getTenantCode().trim(), concurrency);
        loginUser.setSessionId(sessionId);

        return ApiResponse.ok(authTokenIssuer.issue(loginUser));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthLoginResponse> refresh(@Valid @RequestBody AuthRefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.getRefreshToken()));
    }

    @GetMapping("/me")
    public ApiResponse<AuthMeVO> me() {
        LoginUser loginUser = SecurityContextHelper.getLoginUser();
        if (loginUser == null) {
            throw new UnauthorizedException("error.unauthorized");
        }
        return ApiResponse.ok(userPreferenceService.buildAuthMe(loginUser));
    }

    @OperationLog(module = "Auth", businessType = "LOGOUT")
    @Idempotent("auth:logout")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        LoginUser loginUser = SecurityContextHelper.getLoginUser();
        if (loginUser != null && loginUser.getSessionId() != null) {
            loginSessionService.removeSession(loginUser.getSessionId());
        }
        String token = jwtTokenService.resolveToken(request.getHeader(jwtProperties.getHeaderName()));
        tokenBlacklistService.blacklist(token);
        return ApiResponse.ok();
    }
}
