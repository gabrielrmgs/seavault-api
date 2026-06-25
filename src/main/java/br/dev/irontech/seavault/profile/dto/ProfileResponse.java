package br.dev.irontech.seavault.profile.dto;

import java.util.UUID;

public record ProfileResponse(
        UUID id,
        UUID userId,
        String cir,
        String cpf,
        String rg,
        String nationality,
        String phone,
        String emergencyContact,
        UUID categoryId,
        UUID targetCategoryId,
        int completionPercent
) {}
