package br.dev.irontech.seavault.alerts.dto;

import br.dev.irontech.seavault.alerts.domain.AlertStatus;
import jakarta.validation.constraints.NotNull;

public record AlertStatusRequest(@NotNull AlertStatus status) {}
