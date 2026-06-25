package br.dev.irontech.seavault.courses.dto;

import br.dev.irontech.seavault.courses.domain.CourseStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String name,
        UUID catalogCourseId,
        String institution,
        String modality,
        Integer workloadHours,
        LocalDate startDate,
        LocalDate completionDate,
        CourseStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
