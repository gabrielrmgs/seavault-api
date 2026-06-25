package br.dev.irontech.seavault.vessels.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record VesselRequest(
        @NotBlank @Size(max = 160) String name,
        UUID typeId,
        @Size(max = 20) String imo,
        @Size(max = 80) String flag,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal grossTonnage,
        @Size(max = 2000) String notes
) {}
