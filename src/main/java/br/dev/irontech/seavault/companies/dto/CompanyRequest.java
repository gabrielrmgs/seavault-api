package br.dev.irontech.seavault.companies.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CompanyRequest(
        @NotBlank @Size(max = 160) String name,
        UUID typeId,
        @Size(max = 18) String cnpj,
        @Email @Size(max = 160) String email,
        @Size(max = 40) String phone,
        @Size(max = 2000) String notes
) {}
