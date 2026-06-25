package br.dev.irontech.seavault.voyages.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record VoyageResponse(
        UUID id,
        UUID vesselId,
        UUID companyId,
        UUID navigationTypeId,
        UUID categoryId,
        String role,
        LocalDate embarkDate,
        LocalDate disembarkDate,
        String embarkPort,
        String disembarkPort,
        String status,
        Integer calculatedDays,
        Integer computedDays,
        Integer effectiveDays,
        String overrideReason,
        Instant overriddenAt,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
