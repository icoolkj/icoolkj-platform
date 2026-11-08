package com.icoolkj.platform.framework.security.crypto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PasswordPublicKeyVO {

    String algorithm;
    String publicKey;
    String cipherPrefix;
    String payloadFormat;
    long maxSkewSeconds;
}
