package br.dev.irontech.seavault.profile.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProfileUpdateRequest(
        @Size(max = 40) String cir,
        @Size(min = 11, max = 14) String cpf,
        @Size(max = 20) String rg,
        @Size(max = 60) String nationality,
        @Size(max = 20) String phone,
        @Size(max = 160) String emergencyContact,
        UUID categoryId,
        UUID targetCategoryId
) {}
