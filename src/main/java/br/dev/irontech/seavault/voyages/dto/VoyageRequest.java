package br.dev.irontech.seavault.voyages.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record VoyageRequest(
        @NotNull LocalDate embarkDate,
        LocalDate disembarkDate,
        UUID vesselId,
        UUID companyId,
        UUID navigationTypeId,
        UUID categoryId,
        @Size(max = 120) String role,
        @Size(max = 120) String embarkPort,
        @Size(max = 120) String disembarkPort,
        @PositiveOrZero Integer computedDays,
        @Size(max = 500) String overrideReason,
        @Size(max = 2000) String notes
) {}
