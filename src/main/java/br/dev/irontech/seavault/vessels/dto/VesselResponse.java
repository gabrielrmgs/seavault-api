package br.dev.irontech.seavault.vessels.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VesselResponse(
        UUID id,
        String name,
        UUID typeId,
        String imo,
        String flag,
        BigDecimal grossTonnage,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
