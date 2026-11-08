package com.icoolkj.platform.system.auth.service.impl;

import com.icoolkj.platform.common.constant.CacheKeyConstants;
import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import com.icoolkj.platform.system.auth.service.CaptchaService;
import com.icoolkj.platform.system.tenant.service.TenantQueryService;
import com.icoolkj.platform.system.auth.vo.CaptchaChallengeVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final String ERROR_CAPTCHA_REQUIRED = "error.auth.captcha_required";
    private static final String ERROR_CAPTCHA_INVALID = "error.auth.captcha_invalid";

    private static final char[] CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODE_LENGTH = 4;
    private static final int IMAGE_WIDTH = 120;
    private static final int IMAGE_HEIGHT = 40;
    private static final long DEFAULT_TTL_SECONDS = 120L;

    private final TenantQueryService tenantQueryService;
    private final LoginSecurityConfigService loginSecurityConfigService;
    private final StringRedisTemplate stringRedisTemplate;
    private final CacheTtlProperties cacheTtlProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public CaptchaServiceImpl(TenantQueryService tenantQueryService,
                              LoginSecurityConfigService loginSecurityConfigService,
                              StringRedisTemplate stringRedisTemplate,
                              CacheTtlProperties cacheTtlProperties) {
        this.tenantQueryService = tenantQueryService;
        this.loginSecurityConfigService = loginSecurityConfigService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheTtlProperties = cacheTtlProperties;
    }

    @Override
    public CaptchaChallengeVO createChallenge(String tenantCode) {
        if (!StringUtils.hasText(tenantCode)) {
            throw new UnauthorizedException("validation.tenant_code.required");
        }
        String tenantId = tenantQueryService.resolveLoginTenantId(tenantCode.trim());
        CaptchaChallengeVO vo = new CaptchaChallengeVO();
        if (!loginSecurityConfigService.isCaptchaRequired(tenantId)) {
            vo.setRequireCaptcha(false);
            return vo;
        }

        String code = randomCode();
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(captchaKey(captchaKey), code, captchaTtl());

        vo.setRequireCaptcha(true);
        vo.setCaptchaKey(captchaKey);
        vo.setImageBase64(renderPngBase64(code));
        return vo;
    }

    @Override
    public void validateIfRequired(String tenantId, String captchaKey, String captchaCode) {
        if (!loginSecurityConfigService.isCaptchaRequired(tenantId)) {
            return;
        }
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaCode)) {
            throw new UnauthorizedException(ERROR_CAPTCHA_REQUIRED);
        }
        String redisKey = captchaKey(captchaKey.trim());
        // GET + DELETE（兼容 Redis < 6.2；避免 Spring Data Redis getAndDelete → GETDEL）
        String expected = stringRedisTemplate.opsForValue().get(redisKey);
        stringRedisTemplate.delete(redisKey);
        if (!StringUtils.hasText(expected)) {
            throw new UnauthorizedException(ERROR_CAPTCHA_INVALID);
        }
        if (!expected.equalsIgnoreCase(captchaCode.trim())) {
            throw new UnauthorizedException(ERROR_CAPTCHA_INVALID);
        }
    }

    private String captchaKey(String captchaKey) {
        return cacheTtlProperties.getKeyPrefix() + ":"
                + CacheKeyConstants.AUTH_CAPTCHA
                + captchaKey;
    }

    private Duration captchaTtl() {
        long seconds = cacheTtlProperties.getTtl().getAuthCaptcha();
        if (seconds <= 0) {
            seconds = DEFAULT_TTL_SECONDS;
        }
        return Duration.ofSeconds(seconds);
    }

    private String randomCode() {
        char[] chars = new char[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            chars[i] = CODE_CHARS[secureRandom.nextInt(CODE_CHARS.length)];
        }
        return new String(chars);
    }

    private String renderPngBase64(String code) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(245, 247, 250));
            g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

            for (int i = 0; i < 6; i++) {
                g.setColor(randomNoiseColor());
                g.drawLine(
                        secureRandom.nextInt(IMAGE_WIDTH),
                        secureRandom.nextInt(IMAGE_HEIGHT),
                        secureRandom.nextInt(IMAGE_WIDTH),
                        secureRandom.nextInt(IMAGE_HEIGHT)
                );
            }

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            int x = 14;
            for (int i = 0; i < code.length(); i++) {
                g.setColor(new Color(30 + secureRandom.nextInt(80), 40 + secureRandom.nextInt(80), 90 + secureRandom.nextInt(100)));
                int y = 26 + secureRandom.nextInt(6);
                g.drawString(String.valueOf(code.charAt(i)), x, y);
                x += 24;
            }

            for (int i = 0; i < 40; i++) {
                g.setColor(randomNoiseColor());
                g.fillRect(secureRandom.nextInt(IMAGE_WIDTH), secureRandom.nextInt(IMAGE_HEIGHT), 1, 1);
            }
        } finally {
            g.dispose();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to render captcha image", ex);
        }
    }

    private Color randomNoiseColor() {
        return new Color(
                150 + secureRandom.nextInt(80),
                150 + secureRandom.nextInt(80),
                150 + secureRandom.nextInt(80)
        );
    }
}
