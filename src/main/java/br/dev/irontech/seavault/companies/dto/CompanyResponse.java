package br.dev.irontech.seavault.companies.dto;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String name,
        UUID typeId,
        String cnpj,
        String email,
        String phone,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
