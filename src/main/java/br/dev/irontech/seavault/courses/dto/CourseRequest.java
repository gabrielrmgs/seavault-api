package br.dev.irontech.seavault.courses.dto;

import br.dev.irontech.seavault.courses.domain.CourseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CourseRequest(
        @NotBlank @Size(max = 160) String name,
        UUID catalogCourseId,
        @Size(max = 160) String institution,
        @Size(max = 40) String modality,
        @PositiveOrZero Integer workloadHours,
        LocalDate startDate,
        LocalDate completionDate,
        CourseStatus status,
        @Size(max = 2000) String notes
) {}
