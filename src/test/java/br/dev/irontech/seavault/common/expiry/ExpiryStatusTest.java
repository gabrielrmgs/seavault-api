package br.dev.irontech.seavault.common.expiry;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpiryStatusTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 25);
    private static final int WINDOW = 30;

    @Test
    void nullExpiryIsSemValidade() {
        assertEquals(ExpiryStatus.SEM_VALIDADE, ExpiryStatus.of(null, TODAY, WINDOW));
    }

    @Test
    void pastExpiryIsVencido() {
        assertEquals(ExpiryStatus.VENCIDO, ExpiryStatus.of(TODAY.minusDays(1), TODAY, WINDOW));
    }

    @Test
    void expiryTodayIsVencendo() {
        assertEquals(ExpiryStatus.VENCENDO, ExpiryStatus.of(TODAY, TODAY, WINDOW));
    }

    @Test
    void expiryWithinWindowIsVencendo() {
        assertEquals(ExpiryStatus.VENCENDO, ExpiryStatus.of(TODAY.plusDays(WINDOW), TODAY, WINDOW));
    }

    @Test
    void expiryBeyondWindowIsValido() {
        assertEquals(ExpiryStatus.VALIDO, ExpiryStatus.of(TODAY.plusDays(WINDOW + 1), TODAY, WINDOW));
    }
}
