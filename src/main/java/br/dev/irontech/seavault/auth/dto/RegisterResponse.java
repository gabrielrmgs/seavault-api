package br.dev.irontech.seavault.auth.dto;

import java.util.UUID;

public record RegisterResponse(UUID id, String email) {}
