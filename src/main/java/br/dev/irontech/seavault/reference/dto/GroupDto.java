package br.dev.irontech.seavault.reference.dto;

import java.util.UUID;

public record GroupDto(UUID id, String code, String name, int displayOrder) {}
