package br.dev.irontech.seavault.certificates.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CertificateRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 80) String code,
        @Size(max = 160) String institution,
        LocalDate issueDate,
        LocalDate expiryDate,
        @Size(max = 2000) String notes
) {}
