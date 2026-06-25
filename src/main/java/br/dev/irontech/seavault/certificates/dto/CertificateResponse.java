package br.dev.irontech.seavault.certificates.dto;

import br.dev.irontech.seavault.common.expiry.ExpiryStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CertificateResponse(
        UUID id,
        String name,
        String code,
        String institution,
        LocalDate issueDate,
        LocalDate expiryDate,
        ExpiryStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
