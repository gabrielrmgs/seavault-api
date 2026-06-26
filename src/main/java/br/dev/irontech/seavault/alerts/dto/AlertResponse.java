package br.dev.irontech.seavault.alerts.dto;

import br.dev.irontech.seavault.alerts.domain.AlertSource;
import br.dev.irontech.seavault.alerts.domain.AlertStatus;
import br.dev.irontech.seavault.alerts.domain.AlertType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        AlertSource source,
        UUID sourceId,
        AlertType type,
        String title,
        LocalDate dueDate,
        Integer leadDays,
        AlertStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
