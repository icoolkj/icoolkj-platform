package com.icoolkj.platform.framework.security.crypto;

import com.icoolkj.platform.common.config.ConfigKeyRegistry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * At-rest encryption for sensitive {@code sys_config} values.
 * Stored form: {@code enc:v1:} + AES-GCM Base64. Legacy plaintext values are accepted on read.
 */
@Component
public class SensitiveConfigCrypto {

    public static final String PREFIX = "enc:v1:";

    private final AesGcmSecretCipher cipher;

    public SensitiveConfigCrypto(AesGcmSecretCipher cipher) {
        this.cipher = cipher;
    }

    public String sealIfSensitive(String configKey, String plaintext) {
        if (!ConfigKeyRegistry.isSensitive(configKey) || !StringUtils.hasText(plaintext)) {
            return plaintext;
        }
        if (isSealed(plaintext)) {
            return plaintext;
        }
        return PREFIX + cipher.encrypt(plaintext, AesGcmSecretCipher.PURPOSE_CONFIG);
    }

    public String open(String configKey, String stored) {
        if (!StringUtils.hasText(stored)) {
            return stored;
        }
        if (!isSealed(stored)) {
            return stored;
        }
        if (!ConfigKeyRegistry.isSensitive(configKey)) {
            // sealed value under a non-sensitive key — still try decrypt
        }
        String payload = stored.substring(PREFIX.length());
        return cipher.decrypt(payload, AesGcmSecretCipher.PURPOSE_CONFIG);
    }

    public static boolean isSealed(String value) {
        return StringUtils.hasText(value) && value.startsWith(PREFIX);
    }

    public static String mask(String plaintext) {
        if (!StringUtils.hasText(plaintext)) {
            return "";
        }
        if (plaintext.length() <= 4) {
            return "****";
        }
        return "****" + plaintext.substring(plaintext.length() - 4);
    }
}
