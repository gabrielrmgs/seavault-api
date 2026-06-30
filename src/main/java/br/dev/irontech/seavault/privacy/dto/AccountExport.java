package br.dev.irontech.seavault.privacy.dto;

import java.time.Instant;
import java.util.List;

public record AccountExport(
        AccountInfo account,
        Object profile,
        List<?> documents,
        List<?> certificates,
        List<?> courses,
        List<?> vessels,
        List<?> companies,
        List<?> voyages,
        Instant exportedAt) {

    public record AccountInfo(String name, String email, String plan, Instant createdAt, Instant termsAcceptedAt) {
    }
}
