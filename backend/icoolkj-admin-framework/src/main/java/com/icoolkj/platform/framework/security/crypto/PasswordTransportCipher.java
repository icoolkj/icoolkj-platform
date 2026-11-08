package com.icoolkj.platform.framework.security.crypto;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.SecurityProperties;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * SM2 口令传输：公钥 SEC1 {@code 04||x||y}；字段 {@code enc:sm2:v1:} + C1C3C2，负载 {@code epochMillis|password}。
 */
@Component
public class PasswordTransportCipher {

    public static final String CIPHER_PREFIX = "enc:sm2:v1:";
    public static final String ALGORITHM = "SM2";
    public static final String PAYLOAD_FORMAT = "epochMillis|password";
    /** SEC1 uncompressed point hex length: {@code 04} + 32-byte X + 32-byte Y. */
    public static final int PUBLIC_KEY_HEX_LENGTH = 130;

    private final SecurityProperties securityProperties;

    private SM2 sm2;
    private String publicKeyHex;

    public PasswordTransportCipher(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @PostConstruct
    void init() {
        this.sm2 = SmUtil.sm2();
        this.publicKeyHex = encodeUncompressedPublicKey(sm2.getPublicKey());
    }

    /**
     * SEC1 uncompressed public key for browser {@code sm-crypto}.
     * {@link cn.hutool.crypto.BCUtil#encodeECPublicKey} is not used: it is not the FE wire format.
     */
    static String encodeUncompressedPublicKey(PublicKey publicKey) {
        if (!(publicKey instanceof BCECPublicKey becPublicKey)) {
            throw new IllegalStateException("SM2 public key type unsupported");
        }
        String hex = HexUtil.encodeHexStr(becPublicKey.getQ().getEncoded(false));
        if (!hex.startsWith("04") || hex.length() != PUBLIC_KEY_HEX_LENGTH) {
            throw new IllegalStateException("SM2 public key must be SEC1 uncompressed point");
        }
        return hex;
    }

    public PasswordPublicKeyVO publicKey() {
        SecurityProperties.PasswordTransport cfg = securityProperties.getPasswordTransport();
        return PasswordPublicKeyVO.builder()
                .algorithm(ALGORITHM)
                .publicKey(publicKeyHex)
                .cipherPrefix(CIPHER_PREFIX)
                .payloadFormat(PAYLOAD_FORMAT)
                .maxSkewSeconds(cfg.getMaxSkewSeconds())
                .build();
    }

    public String unwrap(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String value = raw.trim();
        SecurityProperties.PasswordTransport cfg = securityProperties.getPasswordTransport();
        if (!cfg.isEnabled()) {
            return value;
        }
        if (value.startsWith(CIPHER_PREFIX)) {
            return decryptPayload(value.substring(CIPHER_PREFIX.length()), cfg.getMaxSkewSeconds());
        }
        if (cfg.isAllowPlaintext()) {
            return value;
        }
        throw new BusinessException("error.auth.password_transport_required");
    }

    public String unwrapIfEncrypted(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String value = raw.trim();
        if (!securityProperties.getPasswordTransport().isEnabled()) {
            return value;
        }
        if (value.startsWith(CIPHER_PREFIX)) {
            return decryptPayload(value.substring(CIPHER_PREFIX.length()),
                    securityProperties.getPasswordTransport().getMaxSkewSeconds());
        }
        return value;
    }

    private String decryptPayload(String cipherHex, long maxSkewSeconds) {
        try {
            // sm-crypto C1 may omit leading 04; Hutool decrypt expects it
            String normalized = normalizeSm2CipherHex(cipherHex);
            String payload = sm2.decryptStr(normalized, KeyType.PrivateKey);
            return parseFreshPassword(payload, maxSkewSeconds);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("error.auth.password_transport_invalid");
        }
    }

    private static String parseFreshPassword(String payload, long maxSkewSeconds) {
        if (!StringUtils.hasText(payload)) {
            throw new BusinessException("error.auth.password_transport_invalid");
        }
        int sep = payload.indexOf('|');
        if (sep <= 0 || sep >= payload.length() - 1) {
            throw new BusinessException("error.auth.password_transport_invalid");
        }
        String tsPart = payload.substring(0, sep);
        String password = payload.substring(sep + 1);
        long ts;
        try {
            ts = Long.parseLong(tsPart);
        } catch (NumberFormatException ex) {
            throw new BusinessException("error.auth.password_transport_invalid");
        }
        long skewMs = Math.abs(System.currentTimeMillis() - ts);
        if (skewMs > Math.max(1L, maxSkewSeconds) * 1000L) {
            throw new BusinessException("error.auth.password_transport_expired");
        }
        if (!StringUtils.hasText(password)) {
            throw new BusinessException("error.auth.password_transport_invalid");
        }
        return password;
    }

    static String normalizeSm2CipherHex(String cipherHex) {
        String hex = cipherHex == null ? "" : cipherHex.trim();
        if (hex.startsWith("04") || hex.startsWith("05")) {
            return hex;
        }
        return "04" + hex;
    }

    /** Package-visible for unit tests (same wire format as browser). */
    String seal(String plaintextPassword) {
        long ts = System.currentTimeMillis();
        String payload = ts + "|" + plaintextPassword;
        String cipherHex = sm2.encryptHex(payload, StandardCharsets.UTF_8, KeyType.PublicKey);
        return CIPHER_PREFIX + normalizeSm2CipherHex(cipherHex);
    }
}
