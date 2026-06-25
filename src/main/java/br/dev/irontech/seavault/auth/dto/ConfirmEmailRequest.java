package br.dev.irontech.seavault.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailRequest(@NotBlank String token) {}
