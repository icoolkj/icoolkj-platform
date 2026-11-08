package com.icoolkj.platform.framework.security.crypto;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.jwt.JwtProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AES-GCM helper; key derived from JWT secret with a purpose-specific info prefix.
 */
@Component
public class AesGcmSecretCipher {

    public static final String PURPOSE_MFA = "mfa";
    public static final String PURPOSE_CONFIG = "config";
    public static final String PURPOSE_FILE = "file";

    private static final String INFO_MFA = "icoolkj-mfa-secret-v1";
    private static final String INFO_CONFIG = "icoolkj-config-secret-v1";
    private static final String INFO_FILE = "icoolkj-file-content-v1";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LEN = 12;

    private final String jwtSecret;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, SecretKey> keys = new ConcurrentHashMap<>();

    public AesGcmSecretCipher(JwtProperties jwtProperties) {
        this.jwtSecret = jwtProperties.getSecret();
        // warm MFA key (backward compatible)
        keys.put(PURPOSE_MFA, deriveKey(INFO_MFA, jwtSecret));
    }

    /** MFA convenience — same as {@code encrypt(plaintext, PURPOSE_MFA)}. */
    public String encrypt(String plaintext) {
        return encrypt(plaintext, PURPOSE_MFA);
    }

    /** MFA convenience — same as {@code decrypt(ciphertext, PURPOSE_MFA)}. */
    public String decrypt(String ciphertext) {
        return decrypt(ciphertext, PURPOSE_MFA);
    }

    public String encrypt(String plaintext, String purpose) {
        if (!StringUtils.hasText(plaintext)) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LEN];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keyFor(purpose), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buf = ByteBuffer.allocate(iv.length + cipherBytes.length);
            buf.put(iv);
            buf.put(cipherBytes);
            return Base64.getEncoder().encodeToString(buf.array());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("error.auth.mfa_crypto_failed");
        }
    }

    public String decrypt(String ciphertext, String purpose) {
        if (!StringUtils.hasText(ciphertext)) {
            return null;
        }
        try {
            byte[] all = Base64.getDecoder().decode(ciphertext);
            byte[] iv = new byte[IV_LEN];
            byte[] cipherBytes = new byte[all.length - IV_LEN];
            System.arraycopy(all, 0, iv, 0, IV_LEN);
            System.arraycopy(all, IV_LEN, cipherBytes, 0, cipherBytes.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keyFor(purpose), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("error.auth.mfa_crypto_failed");
        }
    }

    /** AES-GCM binary: 12-byte IV + ciphertext+tag (no Base64). */
    public byte[] encryptBytes(byte[] plain, String purpose) {
        if (plain == null || plain.length == 0) {
            return plain;
        }
        try {
            byte[] iv = new byte[IV_LEN];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keyFor(purpose), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherBytes = cipher.doFinal(plain);
            ByteBuffer buf = ByteBuffer.allocate(iv.length + cipherBytes.length);
            buf.put(iv);
            buf.put(cipherBytes);
            return buf.array();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("error.file.encryption_failed");
        }
    }

    /** Decrypt {@link #encryptBytes(byte[], String)} payload. */
    public byte[] decryptBytes(byte[] cipherPayload, String purpose) {
        if (cipherPayload == null || cipherPayload.length <= IV_LEN) {
            return cipherPayload;
        }
        try {
            byte[] iv = new byte[IV_LEN];
            byte[] cipherBytes = new byte[cipherPayload.length - IV_LEN];
            System.arraycopy(cipherPayload, 0, iv, 0, IV_LEN);
            System.arraycopy(cipherPayload, IV_LEN, cipherBytes, 0, cipherBytes.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keyFor(purpose), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return cipher.doFinal(cipherBytes);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("error.file.encryption_failed");
        }
    }

    private SecretKey keyFor(String purpose) {
        String p = StringUtils.hasText(purpose) ? purpose : PURPOSE_MFA;
        return keys.computeIfAbsent(p, key -> {
            String info;
            if (PURPOSE_CONFIG.equals(key)) {
                info = INFO_CONFIG;
            } else if (PURPOSE_FILE.equals(key)) {
                info = INFO_FILE;
            } else {
                info = INFO_MFA;
            }
            return deriveKey(info, jwtSecret);
        });
    }

    private static SecretKey deriveKey(String info, String jwtSecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(info.getBytes(StandardCharsets.UTF_8));
            digest.update((jwtSecret == null ? "" : jwtSecret).getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(digest.digest(), "AES");
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to derive cipher key", ex);
        }
    }
}
