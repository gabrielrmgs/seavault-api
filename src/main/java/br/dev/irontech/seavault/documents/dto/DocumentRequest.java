package br.dev.irontech.seavault.documents.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record DocumentRequest(
        @NotNull UUID typeId,
        @Size(max = 80) String number,
        @Size(max = 160) String issuer,
        LocalDate issueDate,
        LocalDate expiryDate,
        @Size(max = 2000) String notes
) {}
