package br.dev.irontech.seavault.common.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpaqueTokensTest {

    @Test
    void generatesDistinctTokens() {
        assertNotEquals(OpaqueTokens.generate(), OpaqueTokens.generate());
        assertTrue(OpaqueTokens.generate().length() >= 32);
    }

    @Test
    void sha256IsDeterministicAndHex() {
        String a = OpaqueTokens.sha256("hello");
        String b = OpaqueTokens.sha256("hello");
        assertEquals(a, b);
        assertEquals(64, a.length());
        assertTrue(a.matches("[0-9a-f]+"));
    }
}
