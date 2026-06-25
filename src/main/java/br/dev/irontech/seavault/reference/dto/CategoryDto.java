package br.dev.irontech.seavault.reference.dto;

import java.util.UUID;

public record CategoryDto(UUID id, UUID groupId, String code, String name, int progressionOrder) {}
