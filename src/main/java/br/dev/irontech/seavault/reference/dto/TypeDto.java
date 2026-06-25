package br.dev.irontech.seavault.reference.dto;

import java.util.UUID;

public record TypeDto(UUID id, String kind, String code, String label) {}
