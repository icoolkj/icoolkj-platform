package com.icoolkj.platform.system.auth.vo;

import lombok.Data;

/**
 * 登录前验证码挑战。{@code requireCaptcha=false} 时 key/image 为空。
 */
@Data
public class CaptchaChallengeVO {

    private boolean requireCaptcha;
    private String captchaKey;
    private String imageBase64;
}
