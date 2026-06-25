package br.dev.irontech.seavault.documents.dto;

import br.dev.irontech.seavault.common.expiry.ExpiryStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID typeId,
        String number,
        String issuer,
        LocalDate issueDate,
        LocalDate expiryDate,
        ExpiryStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
