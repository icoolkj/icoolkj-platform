package com.icoolkj.platform.infra.security;

import com.icoolkj.platform.framework.security.crypto.AesGcmSecretCipher;
import org.springframework.stereotype.Component;

/** App-layer AES-GCM for file content at rest. */
@Component
public class FileContentCipher {

    private final AesGcmSecretCipher aesGcmSecretCipher;

    public FileContentCipher(AesGcmSecretCipher aesGcmSecretCipher) {
        this.aesGcmSecretCipher = aesGcmSecretCipher;
    }

    public byte[] encrypt(byte[] plain) {
        return aesGcmSecretCipher.encryptBytes(plain, AesGcmSecretCipher.PURPOSE_FILE);
    }

    public byte[] decrypt(byte[] cipher) {
        return aesGcmSecretCipher.decryptBytes(cipher, AesGcmSecretCipher.PURPOSE_FILE);
    }
}
