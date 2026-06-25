package br.dev.irontech.seavault.reference.dto;

import java.util.UUID;

public record CourseDto(UUID id, String code, String name, Integer workloadHours) {}
