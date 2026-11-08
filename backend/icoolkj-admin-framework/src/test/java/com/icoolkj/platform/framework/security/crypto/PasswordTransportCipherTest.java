package com.icoolkj.platform.framework.security.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordTransportCipherTest {

    private PasswordTransportCipher cipher;

    @BeforeEach
    void setUp() {
        SecurityProperties securityProperties = new SecurityProperties();
        securityProperties.getPasswordTransport().setEnabled(true);
        securityProperties.getPasswordTransport().setAllowPlaintext(false);
        securityProperties.getPasswordTransport().setMaxSkewSeconds(120);
        cipher = new PasswordTransportCipher(securityProperties);
        cipher.init();
    }

    @Test
    void roundTripSealUnwrap() {
        String wired = cipher.seal("StudyOnly@2026");
        assertTrue(wired.startsWith(PasswordTransportCipher.CIPHER_PREFIX));
        assertEquals("StudyOnly@2026", cipher.unwrap(wired));
    }

    @Test
    void rejectsPlaintextWhenNotAllowed() {
        BusinessException ex = assertThrows(BusinessException.class, () -> cipher.unwrap("plain-password"));
        assertEquals("error.auth.password_transport_required", ex.getMessage());
    }

    @Test
    void unwrapIfEncryptedKeepsPlaintext() {
        assertEquals("plain-password", cipher.unwrapIfEncrypted("plain-password"));
    }

    @Test
    void publicKeyMatchesWireContract() {
        PasswordPublicKeyVO vo = cipher.publicKey();
        assertEquals(PasswordTransportCipher.ALGORITHM, vo.getAlgorithm());
        assertEquals(PasswordTransportCipher.CIPHER_PREFIX, vo.getCipherPrefix());
        assertEquals(PasswordTransportCipher.PAYLOAD_FORMAT, vo.getPayloadFormat());
        assertEquals(120L, vo.getMaxSkewSeconds());
        assertTrue(vo.getPublicKey().startsWith("04"));
        assertEquals(PasswordTransportCipher.PUBLIC_KEY_HEX_LENGTH, vo.getPublicKey().length());
    }
}
