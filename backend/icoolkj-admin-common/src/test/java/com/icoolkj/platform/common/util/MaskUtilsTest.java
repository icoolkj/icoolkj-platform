package com.icoolkj.platform.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MaskUtilsTest {

    @Test
    void phoneMasksMiddle() {
        assertEquals("138****5678", MaskUtils.phone("13812345678"));
        assertEquals("****", MaskUtils.phone("123"));
        assertNull(MaskUtils.phone(null));
    }

    @Test
    void emailMasksLocalPart() {
        assertEquals("a***@example.com", MaskUtils.email("alice@example.com"));
        assertEquals("*@ex.com", MaskUtils.email("a@ex.com"));
        assertEquals("****", MaskUtils.email("bad"));
    }

    @Test
    void nameKeepsFirstChar() {
        assertEquals("张**", MaskUtils.name("张伟"));
        assertEquals("*", MaskUtils.name("A"));
    }

    @Test
    void ipMasksLastOctet() {
        assertEquals("192.168.1.***", MaskUtils.ip("192.168.1.10"));
        assertEquals("***.***.***.***", MaskUtils.ip("::1"));
    }

    @Test
    void idCardKeepsHeadAndTail() {
        assertEquals("1101**********101X", MaskUtils.idCard("11010119900101101X"));
        assertEquals("1101**********101X", MaskUtils.idCard("1101 0119 9001 0110 1X"));
        assertEquals("****", MaskUtils.idCard("123456"));
    }

    @Test
    void bankCardKeepsHeadAndTail() {
        assertEquals("6222********1234", MaskUtils.bankCard("6222021234561234"));
        assertEquals("6222********1234", MaskUtils.bankCard("6222 0212 3456 1234"));
        assertEquals("****", MaskUtils.bankCard("12345678"));
    }
}
