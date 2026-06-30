package br.dev.irontech.seavault.common.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CryptoConverterTest {

    @Test
    void roundTripsValue() {
        CryptoConverter c = new CryptoConverter();
        c.keyBase64 = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";
        String plain = "123.456.789-00";

        String cipher = c.convertToDatabaseColumn(plain);

        assertNotEquals(plain, cipher);
        assertEquals(plain, c.convertToEntityAttribute(cipher));
    }

    @Test
    void handlesNull() {
        CryptoConverter c = new CryptoConverter();
        c.keyBase64 = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

        assertNull(c.convertToDatabaseColumn(null));
        assertNull(c.convertToEntityAttribute(null));
    }

    @Test
    void keepsLegacyPlaintextReadable() {
        CryptoConverter c = new CryptoConverter();
        c.keyBase64 = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

        assertEquals("123.456.789-00", c.convertToEntityAttribute("123.456.789-00"));
    }
}
