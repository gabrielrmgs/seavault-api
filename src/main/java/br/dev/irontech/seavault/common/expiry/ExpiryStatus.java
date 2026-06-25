package br.dev.irontech.seavault.common.expiry;

import java.time.LocalDate;

public enum ExpiryStatus {
    SEM_VALIDADE,
    VALIDO,
    VENCENDO,
    VENCIDO;

    public static ExpiryStatus of(LocalDate expiryDate, LocalDate today, int warningDays) {
        if (expiryDate == null) {
            return SEM_VALIDADE;
        }
        if (expiryDate.isBefore(today)) {
            return VENCIDO;
        }
        if (!expiryDate.isAfter(today.plusDays(warningDays))) {
            return VENCENDO;
        }
        return VALIDO;
    }
}
