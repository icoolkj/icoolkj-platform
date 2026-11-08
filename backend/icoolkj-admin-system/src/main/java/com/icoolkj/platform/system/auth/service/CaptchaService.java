package com.icoolkj.platform.system.auth.service;

import com.icoolkj.platform.system.auth.vo.CaptchaChallengeVO;

/**
 * 登录验证码生成与一次性校验。
 */
public interface CaptchaService {

    CaptchaChallengeVO createChallenge(String tenantCode);

    void validateIfRequired(String tenantId, String captchaKey, String captchaCode);
}
