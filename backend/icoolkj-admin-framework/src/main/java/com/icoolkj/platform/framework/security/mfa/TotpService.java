package com.icoolkj.platform.framework.security.mfa;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

/**
 * RFC 6238 TOTP (6 digits / 30s) using java-otp.
 */
@Service
public class TotpService {

    private static final String KEY_ALG = "HmacSHA1";
    private final TimeBasedOneTimePasswordGenerator totp;

    public TotpService() {
        this.totp = new TimeBasedOneTimePasswordGenerator();
    }

    public String generateSecret() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALG);
            keyGenerator.init(160);
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("HmacSHA1 unavailable", ex);
        }
    }

    public boolean verify(String base64Secret, String code) {
        if (!StringUtils.hasText(base64Secret) || !StringUtils.hasText(code)) {
            return false;
        }
        String normalized = code.trim();
        if (!normalized.matches("\\d{6}")) {
            return false;
        }
        try {
            SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(base64Secret), KEY_ALG);
            Instant now = Instant.now();
            // allow ±1 step drift
            for (int i = -1; i <= 1; i++) {
                Instant at = now.plusSeconds(i * totp.getTimeStep().getSeconds());
                int expected = totp.generateOneTimePassword(key, at);
                if (normalized.equals(String.format(Locale.ROOT, "%06d", expected))) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    public String buildOtpAuthUri(String issuer, String accountName, String base64Secret) {
        String label = url(issuer) + ":" + url(accountName);
        String secret = toBase32(Base64.getDecoder().decode(base64Secret));
        return "otpauth://totp/" + label
                + "?secret=" + secret
                + "&issuer=" + url(issuer)
                + "&algorithm=SHA1&digits=6&period=30";
    }

    private static String url(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    /** Standard Base32 (no padding) for authenticator apps. */
    static String toBase32(byte[] data) {
        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder out = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                out.append(alphabet.charAt((buffer >> (bitsLeft - 5)) & 0x1f));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            out.append(alphabet.charAt((buffer << (5 - bitsLeft)) & 0x1f));
        }
        return out.toString();
    }
}
